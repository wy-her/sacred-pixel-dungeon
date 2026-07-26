# 138. AttackIndicator 클릭 무반응 버그 수정

**날짜**: 2026-07-25

## 배경

AttackIndicator(공격 버튼)를 클릭해도 반응하지 않는 버그가 간헐적으로 발생. 적이 인접하고 버튼이 정상적으로 표시되어 있어도 클릭이 무시되는 현상. 적을 직접 클릭하면 정상 작동하고, 이후 인디케이터도 정상화됨.

**참고**: #135의 FOV 체크 버그와는 다른 문제. #135는 시야 밖 적에게 이동하는 버그였고, 이번 버그는 클릭 자체가 무시되는 문제.

---

## 원인 분석

### Root Cause: Button.pressedButton Race Condition

`Button.java`의 static 변수 `pressedButton`과 instance 변수 `clickReady` 간의 불일치.

```java
// Button.java
protected static Button pressedButton;  // static - 모든 버튼 공유
protected boolean clickReady;            // instance - 버튼별 별도
```

### 버그 시나리오

1. 사용자가 AttackIndicator DOWN
   - `pressedButton = AttackIndicator`
   - `clickReady = true`

2. DOWN과 UP 사이에 다른 버튼의 DOWN 이벤트 발생 (빠른 연속 터치, 멀티터치 등)
   - `pressedButton = 다른버튼` (탈취됨)

3. 사용자가 AttackIndicator UP
   - `pressedButton != this` 조건에 걸림
   - `clickReady = false` 설정

4. `onClick()` 체크 실패
   - `if (clickReady)` → FALSE → 클릭 무시됨

---

## 변경 사항

### 1. pressedButton 탈취 방지 (포인터 이벤트)

```diff
 protected void onPointerDown( PointerEvent event ) {
     // ... window checks ...
-    pressedButton = Button.this;
-    pressTime = 0;
-    clickReady = true;
-    Button.this.onPointerDown();
+    // 시각적 피드백은 항상 허용
+    Button.this.onPointerDown();
+
+    // 클릭 처리는 첫 번째 버튼만 (이미 눌린 버튼이 없을 때만)
+    // pressedButton이 다른 버튼에 의해 탈취되는 것을 방지
+    if (pressedButton == null) {
+        pressedButton = Button.this;
+        pressTime = 0;
+        clickReady = true;
+    }
 }
```

### 2. pressedButton 탈취 방지 (키보드 이벤트)

키보드 입력에도 동일한 패턴 적용.

```diff
 if (event.pressed){
-    pressedButton = Button.this;
-    pressTime = 0;
-    clickReady = true;
-    Button.this.onPointerDown();
+    // 시각적 피드백은 항상 허용
+    Button.this.onPointerDown();
+
+    // 클릭 처리는 첫 번째 버튼만
+    if (pressedButton == null) {
+        pressedButton = Button.this;
+        pressTime = 0;
+        clickReady = true;
+    }
 }
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `ui/Button.java` | `onPointerDown()`에서 pressedButton 가드 추가 |
| 〃 | 키보드 이벤트에도 동일 가드 적용 |

---

## 영향

- AttackIndicator 클릭 무반응 버그 수정
- ActionIndicator 등 모든 Button 서브클래스에 동일하게 적용
- 시각적 피드백(버튼 눌림 효과)은 항상 동작
- 빠른 연속 클릭 시 첫 번째 버튼이 우선권 유지

---

## 관련 문서

- #135 - 공격 버튼 FOV 체크 버그 수정 (다른 버그)
- `Button.java:61-80` - 포인터 이벤트 처리
- `Button.java:157-166` - 키보드 이벤트 처리
