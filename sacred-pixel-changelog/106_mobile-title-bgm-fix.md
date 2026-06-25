# 106. 모바일 브라우저 타이틀 BGM 버그 수정

**날짜**: 2026-06-25

## 개요

모바일 일반 브라우저(Cloudflare 배포본)에서 타이틀 화면 BGM이 재생되지 않는 회귀 버그 수정. 브라우저 자동재생 정책(autoplay policy)과 AudioContext 상태 관리의 race condition 문제 해결.

---

## 문제 증상

### 모바일 일반 브라우저에서 발생하는 증상

1. 타이틀 화면에서 `theme_1/theme_2` BGM이 재생되지 않음
2. 빈 영역 탭, 버튼 클릭해도 BGM 재생 안 됨
3. 음소거 체크 시 오히려 BGM이 재생되는 역현상
4. 주소창/탭 UI 진입 시 갑자기 BGM 재생됨
5. 인게임 진입하면 BGM은 정상 재생

### 정상 동작 환경

- 데스크탑 브라우저: 정상
- Apps in Toss (WebView): 정상

---

## 원인 분석

### 원인 1: JS audio unlock race condition (가장 중요)

`unlockAudio()`가 `Howler.ctx.resume()` 완료 전에 성공 처리:

```javascript
// 기존 코드 문제점
function unlockAudio() {
    window._spdAudioUnlocked = true;  // 즉시 true 설정
    Howler.ctx.resume().then(...);    // await 없이 fire-and-forget
    setTimeout(function() {
        window._spdTriggerMusicAfterUnlock();
    }, 100);  // 100ms 고정 딜레이 - resume 완료 보장 안 됨
}
```

### 원인 2: `Music.enable(false)` 결함

`isPlaying() == true`일 때만 stop 호출:

```java
// 기존 코드 문제점
if (isPlaying() && !value) {
    stop();  // pending/blocked player는 정리 안 됨
}
```

모바일에서 player가 pending/blocked 상태일 때 `isPlaying()` = false이지만, 음소거 버튼 클릭(user gesture)으로 pending player가 살아나서 역재생 발생.

### 원인 3: `Music.update()` fallback 오판

`isPlaying() == false`를 곡 종료로 오판하여 트랙 상태 혼란:

```java
// 기존 코드 문제점
if (!player.isPlaying()) {
    playNextTrack(player);  // blocked/loading 상태도 곡 종료로 판단
}
```

---

## 변경 사항

### Bug Fixes (4)

---

### [B-1] index.html - `unlockAudio()` async/await 기반 수정

**파일**: `teavm/webapp/index.html:135-220`

**수정 내용**:
- `unlockAudio()`를 `async function`으로 변경
- `await Howler.ctx.resume()`으로 실제 완료 대기
- `Howler.ctx.state === 'running'` 확인 후에만 unlocked 처리
- `_spdPendingMusicTrigger` 플래그로 hook 미등록 시 대응
- `_spdOnJavaMusicHookInstalled()` 함수 추가

```javascript
window._spdAudioUnlocked = false;
window._spdAudioUnlocking = false;
window._spdPendingMusicTrigger = false;

async function unlockAudio() {
    if (window._spdAudioUnlocked || window._spdAudioUnlocking) return;
    window._spdAudioUnlocking = true;

    try {
        if (typeof Howler !== 'undefined' && Howler.ctx) {
            if (Howler.ctx.state === 'suspended') {
                await Howler.ctx.resume();  // 완료 대기
            }
            // ... Howler._autoResume(), silent buffer ...
        }

        // 성공 확인 후에만 unlocked 처리
        if (!window.Howler || !Howler.ctx || Howler.ctx.state === 'running') {
            window._spdAudioUnlocked = true;
            removeUnlockListeners();
            triggerMusicAfterUnlock();
        } else {
            window._spdPendingMusicTrigger = true;
        }
    } catch (e) {
        window._spdPendingMusicTrigger = true;
    } finally {
        window._spdAudioUnlocking = false;
    }
}

// Java hook 등록 후 호출됨
window._spdOnJavaMusicHookInstalled = function() {
    if (window._spdAudioUnlocked || window._spdPendingMusicTrigger) {
        triggerMusicAfterUnlock();
    }
};
```

---

### [B-2] Music.java - `enable(false)` 즉시 player 정리

**파일**: `SPD-classes/src/main/java/com/watabou/noosa/audio/Music.java:287-310`

**수정 내용**:
- `stopPlayerOnly()` 메서드 추가 (trackList/lastPlayed 유지)
- `enable(false)` 시 `isPlaying()` 체크 없이 무조건 `stopPlayerOnly()` 호출

```java
/**
 * Stops and disposes the player without clearing trackList/lastPlayed.
 * Used by enable(false) to ensure pending/blocked players are cleaned up
 * while preserving track info for later restoration.
 */
private synchronized void stopPlayerOnly() {
    if (player != null) {
        player.stop();
        player.dispose();
        player = null;
    }
}

public synchronized void enable( boolean value ) {
    enabled = value;

    if (!value) {
        // Always stop player when disabling, regardless of isPlaying() state.
        // On mobile HTML5, player may exist in pending/blocked state where
        // isPlaying() returns false but audio could start playing later.
        stopPlayerOnly();
        return;
    }

    // Enabling: try to restore previous tracks if not already playing
    if (!isPlaying()) {
        if (trackList != null){
            playTracks(trackList, trackChances, shuffle);
        } else if (lastPlayed != null) {
            play(lastPlayed, looping);
        }
    }
}
```

---

### [B-3] Music.java - `update()` fallback에 grace period 추가

**파일**: `SPD-classes/src/main/java/com/watabou/noosa/audio/Music.java:52-53, 146-196, 244-259`

**수정 내용**:
- `html5TimeSincePlayRequest` 필드 추가
- `play()` 호출 시 타이머 리셋
- `update()` fallback에 grace period 적용 (모바일 5초, 데스크탑 1초)

```java
private float html5TimeSincePlayRequest = 999f;

private synchronized void play(String track, ...) {
    try {
        fadeTime = fadeTotal = -1;
        html5TimeSincePlayRequest = 0f;  // 리셋
        // ...
    }
}

public synchronized void update(){
    html5TimeSincePlayRequest += Game.elapsed;

    // ... fade 로직 ...

    if (DeviceCompat.isHTML5() && enabled && !paused && player != null && fadeTotal == -1f) {
        html5PlayingCheckTimer += Game.elapsed;
        if (html5PlayingCheckTimer >= 0.5f) {
            html5PlayingCheckTimer = 0f;
            if (!player.isPlaying()) {
                // Grace period: mobile 5s, desktop 1s
                float grace = DeviceCompat.isMobile() ? 5f : 1f;

                if (looping) {
                    if (html5TimeSincePlayRequest > grace) {
                        player.play();
                    }
                } else if (trackList != null && trackList.length > 0) {
                    if (html5TimeSincePlayRequest > grace) {
                        playNextTrack(player);
                    }
                }
            }
        }
    }
    // ...
}
```

---

### [B-4] TeaVMLauncher.java - hook 등록 후 JS 알림

**파일**: `teavm/src/main/java/com/sacredpixel/sacredpixeldungeon/teavm/TeaVMLauncher.java:472-489`

**수정 내용**:
- `notifyJavaMusicHookInstalled()` JSBody 메서드 추가
- hook 등록 직후 JS에 알림하여 pending trigger 실행

```java
installJsMusicHooks(pauseCallback, resumeCallback, triggerMusicCallback);

// Notify JS that the music hook is installed.
// If audio was unlocked before this point, JS will now trigger the pending restart.
notifyJavaMusicHookInstalled();

// ...

@JSBody(script =
    "if (typeof window._spdOnJavaMusicHookInstalled === 'function') {" +
    "  window._spdOnJavaMusicHookInstalled();" +
    "}")
private static native void notifyJavaMusicHookInstalled();
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `teavm/webapp/index.html:135-220` | `unlockAudio()` async/await 기반 수정, pending flag 추가 |
| `SPD-classes/.../audio/Music.java:52-53` | `html5TimeSincePlayRequest` 필드 추가 |
| `SPD-classes/.../audio/Music.java:146-196` | `update()` grace period 추가 |
| `SPD-classes/.../audio/Music.java:244-259` | `play()` 호출 시 타이머 리셋 |
| `SPD-classes/.../audio/Music.java:287-310` | `stopPlayerOnly()` 추가, `enable()` 수정 |
| `teavm/.../teavm/TeaVMLauncher.java:472-489` | hook 등록 후 JS 알림 추가 |

---

## 검증 체크리스트

- [ ] 모바일 브라우저: 타이틀 진입 후 첫 터치 시 BGM 재생
- [ ] 모바일 브라우저: 버튼 클릭 시 BGM 재생
- [ ] 모바일 브라우저: 음소거 체크 → BGM 즉시 정지 (역재생 안 됨)
- [ ] 모바일 브라우저: 음소거 해제 → BGM 복구
- [ ] 모바일 브라우저: 인게임 BGM 정상 동작
- [ ] 데스크탑 브라우저: 정상 동작 (회귀 없음)
- [ ] Apps in Toss: 정상 동작 유지

---
