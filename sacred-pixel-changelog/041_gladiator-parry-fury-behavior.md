# 041. 검투사 PARRY/FURY 카운터 행동 버그 수정

**날짜**: 2026-04-05

## 개요
Gladiator의 콤보 필살기(PARRY 6스택 받아치기, FURY 10스택 분노폭발)가 제대로 작동하지 않는 버그를 수정.

---

## 문제

### PARRY (6스택) 버그
- PARRY 사용 후 적의 공격을 회피하지만 반격이 발생하지 않음
- RiposteTracker의 콜백이 실행되지 않거나 조건 검사에서 실패

### FURY (10스택) 버그
- FURY 사용 시 첫 번째 공격만 발생하고 나머지 연속 공격이 실행되지 않음
- 콜백 체인이 중간에 끊어짐

---

## 원인 분석

### PARRY 문제 원인
1. **과도하게 엄격한 riposteIdCounter 검증**:
   - 콜백에서 `riposteIdCounter == myRiposteId` 검사가 실패할 수 있음
   - Enhanced Combo(9스택+)에서 여러 적이 공격할 경우 두 번째 RiposteTracker가 ID를 증가시켜 첫 번째 콜백이 무효화됨

2. **hero.busy() 호출 누락**:
   - RiposteTracker.act()에서 반격 애니메이션 시작 전 hero.busy() 미호출
   - 플레이어 입력이 애니메이션을 방해할 수 있음

3. **sprite null 체크 누락**:
   - `target.sprite.attack()` 호출 전 null 검사 없음

### FURY 문제 원인
1. **hero.busy() 호출 누락**:
   - FURY 연속 공격 콜백에서 hero.busy() 미호출
   - 각 공격 사이에 플레이어 입력이 가능해져 콤보 중단 가능

2. **예외 처리 부재**:
   - 콜백 내 예외 발생 시 게임이 멈추거나 다음 공격이 실행되지 않음
   - HTML5/TeaVM 환경에서 예외가 조용히 무시될 수 있음

---

## 수정 내용

### RiposteTracker 수정

```java
public static class RiposteTracker extends Buff{
    { actPriority = VFX_PRIO;}

    public Char enemy;
    private boolean riposteStarted = false;  // 단순화된 중복 실행 방지

    @Override
    public boolean act() {
        if (riposteStarted) {
            detach();
            return true;
        }

        final Combo combo = target.buff(Combo.class);
        // FIX: sprite null 체크 추가
        if (combo != null && enemy != null && enemy.isAlive() && target.sprite != null) {
            riposteStarted = true;
            final Char myTarget = target;
            final Char myEnemy = enemy;

            combo.moveBeingUsed = ComboMove.PARRY;

            // FIX: hero.busy() 추가
            if (myTarget instanceof Hero) {
                ((Hero)myTarget).busy();
            }

            target.sprite.attack(enemy.pos, new Callback() {
                @Override
                public void call() {
                    // FIX: riposteIdCounter 검증 제거 (riposteStarted로 충분)
                    try {
                        Combo currentCombo = myTarget.buff(Combo.class);
                        if (currentCombo != null && myEnemy.isAlive()) {
                            currentCombo.doAttack(myEnemy);
                        } else {
                            if (myTarget instanceof Hero) {
                                ((Hero)myTarget).spendAndNext(Actor.TICK);
                            } else {
                                next();
                            }
                        }
                    } catch (Exception e) {
                        // FIX: 예외 발생 시에도 턴이 진행되도록
                        com.watabou.noosa.Game.reportException(e);
                        if (myTarget instanceof Hero) {
                            ((Hero)myTarget).next();
                        } else {
                            next();
                        }
                    }
                }
            });
            detach();
            return false;
        } else {
            detach();
            return true;
        }
    }
}
```

### FURY 연속 공격 수정

```java
case FURY:
    // ... (기존 코드)
    if (furyHitsLeft > 0 && hero.isAlive() && enemy.isAlive() && ...) {
        final Combo thisCombo = this;
        final Char furyEnemy = enemy;
        final Hero furyHero = hero;

        // FIX: hero.busy() 추가 - 연속 공격 중 입력 방지
        hero.busy();

        target.sprite.attack(enemy.pos, new Callback() {
            @Override
            public void call() {
                try {
                    if (furyHero.isAlive() && furyEnemy.isAlive()) {
                        thisCombo.doAttack(furyEnemy);
                    } else {
                        // 정리 코드
                        thisCombo.furyHitsLeft = 0;
                        thisCombo.detach();
                        ActionIndicator.clearAction(thisCombo);
                        furyHero.next();
                    }
                } catch (Exception e) {
                    // FIX: 예외 발생 시에도 턴이 진행되도록
                    com.watabou.noosa.Game.reportException(e);
                    thisCombo.furyHitsLeft = 0;
                    thisCombo.detach();
                    ActionIndicator.clearAction(thisCombo);
                    furyHero.next();
                }
            }
        });
    }
```

### 기타 콜백 수정

모든 Combo 관련 콜백에 동일한 패턴 적용:
- `hero.busy()` 호출 추가 (필요한 경우)
- try-catch로 예외 처리
- `Dungeon.hero` 대신 캡처된 `final Hero` 변수 사용

---

## 수정 파일

- `actors/buffs/Combo.java`
  - RiposteTracker.act() - 간소화 및 안전장치 추가
  - doAttack() FURY case - hero.busy() 및 try-catch 추가
  - CellSelector.Listener 콜백들 - try-catch 추가

---

## 기술적 세부사항

### riposteIdCounter 제거 이유

기존 코드:
```java
riposteId = ++riposteIdCounter;
final int myRiposteId = riposteId;
// ... callback ...
if (riposteIdCounter == myRiposteId) {
    // execute
}
```

문제점:
- Enhanced Parry(9스택+, 재능 2레벨)에서 여러 적이 공격 시
- 두 번째 RiposteTracker가 생성되면 riposteIdCounter 증가
- 첫 번째 콜백 실행 시 ID 불일치로 반격 무효화

해결:
- `riposteStarted` 플래그만으로 중복 실행 방지 가능
- 각 RiposteTracker 인스턴스는 독립적으로 동작

### HTML5 콜백 예외 처리

HTML5/TeaVM 환경에서:
- 콜백 내 예외가 상위로 전파되지 않을 수 있음
- 게임이 "멈춤" 상태가 되는 원인
- try-catch로 예외를 캡처하고 turnAdvance를 보장

---

## 테스트 체크리스트

- [ ] 콤보 6회+ → PARRY → 적 공격 → 회피 + 반격 정상 작동
- [ ] 콤보 10회+ → FURY → 모든 연속 공격 완료 (10회)
- [ ] FURY 중 적 사망 → 다음 적 또는 정상 종료
- [ ] Enhanced Parry (9스택+) → 여러 적 공격 시 반격 정상 작동
- [ ] 점프 공격 (Enhanced Combo 3레벨) → 정상 작동
- [ ] 게임 저장/로드 중 FURY → 공격 계속

---

## 이전 Changelog와의 관계

- **Changelog 61**: Combo 버그 수정 (RiposteTracker ID, Bundle 저장)
- **Changelog 63 (본 문서)**: 콜백 안전성 개선, riposteIdCounter 제거

61번에서 riposteIdCounter 검증을 추가했으나, 이것이 오히려 Enhanced Parry에서 문제를 일으킬 수 있어 63번에서 제거.
`riposteStarted` 플래그가 중복 실행을 방지하는 데 충분함.
