# 080. 강철의 위장 (Iron Stomach) 버그 수정

**날짜**: 2026-06-01

## 개요

전사의 2-1 특성 "강철의 위장(Iron Stomach)"의 데미지 감소 효과가 적용되지 않던 버그를 수정했습니다.

---

## 변경 사항

### 문제 현상

- 강철의 위장 특성을 찍은 상태에서 음식을 먹어도 데미지 감소 효과가 적용되지 않음
- +1 포인트: 75% 데미지 감소가 동작하지 않음
- +2 포인트: 100% 데미지 면역이 동작하지 않음

---

### 원인 분석

### actPriority 문제

`WarriorFoodImmunity` 버프의 `actPriority`가 잘못 설정되어 있었습니다.

**기존 코드:**
```java
public static class WarriorFoodImmunity extends FlavourBuff{
    { actPriority = HERO_PRIO + 1; }  // = 1
}
```

**문제점:**
- 같은 시간(time)에 여러 Actor가 동시에 행동할 때, `actPriority` 값이 **높은** Actor가 먼저 행동
- 버프의 actPriority = 1 (HERO_PRIO + 1)
- 몬스터의 actPriority = -20 (MOB_PRIO)
- 결과: 버프가 몬스터보다 **먼저** 행동하여 분리(detach)됨

### 타임라인 (버그 상태)

```
시간 0.0: 영웅이 음식을 먹음
         └─ WarriorFoodImmunity 버프 적용 (duration = 1.0)
         └─ 영웅 spend(1.0)

시간 1.0: [actPriority 순서로 처리]
         1. 버프 act() 호출 (priority=1) → 즉시 detach()
         2. 몬스터 act() 호출 (priority=-20) → 영웅 공격
            └─ 버프가 이미 없음 → 데미지 감소 미적용!
```

---

### 수정 내용

### WarriorFoodImmunity actPriority 변경

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/actors/hero/Talent.java`

```java
// 수정 전
public static class WarriorFoodImmunity extends FlavourBuff{
    { actPriority = HERO_PRIO + 1; }
}

// 수정 후
public static class WarriorFoodImmunity extends FlavourBuff{
    { actPriority = MOB_PRIO - 1; }  // = -21
}
```

### 수정 후 타임라인

```
시간 0.0: 영웅이 음식을 먹음
         └─ WarriorFoodImmunity 버프 적용 (duration = 1.0)
         └─ 영웅 spend(1.0)

시간 1.0: [actPriority 순서로 처리]
         1. 몬스터 act() 호출 (priority=-20) → 영웅 공격
            └─ 버프 활성 상태 → 데미지 감소 적용!
         2. 버프 act() 호출 (priority=-21) → detach()
```

---

## 기술적 설명

### Actor Priority 시스템

Sacred Pixel Dungeon에서 동일한 시간에 여러 Actor가 행동해야 할 때, `actPriority` 값으로 순서를 결정합니다:

| Actor 타입 | Priority 상수 | 값 |
|-----------|---------------|-----|
| 버프 (before) | BUFF_PRIO | 3 |
| VFX | VFX_PRIO | 2 |
| 영웅 | HERO_PRIO | 0 |
| 블롭 | BLOB_PRIO | -10 |
| 몹 | MOB_PRIO | -20 |

높은 값이 먼저 행동합니다.

### FlavourBuff의 act() 동작

```java
// FlavourBuff.java
@Override
public boolean act() {
    detach();  // 시간이 되면 즉시 분리
    return true;
}
```

`FlavourBuff`는 지속시간이 끝나면 `act()`에서 즉시 `detach()`됩니다. 따라서 actPriority가 몹보다 높으면 몹이 공격하기 전에 버프가 사라집니다.

### 해결책

버프의 actPriority를 `MOB_PRIO - 1 = -21`로 설정하여 몹이 먼저 행동하도록 합니다. 이렇게 하면:
1. 몹이 공격할 때 버프가 아직 활성 상태
2. 데미지 감소 효과 적용
3. 그 후 버프가 act()에서 detach()

---

## 테스트 결과

### 테스트 환경
- 전사 캐릭터
- 강철의 위장 +2 포인트

### 수정 전
```
[IRON_STOMACH] hasTalent=true, cooldown=1.0, points=2, now=0.0
[IRON_STOMACH] Buff applied, buffCooldown=1.0
[IRON_STOMACH] No buff active, damage=8.0, now=1.0  ← 버프 없음!
```

### 수정 후
```
[IRON_STOMACH] hasTalent=true, cooldown=1.0, points=2, now=0.0
[IRON_STOMACH] Buff applied, buffCooldown=1.0
[IRON_STOMACH] Damage reduction: before=8.0, points=2, now=1.0, buffCooldown=0.0
[IRON_STOMACH] Damage reduction: after=0.0  ← 데미지 0으로 감소!
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `core/.../Talent.java` | WarriorFoodImmunity actPriority 변경 |

---

## 참고: ParryTracker도 동일한 문제

전사의 콤보 무브 "패리(Parry)"도 같은 문제가 있었습니다:

```java
public static class ParryTracker extends FlavourBuff{
    { actPriority = MOB_PRIO - 1; }  // 이미 수정됨
}
```

패리는 적의 공격을 회피하고 반격하는 기술입니다. actPriority가 몹보다 높으면 패리 버프가 먼저 사라져서 회피가 동작하지 않습니다.

---
