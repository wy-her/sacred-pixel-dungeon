# 153. 프리즈마 거울상 전투 중 휴식 버그 수정

**날짜**: 2026-08-09

## 배경

프리즈마 거울상이 전투 중일 때 영웅이 휴식을 취하면, 거울상이 죽은 후 적이 영웅을 공격해도 휴식이 중단되지 않거나, 적이 무한히 공격하는 버그가 발생했다. 이 문제는 두 가지 원인이 복합적으로 작용하여 발생했다.

---

## 변경 사항

### 1. 죽은 몹 타겟팅 방지 (`Mob.chooseEnemy()`)

적이 타겟을 선택할 때 `isAlive()` 체크가 누락되어, 죽은 프리즈마 거울상(5턴 fadeout 중인 상태)을 계속 공격하는 문제가 있었다.

**수정 위치**: `Mob.java`의 `chooseEnemy()` 메서드 4곳

```diff
// Line 344: Amoked 몹 → ENEMY 타겟팅
- if (mob.alignment == Alignment.ENEMY && mob != this
+ if (mob.alignment == Alignment.ENEMY && mob != this && mob.isAlive()
        && fieldOfView[mob.pos] && mob.invisible <= 0) {

// Line 352: Amoked 몹 → ALLY 타겟팅
- if (mob.alignment == Alignment.ALLY && mob != this
+ if (mob.alignment == Alignment.ALLY && mob != this && mob.isAlive()
        && fieldOfView[mob.pos] && mob.invisible <= 0) {

// Line 369: ALLY 몹 → ENEMY 타겟팅
- if (mob.alignment == Alignment.ENEMY && fieldOfView[mob.pos]
+ if (mob.alignment == Alignment.ENEMY && mob.isAlive() && fieldOfView[mob.pos]
        && mob.invisible <= 0 && !mob.isInvulnerable(getClass()))

// Line 382: ENEMY 몹 → ALLY 타겟팅 (핵심 수정)
- if (mob.alignment == Alignment.ALLY && fieldOfView[mob.pos] && mob.invisible <= 0)
+ if (mob.alignment == Alignment.ALLY && mob.isAlive() && fieldOfView[mob.pos] && mob.invisible <= 0)
```

### 2. 휴식 중단 시 턴 순서 리셋 (`Hero.interrupt()`)

영웅이 장시간 휴식하면 `time`이 누적되어 적들보다 턴 순서가 뒤로 밀린다. 휴식이 중단되어도 이 시간 격차가 유지되어 적들이 무한히 먼저 행동하는 문제가 있었다.

**수정 위치**: `Hero.java`의 `interrupt()` 메서드

```diff
  resting = false;

+ //On HTML5, reset hero's time when interrupted from resting.
+ //During rest, hero accumulates time (spends TIME_TO_REST each turn), which puts
+ //them behind enemies in turn order. By setting hero.time to before the minimum,
+ //the hero acts before all enemies after being interrupted.
+ if (wasResting) {
+     timeToBeforeMin();
+ }
```

이 수정은 `MagicalSleep.detach()`, `Frost.detach()`, `Paralysis.detach()`와 동일한 패턴을 따른다.

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `core/.../actors/mobs/Mob.java` | `chooseEnemy()`에 `isAlive()` 체크 4곳 추가 |
| `core/.../actors/hero/Hero.java` | `interrupt()`에서 휴식 중단 시 `timeToBeforeMin()` 호출 |

---

## 영향

- 프리즈마 거울상 사망 후 적이 즉시 영웅을 타겟으로 전환
- 휴식 중단 후 영웅이 정상적으로 턴을 받음
- Amok 상태의 몹도 죽은 대상을 공격하지 않음
- 기존 `MagicalSleep`, `Frost`, `Paralysis` 해제 동작과 일관성 유지

---

## 관련 문서 / 코드

- `MagicalSleep.java:95` - `timeToBeforeMin()` 호출 패턴 참조
- `PrismaticImage.java:114-123` - 5턴 fadeout 메커니즘 (`deathTimer`)
- `Actor.java:123-125` - `timeToBeforeMin()` 구현
