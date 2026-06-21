# 098. 공격 버튼 및 랭킹 리플레이 버그 수정

**날짜**: 2026-06-20

## 개요

공격 버튼 클릭 불가 문제, 랭킹에서 "다시 플레이" 시드 불일치 문제, 타겟팅 크로스헤어가 사라지지 않는 문제를 수정했습니다.

---

## 수정된 버그

### 1. 공격 버튼 클릭 불가 (적 1명일 때)

**파일:** `core/src/main/java/com/sacredpixel/sacredpixeldungeon/ui/AttackIndicator.java`

**증상:**
- 적이 1명만 있을 때 공격 버튼이 간헐적으로 클릭되지 않음
- 적을 직접 클릭하면 공격 가능하지만, 공격 버튼(우측 하단)이 반응하지 않음

**원인 분석:**
- `Hero.ready()` 메서드에서 `ready = true` 설정 후 `AttackIndicator.updateState()` 호출
- `onClick()`에서 `enabled` 플래그를 체크했으나, `updateState()`가 아직 `enabled`를 갱신하지 않은 상태에서 클릭 이벤트 발생
- 적이 1명일 때 턴 순환이 빨라 레이스 컨디션 윈도우가 더 자주 발생

**수정 내용:**
```java
@Override
protected synchronized void onClick() {
    super.onClick();
    // enabled 체크 제거 - hero.ready만 확인하여 레이스 컨디션 방지
    // lastTarget 유효성 검증 추가 (죽거나 범위 밖인 경우 대응)
    if (Dungeon.hero.ready && lastTarget != null && Dungeon.hero.canAttack(lastTarget)) {
        if (Dungeon.hero.handle( lastTarget.pos )) {
            Dungeon.hero.next();
            enable(false);
            QuickSlotButton.cancel();
            InventoryPane.cancelTargeting();
        }
    }
}
```

**변경점:**
- `enabled` 체크 제거 → `hero.ready`만 확인
- `synchronized` 키워드 추가로 스레드 안전성 확보
- `canAttack(lastTarget)` 검증 추가로 죽은 적/범위 밖 적 처리

---

### 2. "다시 플레이" 시드 불일치

**파일:** `core/src/main/java/com/sacredpixel/sacredpixeldungeon/windows/WndRanking.java`

**증상:**
- 랭킹에서 이전 게임을 "다시 플레이"하면 UI에 시드가 표시되지만 실제 던전 내용이 다름
- 같은 시드로 시작했는데 아이템 배치, 맵 구조가 다름

**원인 분석:**
- 리플레이 시작 시 `Dungeon.seed`(현재 진행 중인 게임의 시드)를 사용
- 선택한 `record.dungeonSeed`(랭킹 기록의 시드)를 사용해야 함

**수정 전:**
```java
final long seed = Dungeon.seed;
final int challenges = Dungeon.challenges;
```

**수정 후:**
```java
// 현재 Dungeon 상태가 아닌 기록의 시드와 도전과제 사용
final long seed = record.dungeonSeed;
final int challenges = record.challenges;
```

---

### 3. 타겟팅 크로스헤어 잔존

**파일:** `core/src/main/java/com/sacredpixel/sacredpixeldungeon/ui/AttackIndicator.java`

**증상:**
- 퀵슬롯 아이템(물약, 완드 등) 사용 후 적 타겟팅 중 공격 버튼으로 공격하면 크로스헤어가 사라지지 않음
- 빨간 크로스헤어가 화면에 계속 남아있음

**원인 분석:**
- 공격 버튼 클릭 시 `QuickSlotButton`과 `InventoryPane`의 타겟팅 모드가 취소되지 않음
- 직접 클릭으로 공격할 때는 `CellSelector`가 타겟팅을 정리하지만, 버튼 클릭 경로에서는 누락

**수정 내용:**
```java
// onClick() 내에서 공격 성공 후 타겟팅 모드 취소
QuickSlotButton.cancel();
InventoryPane.cancelTargeting();
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|-----------|
| `AttackIndicator.java` | onClick() 레이스 컨디션 수정, synchronized 추가, 타겟팅 취소 추가 |
| `WndRanking.java` | 리플레이 시 record.dungeonSeed/challenges 사용 |

---

## 테스트 체크리스트

- [x] 적 1명 상황에서 공격 버튼 정상 작동
- [x] 적 여러 명 상황에서 공격 버튼 정상 작동
- [x] 랭킹에서 다시 플레이 시 동일한 던전 생성
- [x] 퀵슬롯 타겟팅 후 공격 버튼 사용 시 크로스헤어 정상 제거
- [x] 빠른 연속 클릭 시 중복 공격 방지

---
