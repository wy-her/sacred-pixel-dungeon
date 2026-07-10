# 119. 마비 상태 해제 후 적 공격 버그 수정

**날짜**: 2026-07-10

## 개요

수면(MagicalSleep), 빙결(Frost), 마비(Paralysis) 등 마비 상태에서 해제된 후 적이 계속 공격하는 버그 수정. 마비 중 누적된 시간으로 인해 영웅이 턴 순서에서 뒤처지는 문제를 `timeToBeforeMin()` 기법으로 해결.

---

## 증상

- 영웅이 마비 상태(수면/빙결/마비)에 빠짐
- 상태 해제 시 (피해 또는 시간 경과)
- **예상 동작**: 영웅이 행동권 획득
- **실제 동작**: 적이 계속 공격 (영웅 행동 불가)

---

## 영향받는 버프

| 버프 | 설명 | 수정 필요 |
|------|------|----------|
| MagicalSleep | 자장가 주문서 등 마법 수면 | ✅ 수정 |
| Frost | 빙결 상태 | ✅ 수정 |
| Paralysis | 마비 상태 | ✅ 수정 |
| TimeStasis | 시간 정지 (모든 것이 멈춤) | ❌ 불필요 |

TimeStasis는 영웅과 적 모두 시간이 정지되므로 해제 후 기존 턴 순서 유지가 맞음.

---

## 문제 분석

### 원인: 마비 중 시간 누적

Hero.act()에서 `paralysed > 0`일 때:

```java
if (paralysed > 0) {
    curAction = null;
    spendAndNext( TICK );  // 시간 누적!
    return false;
}
```

마비 중 영웅은 매 턴 시간이 누적됨. 해제 후:
- 영웅 time = 4.0 (누적됨)
- 적 time = 2.5 (공격 턴)

가드 조건 `current.time >= heroTime`에서 `2.5 < 4.0`이므로 적이 차단되지 않음.

---

## 변경 사항

### Actor.java - 시간 리셋 함수 추가

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/actors/Actor.java`

```java
//Returns the minimum time among all actors
public static synchronized float minActorTime() {
    float min = Float.MAX_VALUE;
    for (Actor actor : all) {
        if (actor.time < min) {
            min = actor.time;
        }
    }
    return min == Float.MAX_VALUE ? now : min;
}

//Sets this actor's time to slightly before the minimum actor time
public void timeToBeforeMin() {
    time = minActorTime() - 0.001f;
}
```

### MagicalSleep.java - 기상 시 시간 리셋

```java
@Override
public void detach() {
    if (target.paralysed > 0) {
        target.paralysed--;
    }
    if (target instanceof Hero) {
        ((Hero) target).resting = false;
        target.timeToBeforeMin();  // 시간 리셋
    }
    // ...
}
```

### Frost.java - 빙결 해제 시 시간 리셋

```java
@Override
public void detach() {
    super.detach();
    if (target.paralysed > 0)
        target.paralysed--;
    if (Dungeon.level.water[target.pos])
        Buff.prolong(target, Chill.class, Chill.DURATION/2f);
    if (target instanceof Hero) {
        target.timeToBeforeMin();  // 시간 리셋
    }
}
```

### Paralysis.java - 마비 해제 시 시간 리셋

```java
@Override
public void detach() {
    super.detach();
    if (target.paralysed > 0)
        target.paralysed--;
    if (target instanceof Hero) {
        target.timeToBeforeMin();  // 시간 리셋
    }
}
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `actors/Actor.java` | `minActorTime()`, `timeToBeforeMin()` 함수 추가 |
| `actors/buffs/MagicalSleep.java` | `detach()` 시 `timeToBeforeMin()` 호출 |
| `actors/buffs/Frost.java` | `detach()` 시 `timeToBeforeMin()` 호출 |
| `actors/buffs/Paralysis.java` | `detach()` 시 `timeToBeforeMin()` 호출 |

---

## 수정 효과

| 항목 | Before | After |
|------|--------|-------|
| 수면 해제 후 영웅 행동권 | 적에게 빼앗김 | 즉시 획득 |
| 빙결 해제 후 영웅 행동권 | 적에게 빼앗김 | 즉시 획득 |
| 마비 해제 후 영웅 행동권 | 적에게 빼앗김 | 즉시 획득 |

---

## 기술적 배경

### timeToBeforeMin() 해법

마비 중 누적된 시간 문제를 해결하기 위해:
1. 해제 시 모든 액터 중 최소 시간 탐색
2. 영웅 시간을 `최소값 - 0.001f`로 설정
3. 영웅이 모든 적보다 먼저 행동권 획득

```
수정 후 Timeline:
상태 해제 → timeToBeforeMin() 호출
→ hero.time = min(all actors) - 0.001 = 2.499
→ heroWaiting=true, heroTime=2.499
→ enemy.time(2.5) >= heroTime(2.499) → 차단됨 ✓
→ 영웅 행동권 획득 ✓
```

---

## 관련 파일

- `Actor.java` - HTML5 액터 시스템
- `MagicalSleep.java` - 마법 수면 버프
- `Frost.java` - 빙결 버프
- `Paralysis.java` - 마비 버프
- `TimeStasis.java` - 시간 정지 버프 (수정 안 함)

---
