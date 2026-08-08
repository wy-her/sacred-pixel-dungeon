# 152. Rapier/Katana 런지 능력 지연 발동 버그 수정

**날짜**: 2026-08-08

## 배경

Rapier와 Katana의 런지(lunge) 능력 사용 중 이동 커맨드를 입력하면 능력이 취소되지만, 이후 임의의 시간이 지난 후 갑자기 공격이 발동되고 충전량이 소모되는 버그가 발생했다. 이는 HTML5/TeaVM 환경의 비동기 콜백 처리 특성과 능력 상태 관리의 불완전함이 결합되어 발생한 문제였다.

---

## 문제 분석

### 근본 원인

1. **abilityWeapon 미정리**: `lungeAbility()`에서 점프 후 타겟이 없는 경우의 분기에서 `abilityWeapon`을 `null`로 설정하지 않음
2. **interrupt() 미정리**: `Hero.interrupt()` 호출 시 진행 중인 능력 상태를 정리하지 않음
3. **콜백 잔존**: 점프/공격 애니메이션 콜백이 취소되지 않고 나중에 실행됨

### 버그 재현 시나리오

1. 듀얼리스트가 Rapier 런지 능력 발동
2. 점프 애니메이션 진행 중 이동 커맨드 입력
3. `interrupt()` 호출로 이동 실행, 그러나 `abilityWeapon` 유지
4. 점프 콜백이 나중에 실행되어 공격 발동

---

## 변경 사항

### 1. Rapier.java - "no target" 분기 수정

점프 후 타겟이 공격 범위에 없는 경우 `abilityWeapon`을 명시적으로 정리:

```diff
} else {
    //spends charge but otherwise does not count as an ability use
+   hero.belongings.abilityWeapon = null;
    Charger charger = Buff.affect(hero, Charger.class);
```

### 2. Rapier.java - 공격 콜백 방어적 검사

콜백 실행 시점에 상태 변경을 검사하여 조기 종료:

```java
public void call() {
    // 방어적 검사: 콜백 실행 시점에 상태가 변경된 경우 조기 종료
    if (!hero.isAlive()) {
        hero.belongings.abilityWeapon = null;
        return;
    }
    if (enemy == null || !enemy.isAlive() || !Actor.chars().contains(enemy)) {
        hero.belongings.abilityWeapon = null;
        hero.spendAndNext(hero.attackDelay());
        return;
    }
    // ... 기존 공격 로직
}
```

### 3. CharSprite.java - 애니메이션 취소 메서드 추가

점프 및 애니메이션 콜백을 안전하게 취소할 수 있는 public 메서드 추가:

```java
/**
 * 진행 중인 점프를 취소하고 sprite를 ch.pos로 복귀시킵니다.
 */
public void cancelJump() {
    if (jumpTweener != null) {
        if (ch != null) {
            point(worldToCamera(ch.pos));
        }
        shadowOffset = 0.25f;
        jumpTweener.killAndErase();
        jumpTweener = null;
    }
    jumpCallback = null;
}

/**
 * 현재 설정된 애니메이션 콜백을 취소합니다.
 */
public void cancelAnimCallback() {
    animCallback = null;
}

/**
 * 점프가 현재 진행 중인지 확인합니다.
 */
public boolean isJumping() {
    return jumpTweener != null;
}
```

### 4. Hero.java - interrupt() 능력 상태 정리

`interrupt()` 호출 시 능력 관련 상태를 정리:

```java
// 능력 상태 정리: abilityWeapon과 애니메이션 콜백
// - 대부분의 경우 이미 null이므로 부작용 없음
// - Rapier/Katana 런지 중 interrupt 시 잔존 상태 정리
if (belongings.abilityWeapon != null) {
    belongings.abilityWeapon = null;
}
if (sprite != null) {
    sprite.cancelAnimCallback();
}
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `items/weapon/melee/Rapier.java` | "no target" 분기에서 abilityWeapon 정리, 공격 콜백 방어적 검사 추가 |
| `sprites/CharSprite.java` | `cancelJump()`, `cancelAnimCallback()`, `isJumping()` 메서드 추가 |
| `actors/hero/Hero.java` | `interrupt()`에서 abilityWeapon 및 animCallback 정리 |

---

## 영향

- Rapier/Katana 런지 능력 중 이동 커맨드 입력 시 능력이 깔끔하게 취소됨
- 지연된 공격 발동 및 의도치 않은 충전량 소모 방지
- Katana의 `lungeAbility()` 호출도 동일하게 수정됨 (Rapier의 static 메서드 사용)
- 다른 무기 능력에는 영향 없음 (Rapier/Katana 전용 수정)

---

## 관련 문서

- `Rapier.java:95` - `lungeAbility()` 메서드
- `CharSprite.java:136` - `jump()` 메서드
- `Hero.java:968` - `interrupt()` 메서드
