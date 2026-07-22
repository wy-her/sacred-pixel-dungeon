# 137. 영웅 사망 후 특수능력 아이콘 클릭 방지

## 배경

영웅이 사망한 직후에도 특수능력 아이콘(수도사 에너지, 프리러너 모멘텀, 검투사 콤보 등)을 클릭할 수 있는 문제가 있었습니다. 한 번 클릭 후 창을 닫으면 이후에는 클릭이 차단되었지만, 사망 직후 첫 번째 클릭은 허용되는 상태였습니다.

---

## 원인 분석

`ActionIndicator.onClick()`에서 `Dungeon.hero.ready`만 체크하고 `Dungeon.hero.isAlive()`는 체크하지 않았습니다.

```java
// 기존 코드
if (action != null && Dungeon.hero.ready) {
    action.doAction();
}
```

다른 UI 컴포넌트들(QuickSlotButton, InventoryPane, Toolbar, AttackIndicator 등)은 모두 `isAlive()` 체크를 포함하고 있었으나, ActionIndicator만 누락된 상태였습니다.

---

## 변경 사항

### ActionIndicator.onClick() - isAlive() 체크 추가

```diff
  @Override
  protected void onClick() {
      super.onClick();
-     if (action != null && Dungeon.hero.ready) {
+     if (action != null && Dungeon.hero.isAlive() && Dungeon.hero.ready) {
          action.doAction();
      }
  }
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `ui/ActionIndicator.java:142` | `isAlive()` 체크 추가 |

---

## 영향

- 모든 직업의 특수능력 아이콘이 영웅 사망 후 클릭 불가
- 광전사의 광란(Berserk) 상태는 정상 작동 (HP ≤ 0이어도 `isAlive()` = true)
- 기존 코드베이스 패턴과 일관성 유지

### 영향받는 능력들

| 능력 | 클래스 |
|------|--------|
| 수도사 에너지 | MonkEnergy |
| 프리러너 모멘텀 | Momentum |
| 검투사 콤보 | Combo |
| 광전사 버서크 | Berserk |
| 암살자 준비 | Preparation |
| 저격수 표식 | SnipersMark |
| 성서 충전 | HolyTome.TomeRecharge |
| 무기 충전 | MeleeWeapon.Charger |

---

## 검증

| 검증 항목 | 결과 |
|----------|------|
| 일반 사망 후 능력 아이콘 클릭 차단 | O |
| 광전사 광란 중 능력 사용 가능 | O |
| 부활(Ankh) 후 능력 사용 가능 | O |
| 다른 UI 컴포넌트와 동일한 패턴 | O |

---

## 관련 코드

- `Hero.isAlive()` - 광전사 광란 상태 고려한 생존 여부 판단
- `ActionIndicator.Action.doAction()` - 능력 실행 인터페이스
- `QuickSlotButton.onClick()` - 동일한 패턴 사용 예시
