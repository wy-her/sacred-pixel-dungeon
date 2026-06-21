# 039. 콤보 콜백 안전성 수정

**날짜**: 2026-04-04

## 개요

검투사 필살기 (받아치기 PARRY, 분노폭발 FURY)에서 발생하는 콜백 관련 버그 수정. 한 턴에 여러 행동이 발생하거나, 적과 영웅이 동시에 공격하는 케이스에서 발생하는 문제 해결.

---

## 변경 사항

### 1. 발견된 문제들

#### 문제 #1: RiposteTracker 중복 콜백 버그

**원인:**
- 여러 적이 빠르게 공격할 때, 기존 RiposteTracker가 detach되어도 애니메이션 콜백은 여전히 실행
- 새로운 RiposteTracker가 생성되어도, 이전 RiposteTracker의 콜백이 여전히 `doAttack()` 호출
- 결과: 이중 공격 또는 잘못된 적 공격

**시나리오:**
```
1. 적 A가 영웅 공격 → 영웅 회피 → RiposteTracker(A) 생성
2. RiposteTracker(A).act() → 공격 애니메이션 시작 → detach()
3. 애니메이션 진행 중 적 B가 공격 → 영웅 회피 → RiposteTracker(B) 생성
4. RiposteTracker(A) 콜백 실행 → doAttack(적 A) ← 문제!
5. RiposteTracker(B) 콜백 실행 → doAttack(적 B)
```

### 문제 #2: FURY 콜백 체인 불안정

**원인:**
- FURY 다중 공격 중 영웅/적 사망 시 콜백 체인이 끊어질 수 있음
- sprite가 null이 되면 다음 공격 애니메이션 시작 실패
- 결과: 게임 멈춤 또는 턴 미완료

### 문제 #3: Combo 초기 콜백 안전성 부족

**원인:**
- 점프 공격 또는 일반 공격 콜백에서 영웅/적 상태 체크 없음
- 콜백 실행 전에 적이 사망하거나 영웅이 죽으면 예외 발생 가능

---

## 2. 수정 내용

### 수정 #0: FURY 실행 중 Combo 자동 해제 방지 (Agent 3 발견)

**문제:**
- Combo.act()가 매 틱마다 comboTime을 감소시킴
- comboTime이 0 이하가 되면 detach() 호출
- FURY 10회 공격 중에 comboTime이 0이 되면 Combo가 해제되어 공격 체인 중단

**수정:**
```java
@Override
public boolean act() {
    comboTime -= TICK * HoldFast.buffDecayFactor(target);
    spend(TICK);
    // FIX: Don't detach during FURY multi-hit sequence
    // furyHitsLeft > 0 means FURY is in progress
    if (comboTime <= 0 && furyHitsLeft <= 0) {
        detach();
    }
    return true;
}
```

### 수정 #1: RiposteTracker 콜백 유효성 검사

```java
public static class RiposteTracker extends Buff {
    // 고유 ID로 stale 콜백 감지
    private static int riposteIdCounter = 0;
    private int riposteId;
    private boolean riposteStarted = false;

    @Override
    public boolean act() {
        // 이중 실행 방지
        if (riposteStarted) {
            detach();
            return true;
        }

        if (combo != null && enemy != null && enemy.isAlive()) {
            riposteStarted = true;
            riposteId = ++riposteIdCounter;
            final int myRiposteId = riposteId;

            target.sprite.attack(enemy.pos, new Callback() {
                @Override
                public void call() {
                    // 현재 활성 riposte인지 확인
                    if (riposteIdCounter == myRiposteId) {
                        // 실제 공격 실행
                    }
                    next();
                }
            });
            detach();
            return false;
        }
        // ...
    }
}
```

### 수정 #2: FURY 콜백 안전성 강화

```java
case FURY:
    // 영웅 생존 및 sprite 유효성 체크 추가
    if (furyHitsLeft > 0 && hero.isAlive() && enemy.isAlive() &&
            hero.canAttack(enemy) && target.sprite != null && ...) {
        final Combo thisCombo = this;
        final Char furyEnemy = enemy;
        target.sprite.attack(enemy.pos, new Callback() {
            @Override
            public void call() {
                // 콜백 내에서도 생존 체크
                if (hero.isAlive() && furyEnemy.isAlive()) {
                    thisCombo.doAttack(furyEnemy);
                } else {
                    // FURY 중단 - 정리 및 턴 종료
                    thisCombo.furyHitsLeft = 0;
                    thisCombo.detach();
                    ActionIndicator.clearAction(thisCombo);
                    hero.next();
                }
            }
        });
    }
```

### 수정 #3: listener.onSelect() 콜백 안전성

```java
// 점프 공격
target.sprite.jump(target.pos, leapPos, new Callback() {
    @Override
    public void call() {
        // 점프 후 공격 전 상태 체크
        if (Dungeon.hero.isAlive() && leapEnemy.isAlive() && target.sprite != null) {
            target.sprite.attack(cell, new Callback() {
                @Override
                public void call() {
                    if (Dungeon.hero.isAlive() && leapEnemy.isAlive()) {
                        thisCombo.doAttack(leapEnemy);
                    } else {
                        ((Hero)target).next();  // 턴 종료
                    }
                }
            });
        } else {
            ((Hero)target).next();  // 공격 불가 - 턴 종료
        }
    }
});

// 일반 공격
target.sprite.attack(cell, new Callback() {
    @Override
    public void call() {
        if (Dungeon.hero.isAlive() && attackEnemy.isAlive()) {
            thisCombo.doAttack(attackEnemy);
        } else {
            ((Hero)target).next();  // 공격 불가 - 턴 종료
        }
    }
});
```

---

## 3. 수정된 파일 목록

| 파일 | 변경 내용 |
|------|----------|
| `actors/buffs/Combo.java` | RiposteTracker ID 기반 콜백 검증, FURY 안전성 강화, 초기 콜백 안전성 추가 |

---

## 4. 기술적 배경

### 콜백 생명주기 문제

Anonymous inner class의 콜백은 외부 객체(RiposteTracker)가 detach되어도 여전히 실행됨:

```java
// RiposteTracker.act()에서 생성된 콜백
new Callback() {
    @Override
    public void call() {
        doAttack(enemy);  // 이 시점에 RiposteTracker는 이미 detach됨
        next();           // RiposteTracker.this.next() 여전히 호출 가능
    }
}
```

### 해결 방법: ID 기반 검증

```java
private static int riposteIdCounter = 0;  // 전역 카운터
private int riposteId;                     // 이 인스턴스의 ID

// act()에서:
riposteId = ++riposteIdCounter;
final int myRiposteId = riposteId;

// 콜백에서:
if (riposteIdCounter == myRiposteId) {
    // 아직 활성 상태
}
```

새로운 RiposteTracker가 생성되면 `riposteIdCounter`가 증가하여 기존 콜백은 실행되지 않음.

---

## 5. 테스트 체크리스트

- [ ] PARRY 사용 후 단일 적 riposte 정상 작동
- [ ] PARRY 사용 후 여러 적 공격 시 하나만 riposte
- [ ] FURY 10연타 모두 정상 실행 (comboTime 소진 전에 완료)
- [ ] FURY 10연타 중 comboTime이 0이 되어도 공격 체인 유지
- [ ] FURY 도중 적 사망 시 정상 종료
- [ ] FURY 도중 영웅 사망 시 게임 오버 정상 처리
- [ ] 점프 FURY (Enhanced Combo) 정상 작동
- [ ] 게임 멈춤 현상 없음

---

## 6. 빌드 정보

- **빌드 결과**: BUILD SUCCESSFUL
- **총 클래스 수**: 5,375개
- **출력 경로**: `teavm/build/dist/webapp/`

---

## 7. 관련 수정

- Changelog 59: Animation Callback 기반 턴 완료 시스템 수정 (CrystalWisp, Shaman, Warlock)
