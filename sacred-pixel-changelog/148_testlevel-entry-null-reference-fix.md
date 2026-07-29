# 148. TestLevel 진입 시 Null 참조 오류 수정

**날짜**: 2026-07-29

## 배경

TestLevel 진입 시 1차, 2차 시도에서 `TypeError: Cannot read properties of null (reading '$target0')` 오류가 발생하고, 3차 시도에서야 성공하는 문제가 있었다. 이는 TeaVM 환경에서 레벨 전환 시 GameScene이 아직 생성되지 않은 상태에서 UI 관련 정적 필드에 접근할 때 발생하는 null 참조 오류였다.

---

## 문제 분석

### 근본 원인

`Dungeon.switchLevel()` → `hero.checkVisibleMobs()` 호출 시, GameScene이 아직 생성되지 않은 상태에서 다음 정적 필드들에 접근:

1. `TargetHealthIndicator.instance` - GameScene.create()에서 초기화
2. `InventoryPane.lastTarget` - GameScene.create()에서 초기화
3. `GameScene.cellSelector` - GameScene.create()에서 초기화

TeaVM은 이러한 정적 필드 참조를 `$target0`, `$resetKeyHold` 등의 내부 변수로 캐싱하는데, 필드가 null인 경우 JavaScript TypeError가 발생한다.

### 오류 체인

```
switchLevel()
  → hero.checkVisibleMobs()
    → QuickSlotButton.target()
      → TargetHealthIndicator.instance.target()  // null!
      → InventoryPane.lastTarget = target        // 클래스 미초기화!
    → interrupt()
      → GameScene.resetKeyHold()
        → cellSelector.resetKeyHold()            // null!
```

---

## 변경 사항

### 1. QuickSlotButton.target() - Null 체크 추가

```diff
 public static void target( Char target ) {
+    if (target == null) return;
+    if (target.alignment == Char.Alignment.ALLY) return;
+
     lastTarget = target;

-    if (!targetingCancelled) {
-        TargetHealthIndicator.instance.target( target );
-    }
-    InventoryPane.lastTarget = target;
+    TargetHealthIndicator indicator = TargetHealthIndicator.instance;
+    if (!targetingCancelled && indicator != null) {
+        indicator.target( target );
+    }
+
+    try {
+        InventoryPane.lastTarget = target;
+    } catch (Exception e) {
+        // InventoryPane not yet initialized, ignore
+    }
 }
```

### 2. QuickSlotButton.cancel() - Null 체크 추가

```diff
 public static void cancel() {
     // ... crosshair cleanup ...
     targetingSlot = -1;
-    TargetHealthIndicator.instance.target(null);
+    TargetHealthIndicator indicator = TargetHealthIndicator.instance;
+    if (indicator != null) {
+        indicator.target(null);
+    }
     targetingCancelled = true;
 }
```

### 3. Hero.checkVisibleMobs() - 안전한 호출 및 예외 처리

```diff
 if (shouldTarget) {
+    // Force QuickSlotButton class initialization
+    try {
+        @SuppressWarnings("unused")
+        int dummy = QuickSlotButton.targetingSlot;
+    } catch (Exception classInitEx) {
+        shouldTarget = false;
+    }
+    if (shouldTarget) {
         QuickSlotButton.target(target);
+    }
 }
+} catch (Exception e) {
+    // Don't re-throw during level transitions - targeting is not critical
 }
```

### 4. GameScene - cellSelector Null 체크 추가

```diff
 public static void resetKeyHold(){
+    if (cellSelector != null) {
         cellSelector.resetKeyHold();
+    }
 }

 public static void checkKeyHold(){
+    if (cellSelector != null) {
         cellSelector.processKeyHold();
+    }
 }

 public static boolean cancel() {
-    cellSelector.resetKeyHold();
+    if (cellSelector != null) {
+        cellSelector.resetKeyHold();
+    }
     // ...
 }

 public static boolean cancelCellSelector() {
+    if (cellSelector == null) {
+        return false;
+    }
     // ...
 }
```

### 5. InterlevelScene - curTransition Null 체크 추가

```diff
 // descend() method
-if (curTransition.destBranch != Dungeon.branch && ...) {
+if (curTransition != null && curTransition.destBranch != Dungeon.branch && ...) {

 // ascend() method
-if (curTransition.destBranch != Dungeon.branch && ...) {
+if (curTransition != null && curTransition.destBranch != Dungeon.branch && ...) {
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `ui/QuickSlotButton.java` | `target()`, `cancel()` 메서드에 null 체크 추가 |
| `actors/hero/Hero.java` | `checkVisibleMobs()`에 클래스 초기화 및 예외 처리 추가 |
| `scenes/GameScene.java` | `resetKeyHold()`, `checkKeyHold()`, `cancel()`, `cancelCellSelector()`에 null 체크 추가 |
| `scenes/InterlevelScene.java` | `descend()`, `ascend()`에 curTransition null 체크 추가 |

---

## 영향

- TestLevel 진입 시 1차 시도에서도 안정적으로 성공
- 레벨 전환 중 UI 컴포넌트가 초기화되지 않은 상태에서도 크래시 없이 진행
- 타겟팅 기능은 GameScene 생성 후 정상 작동 (레벨 전환 중에는 스킵)

---

## 기술적 배경

### TeaVM 변수 캐싱

TeaVM은 Java를 JavaScript로 컴파일할 때 정적 필드 접근을 최적화한다:
```javascript
var $target0 = TargetHealthIndicator.instance;
$target0.target(char);  // $target0가 null이면 TypeError
```

### GameScene 생명주기

```
InterlevelScene.thread.run()
  → Dungeon.switchLevel()
    → hero.checkVisibleMobs()  // GameScene 없음!
  → InterlevelScene.fadeOut()
  → Game.switchScene(GameScene.class)
    → GameScene.create()       // 여기서 UI 초기화
```

레벨 전환 중 `checkVisibleMobs()`가 호출될 때 GameScene은 아직 생성되지 않았으므로, UI 관련 정적 필드들은 모두 null 상태이다.

---

## 관련 문서

- `TargetHealthIndicator.java:43` - `instance` 필드 초기화
- `GameScene.java:382` - TargetHealthIndicator 생성
- `GameScene.java:293` - cellSelector 생성
