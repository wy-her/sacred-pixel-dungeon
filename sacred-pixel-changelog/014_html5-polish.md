# 014. HTML5 브라우저 최적화 및 게임플레이 개선

**날짜**: 2026-03-25

## 개요

HTML5 버전의 브라우저 탭 전환 시 BGM 일시정지/재개, 오디오 자동재생 정책 대응, 층간 이동 화면 최소 표시 시간, 시스템 폰트 옵션 복원, 하이라이트 텍스트 간격 수정, 투척물 속도 조정 등 다수의 개선사항 적용.

---

## 변경 사항

### 1. BGM 탭 전환 시 일시정지/재개

### 1-1. JavaScript 이벤트 리스너
**파일:** `teavm/webapp/index.html`

브라우저 `visibilitychange` 이벤트를 감지하여:
- 탭 숨김 (`hidden`): `window._spdPauseMusic()` 호출
- 탭 표시 (`visible`): `window._spdResumeMusic()` 호출 + AudioContext 재개

```javascript
document.addEventListener('visibilitychange', function() {
    if (document.visibilityState === 'hidden') {
        spdTriggerSave();
        if (typeof window._spdPauseMusic === 'function') {
            window._spdPauseMusic();
        }
    } else if (document.visibilityState === 'visible') {
        resumeAudioContext();
        if (typeof window._spdResumeMusic === 'function') {
            window._spdResumeMusic();
        }
    }
});
```

### 1-2. Java-JS 브릿지 콜백
**파일:** `teavm/.../TeaVMLauncher.java`

`JsVoidCallback` 인터페이스와 `@JSFunctor` 어노테이션을 사용하여 Java에서 `Music.INSTANCE.pause()`/`resume()` 호출.

```java
@JSFunctor
public interface JsVoidCallback extends JSObject {
    void call();
}

private static void registerMusicHooks() {
    JsVoidCallback pauseCallback = new JsVoidCallback() {
        @Override
        public void call() {
            if (Music.INSTANCE.isPlaying()) {
                musicPausedByVisibility = true;
                Music.INSTANCE.pause();
            }
        }
    };
    // ... resumeCallback, triggerMusicCallback 유사 구현
    installJsMusicHooks(pauseCallback, resumeCallback, triggerMusicCallback);
}
```

---

## 2. 오디오 자동재생 정책 대응

### 2-1. 사용자 제스처 후 음악 트리거
**파일:** `teavm/webapp/index.html`, `teavm/.../TeaVMLauncher.java`

브라우저 자동재생 정책으로 인해 첫 사용자 상호작용(클릭/터치/키보드) 전까지 오디오 재생 불가. `unlockAudio()` 함수에서 AudioContext 해제 후 `window._spdTriggerMusicAfterUnlock()` 호출하여 대기 중이던 BGM 재생.

```javascript
// index.html
if (typeof window._spdTriggerMusicAfterUnlock === 'function') {
    setTimeout(function() {
        window._spdTriggerMusicAfterUnlock();
    }, 100);
}
```

```java
// TeaVMLauncher.java
JsVoidCallback triggerMusicCallback = new JsVoidCallback() {
    @Override
    public void call() {
        if (Music.INSTANCE.isEnabled() && !Music.INSTANCE.isPlaying() && !Music.INSTANCE.paused()) {
            Music.INSTANCE.resume();
        }
    }
};
```

---

## 3. 층간 이동 화면 최소 표시 시간

### 3-1. 1.5초 최소 표시
**파일:** `core/.../scenes/InterlevelScene.java`

층간 이동(로딩) 화면이 너무 빨리 사라지는 문제 해결. 최소 1.5초간 표시 후 다음 단계로 진행.

```java
private static final float MINIMUM_DISPLAY_TIME = 1.5f;
private float displayTimeElapsed = 0f;
private boolean waitingForMinTime = false;

@Override
public void update() {
    super.update();
    displayTimeElapsed += Game.elapsed;
    // ... 로딩 완료 후 최소 시간 대기 로직
}
```

---

## 4. 시스템 폰트 옵션 복원

### 5-1. 설정 UI에 체크박스 추가
**파일:** `core/.../windows/WndSettings.java`

UITab에 "시스템 폰트 사용" 체크박스 복원. 토글 시 `SPDSettings.systemFont()` 설정 변경 및 씬 리로드.

```java
CheckBox chkSystemFont;

chkSystemFont = new CheckBox(Messages.get(this, "system_font")) {
    @Override
    protected void onClick() {
        super.onClick();
        SPDSettings.systemFont(checked());
        SacredPixelDungeon.seamlessResetScene();
    }
};
chkSystemFont.checked(SPDSettings.systemFont());
add(chkSystemFont);
```

---

## 5. 하이라이트 텍스트 뒤 공백 제거

### 렌더링 개선
**파일:** `core/.../ui/RenderedTextBlock.java`

**문제:** `_노란텍스트_ 다음단어` 형식에서 하이라이트 종료 후 공백 토큰이 그대로 렌더링되어 노란 글자 뒤에 불필요한 공백 표시.

**수정:** `build()` 메서드에서 하이라이트가 끝난 직후(`justEndedHighlight`)의 SPACE 토큰을 건너뛰도록 처리.

```java
boolean justEndedHighlight = false;
for (String str : tokens) {
    if ((str.equals("_") || str.equals("**")) && highlightingEnabled) {
        boolean wasHighlighting = highlighting;
        highlighting = !highlighting;
        justEndedHighlight = wasHighlighting && !highlighting;
    } else if (str.equals(" ")) {
        // 하이라이트 종료 직후 공백 건너뛰기
        if (!justEndedHighlight) {
            words.add(SPACE);
        }
        justEndedHighlight = false;
    } else {
        // ...
        justEndedHighlight = false;
    }
}
```

---

## 6. 투척물 속도 향상

### 비행 시간 단축
**파일:** `core/.../sprites/MissileSprite.java`

투척물 비행 시간 계산의 배율을 0.8f에서 **0.6f**로 변경하여 투척물이 더 빠르게 날아감.

**변경 전:**
```java
float duration = tiles * CharSprite.DEFAULT_MOVE_INTERVAL * 0.8f;
```

**변경 후:**
```java
float duration = tiles * CharSprite.DEFAULT_MOVE_INTERVAL * 0.6f;
```

---

## 7. 빌드 환경 정리

### Android 리소스 정리
**경로:** `android/src/main/res/`

Windows `desktop.ini` 파일이 Android 리소스 폴더에 포함되어 빌드 실패 유발. 해당 파일들 삭제:
- `mipmap-*/desktop.ini`
- `values*/desktop.ini`

### local.properties 생성
**파일:** `local.properties`

Android SDK 경로 설정 파일 생성:
```properties
sdk.dir=[ANDROID_SDK_PATH]
# Example: C:\\Users\\<username>\\AppData\\Local\\Android\\Sdk
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `teavm/webapp/index.html` | visibilitychange BGM 제어, 오디오 언락 후 음악 트리거 |
| `teavm/.../TeaVMLauncher.java` | JsVoidCallback 브릿지, 음악 pause/resume/trigger 훅 |
| `core/.../scenes/InterlevelScene.java` | 최소 1.5초 표시 |
| `core/.../windows/WndSettings.java` | 시스템 폰트 체크박스 복원 |
| `core/.../ui/RenderedTextBlock.java` | 하이라이트 뒤 공백 제거 |
| `core/.../sprites/MissileSprite.java` | 투척물 속도 0.8f → 0.6f |
| `local.properties` | Android SDK 경로 설정 (신규) |

---

## 빌드 결과물

- **HTML5:** `teavm/build/dist/webapp/`
