# 022. 영웅 자동이동 인터럽트 수정

**날짜**: 2026-03-26

## 개요

영웅이 자동이동 중 적의 공격을 받으면 즉시 멈추지 않고 추가 이동하거나, 타일 사이에서 멈추는 버그를 수정.

---

## 변경 사항

### 문제 현상

영웅이 먼 타일을 클릭하여 자동이동(auto-move) 중일 때, 적이 영웅을 공격하면 다음과 같은 비정상 동작이 발생했다:

1. **영웅이 즉시 멈추지 않음**: 적의 공격 커맨드가 발생해도 영웅이 몇 턴 더 이동한 후에야 멈춤
2. **적의 순간이동 현상**: 공격 후 적이 한 턴에 여러 타일을 한번에 이동하여 영웅을 따라오는 것처럼 보임
3. **타일 사이 정지**: 인터럽트 시 영웅 스프라이트가 타일 위가 아닌 타일 사이의 임의 위치에서 멈춤

### 기대 동작

- 적의 공격 시 영웅은 **현재 이동 중인 타일까지 도착한 후** 멈춰야 함
- 영웅은 항상 **정확한 타일 위치**에서 멈춰야 함
- 적의 이동은 턴 단위로 자연스럽게 보여야 함

---

## 근본 원인 분석

### 원인 1: 공격 애니메이션 중 영웅 추가 이동 (HTML5/TeaVM)

**Actor 턴 처리 시스템**에서 문제가 발생한다.

```
Hero 우선순위: 0 (높음)
Mob 우선순위: -20 (낮음)
```

HTML5 환경에서의 턴 처리 흐름:

```
Frame 1: Hero가 이동 (spend 1.0) → actResult=true → yield (break)
Frame 2: Hero 스프라이트 이동 애니메이션 재생 중 → break
Frame 3: 스프라이트 완료 → Mob 선택 (시간 가장 낮음) → doAttack() 호출
         → sprite.attack() 애니메이션 시작 → return false → Mob에 waitingForCallback 설정
Frame 4: Mob은 waitingForCallback (스킵) → Hero가 다시 선택됨 → Hero 추가 이동!
Frame 5: Hero 또 이동...
Frame N: Mob 공격 애니메이션 완료 → 콜백에서 attack() → Char.hit() → interrupt()
         → 이 시점에서야 Hero가 멈추지만, 이미 여러 타일 이동한 상태
```

**핵심**: `Mob.doAttack()`가 공격 애니메이션을 시작하고 `false`를 반환하면, Mob은 `waitingForCallback` 상태가 된다. 이 동안 Hero는 계속 선택되어 자동이동을 지속한다. `interrupt()`는 애니메이션 콜백(`onAttackComplete`)에서야 호출되므로, 그때까지 Hero는 이미 여러 타일을 추가 이동한 상태다.

### 원인 2: finishMotion()의 스프라이트 위치 문제

기존 `Hero.interrupt()`에서 `sprite.finishMotion()`을 호출하여 이동 애니메이션을 즉시 중단했다.

```java
// CharSprite.finishMotion()
public void finishMotion() {
    if (motion != null) {
        motion.killAndErase();  // tweener 강제 제거
        motion = null;
    }
    isMoving = false;
}
```

문제: `killAndErase()`는 motion tweener를 제거하지만 **스프라이트 위치를 목적지로 이동시키지 않는다**. 스프라이트가 보간(interpolation) 중간 위치에서 그대로 멈추게 되어, 타일 사이의 임의 위치에 영웅이 표시된다.

---

## 해결 방법

### 수정 1: Mob.doAttack()에서 즉시 인터럽트

`Mob.doAttack()`에서 공격 애니메이션 시작 **전**에 `hero.interrupt()`를 호출하여, 영웅의 `curAction`을 즉시 `null`로 설정한다. 이렇게 하면 다음 프레임에서 Hero가 선택되더라도 `curAction == null`이므로 `ready()`가 호출되어 자동이동이 중단된다.

**파일**: `actors/mobs/Mob.java` — `doAttack()` 메서드

```java
protected boolean doAttack( Char enemy ) {
    if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
        // 공격 애니메이션 시작 전에 영웅 자동이동 즉시 취소.
        // 애니메이션 콜백까지 기다리면 HTML5에서 영웅이 추가 이동함.
        if (enemy == Dungeon.hero && Dungeon.hero.damageInterrupt) {
            Dungeon.hero.interrupt();
        }
        sprite.attack( enemy.pos );
        return false;
    } else {
        attack( enemy );
        Invisibility.dispel(this);
        spend( attackDelay() );
        return true;
    }
}
```

**같은 수정 적용**: `Goo.java`도 `doAttack()`를 오버라이드하면서 `super.doAttack()`를 호출하지 않으므로, 동일한 인터럽트 코드를 별도로 추가했다.

### 수정 2: interrupt()에서 finishMotion() 제거

`Hero.interrupt()`에서 `sprite.finishMotion()` 호출을 제거하여, 이동 애니메이션이 자연스럽게 완료되도록 한다.

**파일**: `actors/hero/Hero.java` — `interrupt()` 메서드

```java
public void interrupt() {
    if (isAlive() && curAction != null &&
        ((curAction instanceof HeroAction.Move && curAction.dst != pos) ||
        (curAction instanceof HeroAction.LvlTransition))) {
        lastAction = curAction;
    }
    curAction = null;
    path = null;
    GameScene.resetKeyHold();
    resting = false;
    recentlyInterrupted = true;
    // finishMotion() 호출하지 않음.
    // 영웅은 현재 이동 중인 타일까지 도착한 후 멈춰야 한다.
    // finishMotion()은 tweener를 killAndErase()로 제거하지만
    // 스프라이트 위치를 목적지로 갱신하지 않아 타일 사이에서 멈추는 버그 발생.
    // Actor.process()가 sprite.isMoving을 체크하여 애니메이션 완료를 대기하고,
    // recentlyInterrupted가 onMotionComplete()에서 checkKeyHold() 재시작을 방지.
}
```

### 수정이 안전한 이유

| 검증 항목 | 결과 |
|-----------|------|
| `curAction = null` | Hero.act()에서 `ready()` 호출 → 자동이동 중단 |
| `recentlyInterrupted = true` | onMotionComplete()에서 checkKeyHold() 스킵 → 키 홀드 이동 재시작 방지 |
| `Actor.process()` isMoving 체크 | 스프라이트 애니메이션 완료 전 Hero.act() 실행 안됨 |
| finishMotion() 제거 | 스프라이트가 목적 타일까지 자연 이동 → 타일 사이 정지 불가 |
| `damageInterrupt` 플래그 | `resume()` 후에는 false → 플레이어가 명시적으로 이동 재개 시 인터럽트 안됨 |
| Goo (super 미호출) | 별도로 동일 패턴 적용 완료 |
| 기타 doAttack 오버라이드 8개 | 모두 `super.doAttack()` 호출 → 자동 상속 |

---

## 수정 전/후 동작 비교

### 자동이동 중 적의 공격

| | 수정 전 | 수정 후 |
|---|---------|---------|
| 영웅 이동 | 적 공격 후 2-3타일 추가 이동 | 현재 타일 도착 후 즉시 정지 |
| 적 위치 | 한번에 여러 타일 점프 (순간이동) | 턴 단위로 자연스럽게 이동 |
| 스프라이트 위치 | 타일 사이 임의 위치 | 항상 정확한 타일 위 |

### 단일 이동 중 적의 공격

| | 수정 전 | 수정 후 |
|---|---------|---------|
| 이동 애니메이션 | 즉시 중단 (타일 사이) | 목적 타일까지 완료 후 정지 |
| 시각적 완성도 | 부자연스러운 정지 | 자연스러운 도착 |

---

## 수정된 파일

| File | Changes |
|------|-----------|
| `actors/mobs/Mob.java` | `doAttack()`에서 공격 애니메이션 시작 전 `hero.interrupt()` 호출 추가 |
| `actors/mobs/Goo.java` | `doAttack()` 오버라이드에 동일한 인터럽트 로직 추가 |
| `actors/hero/Hero.java` | `interrupt()`에서 `sprite.finishMotion()` 호출 제거 |

---

## 관련 메서드 참조

- `Mob.doAttack()` — `Mob.java:664`
- `Goo.doAttack()` — `Goo.java:182`
- `Hero.interrupt()` — `Hero.java:958`
- `Hero.onMotionComplete()` — `Hero.java:2359`
- `CharSprite.finishMotion()` — `CharSprite.java:243`
- `Actor.process()` — `Actor.java:270` (isMoving 체크: 328-364)
- `Char.hit()` — `Char.java:639` (기존 인터럽트)
- `Hero.damage()` — `Hero.java:1607` (기존 인터럽트)
