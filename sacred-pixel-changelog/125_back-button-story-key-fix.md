# 125. 안드로이드 뒤로 버튼 및 스토리 키 입력 수정

## 배경
이전 변경(#123)에서 Cloudflare 버전의 Android 뒤로 버튼을 ESC 키로 매핑하려 했으나, Firefox와 Samsung Internet에서 브라우저 버그로 인해 정상 작동하지 않는 문제가 있었습니다.

또한 InterlevelScene의 스토리가 의도치 않은 키 이벤트에 의해 넘어가는 버그가 발견되었습니다.

## 문제 1: Cloudflare 뒤로 버튼 호환성
Firefox Android의 알려진 버그([bugzilla #1813755](https://bugzilla.mozilla.org/show_bug.cgi?id=1813755))로 인해 `history.pushState()`로 조작된 히스토리를 물리적 뒤로가기 버튼이 무시합니다. Samsung Internet도 유사한 동작을 보입니다.

**결론**: 브라우저 구조상 해결 불가능한 문제로 판단하여, Cloudflare 버전에서는 뒤로 버튼 ESC 매핑 기능을 **제거**하고 일반 브라우저 동작(페이지 뒤로 이동)으로 복원.

## 문제 2: 스토리 키 입력 제한
InterlevelScene의 스토리 키 리스너가 **모든** 키 릴리즈 이벤트를 수용하고 있어, 외부 요인에 의한 예기치 않은 키 이벤트가 스토리를 자동으로 넘기는 버그 발생.

## 수정 내용

### 1. Cloudflare 뒤로 버튼 핸들러 제거

#### teavm/webapp/index.html

```diff
        }, false);

-        // --- Android back button handler (Cloudflare/PWA) ---
-        // Intercept browser back navigation and send ESC key to game instead.
-        // ...
-        (function() {
-            var backHandled = false;
-            function handleBackButton() { ... }
-            history.pushState({ spdGame: true }, '');
-            if (typeof navigation !== 'undefined' && navigation.addEventListener) { ... }
-            window.addEventListener('popstate', function(e) { ... });
-        })();

        // Global error handler
```

약 54줄의 back button handler 코드 블록 전체 제거.

### 2. InterlevelScene 스토리 키 제한

#### core/.../scenes/InterlevelScene.java

```diff
+ import com.badlogic.gdx.Input;
+ import com.sacredpixel.sacredpixeldungeon.SPDAction;
+ import com.watabou.input.KeyBindings;

  Signal.Listener<KeyEvent> keyListener = new Signal.Listener<KeyEvent>() {
      @Override
      public boolean onSignal(KeyEvent keyEvent) {
          // Require minimum delay after button is enabled to prevent accidental dismissal
+         // Only accept specific keys: Enter, Space, or BACK action (ESC/Android back)
+         // This prevents accidental story dismissal from unexpected key events
          if (!keyEvent.pressed && btnContinue.active && btnContinueEnabledTime >= BTN_INPUT_DELAY){
+             int key = keyEvent.code;
+             boolean isAcceptedKey = key == Input.Keys.ENTER
+                     || key == Input.Keys.NUMPAD_ENTER
+                     || key == Input.Keys.SPACE
+                     || KeyBindings.getActionForKey(keyEvent) == SPDAction.BACK;
+
+             if (isAcceptedKey) {
                  btnContinue.enable(false);
                  KeyEvent.removeKeyListener(this);
                  startStoryFadeOut();
                  return true;
+             }
          }
          return false;
      }
  };
```

## 허용되는 스토리 진행 키

| 키 | 설명 |
|----|------|
| `Enter` | 표준 확인 키 |
| `Numpad Enter` | 숫자패드 엔터 |
| `Space` | 스페이스바 |
| `SPDAction.BACK` | ESC, Backspace, Numpad Dot, Android Back |

## 플랫폼별 동작 비교

| 기능 | Cloudflare | 앱인토스 |
|------|------------|----------|
| 브라우저 뒤로 버튼 | 페이지 뒤로 이동 (기본 동작) | ESC 키로 매핑 (유지) |
| 스토리 키 입력 | Enter/Space/Back만 허용 | Enter/Space/Back만 허용 |

## 영향

- **Cloudflare**: 뒤로 버튼이 일반 브라우저처럼 작동 (이전 페이지로 이동)
- **앱인토스**: 뒤로 버튼 ESC 매핑 기능 그대로 유지
- **모든 플랫폼**: 예기치 않은 키 이벤트로 스토리가 넘어가지 않음

## 관련 변경
- #123: Cloudflare 버전 Android 뒤로 버튼 호환성 개선 (이번 변경으로 롤백됨)
