# 145. Wait 버튼 사망 시 UI 정지 버그 수정

**날짜**: 2026-07-26

## 배경

영웅이 휴식(Wait 버튼 꾹 누르기) 중 사망하면 Wait 버튼이 강조 상태로 고정되고, 이후 모든 UI 입력이 차단되어 게임이 완전히 정지되는 버그가 발생했습니다.

특히 Wait 버튼을 빠르게 연타하면서 사망하면 Game Over 화면의 모든 버튼이 클릭에 반응하지 않는 심각한 문제가 있었습니다.

---

## 문제 분석

### 증상

1. Wait 버튼을 길게 눌러 휴식 상태 진입 (또는 빠르게 연타)
2. 휴식 중 적에게 공격받아 사망
3. Wait 버튼이 밝은 상태로 고정됨
4. Game Over 화면의 모든 버튼(재시작, 메뉴)이 클릭 불가
5. 게임 완전 정지 (강제 새로고침 필요)

### 원인

세 가지 문제가 복합적으로 작용:

1. **CellSelector 키 리스너 충돌**: `gameOver()`에서 `deathKeyListener`를 등록하지만, 기존 `cellSelector`의 키 리스너가 해제되지 않아 두 리스너가 동시에 활성화됨

2. **Tool.enable() 시각적 상태 미초기화**: `enable(false)` 호출 시 아이콘 투명도만 변경하고 `base` 색상이 초기화되지 않아, 버튼이 눌린 상태의 밝기로 고정됨

3. **Button.pressedButton static 변수 미초기화**: 빠른 연타 중 사망 시 `pressedButton`이 이전 Wait 버튼을 계속 참조하여, 새로운 Game Over 버튼들이 클릭을 처리할 수 없음 (#138 changelog의 `if (pressedButton == null)` 가드에 의해 차단됨)

---

## 변경 사항

### 1. GameScene.gameOver() - UI 상태 정리 추가

사망 시 cellSelector와 영웅 상태를 명시적으로 정리하여 입력 충돌 방지:

```diff
  public static void gameOver() {
      if (scene == null) return;

+     // Clean up UI state to prevent input conflicts
+     if (cellSelector != null) {
+         cellSelector.resetKeyHold();
+         cellSelector.enabled = false;
+         cellSelector.listener = defaultCellListener;
+     }
+     if (Dungeon.hero != null) {
+         Dungeon.hero.resting = false;
+         Dungeon.hero.curAction = null;
+     }
+
+     // Reset static button state to allow Game Over buttons to receive input
+     // (prevents stuck pressedButton from rapid Wait button spam before death)
+     Button.clearPressedButton();

      Banner gameOver = new Banner( BannerSprites.get( BannerSprites.Type.GAME_OVER ) );
```

### 2. Toolbar.Tool.enable() - 버튼 시각적 상태 초기화 추가

비활성화 시 base 색상도 함께 초기화:

```diff
  public void enable( boolean value ) {
      if (value != active) {
          if (icon != null) icon.alpha( value ? 1f : 0.4f);
          active = value;
+         // Reset base color to match active state
+         if (value) {
+             base.resetColor();
+         } else {
+             base.tint( BGCOLOR, 0.7f );
+         }
      }
  }
```

### 3. Button.clearPressedButton() - 정적 초기화 메서드 추가

외부에서 `pressedButton` 상태를 안전하게 초기화할 수 있는 메서드 추가:

```diff
  //only one button should be pressed at a time
  protected static Button pressedButton;
  protected float pressTime;
  protected boolean clickReady;

+ // Clears the global pressed button state (e.g., on scene transitions or hero death)
+ public static void clearPressedButton() {
+     pressedButton = null;
+ }
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `ui/Button.java:57-60` | `clearPressedButton()` 정적 메서드 추가 |
| `scenes/GameScene.java:106` | `Button` import 추가 |
| `scenes/GameScene.java:1595-1609` | `gameOver()`에 UI 상태 정리 및 `clearPressedButton()` 호출 추가 |
| `ui/Toolbar.java:860-865` | `enable()`에 base 색상 초기화 추가 |

---

## 영향

- 휴식 중 사망 시 UI가 정상적으로 전환됨
- Wait 버튼 연타 중 사망해도 Game Over 버튼이 정상 작동
- Wait 버튼 외 다른 Tool 버튼도 동일한 문제 예방
- 기존 게임플레이에 영향 없음
- #138 changelog의 pressedButton 가드 로직과 상호 보완적으로 작동

---

## 관련 코드

- `CellSelector.resetKeyHold()` - 키 홀드 상태 초기화
- `Tool.onPointerUp()` - 버튼 릴리즈 시 색상 처리 (동일한 패턴 적용)
- `Hero.resting` - 휴식 상태 플래그
- `ItemSlot.enable()` - 동일한 `pressedButton = null` 패턴 사용 (line 331-336)
- #138 - AttackIndicator 클릭 무반응 버그 수정 (pressedButton 가드 추가)

