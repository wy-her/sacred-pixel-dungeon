# 135. 공격 버튼 시야 체크 버그 수정

## 배경

공격 버튼(게임 우측 하단 공격 아이콘)이 가끔 작동하지 않는 버그가 있었습니다. 특히 적이 이동한 직후에 자주 발생했으며, 다른 입력(방향키, 직접 클릭 등)은 정상 작동했습니다.

---

## 원인 분석

### 문제 시나리오

1. 적이 영웅 시야 경계 근처에 있음 → 공격 버튼에 타겟됨
2. 적이 이동하여 **시야 밖**으로 나감 (하지만 무기 범위 내)
3. `canAttack(lastTarget)` = true (범위만 체크)
4. `checkEnemies()` 호출되지 않음 → `lastTarget` 업데이트 안 됨
5. 공격 버튼 클릭 시 `Hero.handle(lastTarget.pos)` 호출
6. `fieldOfView[cell]` = false → 공격 조건 실패
7. **이동 액션**으로 폴백 (공격 대신 이동 시도)

### 근본 원인

`AttackIndicator.update()`에서 `canAttack()`만 체크하고 **시야(heroFOV)**는 체크하지 않았습니다.

```java
// 기존 코드 - 범위만 체크
if (lastTarget != null && !Dungeon.hero.canAttack(lastTarget)) {
    checkEnemies();
}
```

`Hero.handle()`에서는 `fieldOfView[cell]` 체크가 있어서, 시야 밖 적을 공격하려 하면 공격 액션이 설정되지 않고 이동으로 폴백됩니다:

```java
} else if (fieldOfView[cell] && ch instanceof Mob) {
    curAction = new HeroAction.Attack( ch );
} else {
    curAction = new HeroAction.Move( cell );  // ← 폴백
}
```

---

## 변경 사항

### AttackIndicator.update()에 시야 체크 추가

```diff
  if (Dungeon.hero.isAlive()) {

-     //re-check if the current target is still within attack range
-     //this handles enemies that move out of range between updateState() calls
-     if (lastTarget != null && !Dungeon.hero.canAttack(lastTarget)) {
+     //re-check if the current target is still within attack range or field of view
+     //this handles enemies that move out of range or out of sight between updateState() calls
+     if (lastTarget != null &&
+             (!Dungeon.hero.canAttack(lastTarget) || !Dungeon.level.heroFOV[lastTarget.pos])) {
          checkEnemies();
      }
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `ui/AttackIndicator.java:115-117` | 시야(heroFOV) 체크 조건 추가 |

---

## 영향

- 적이 시야 밖으로 이동했을 때 공격 버튼이 올바르게 업데이트됨
- 공격 버튼 클릭 시 의도치 않은 이동 동작 방지
- 다른 공격 방식(직접 클릭, 방향키)에는 영향 없음

---

## 관련 코드

- `AttackIndicator.checkEnemies()` - 공격 가능한 적 목록 갱신
- `Hero.canAttack()` - 공격 범위 체크 (시야 미체크)
- `Hero.handle()` - 셀 클릭 처리, fieldOfView 체크 포함
- `Dungeon.level.heroFOV[]` - 영웅 시야 배열
