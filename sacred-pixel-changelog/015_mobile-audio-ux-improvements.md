# 015. 모바일 오디오 호환성 및 UX 개선

**날짜**: 2026-03-25

## 개요

모바일 브라우저 오디오 호환성, 브라우저 네비게이션 오류 처리, Enter 키 입력 버그 수정, 게임 속도 조정, UI 설정 정리 등 다수의 버그 수정 및 개선사항 적용.

---

## 변경 사항

### 1. 모바일 브라우저 오디오 호환성 개선

### 1-1. 추가 터치 이벤트 리스너
**파일:** `teavm/webapp/index.html`

**문제:** 모바일 브라우저에서 탭 후에도 BGM이 재생되지 않는 경우 발생.

**해결:** 기존 `click`, `touchstart`, `touchend`, `keydown` 이벤트에 `pointerdown`, `pointerup` 이벤트 추가하여 모바일 호환성 향상.

```javascript
// 기존
['click', 'touchstart', 'touchend', 'keydown']

// 변경 후
['click', 'touchstart', 'touchend', 'pointerdown', 'pointerup', 'keydown']
```

### 1-2. AudioContext 오류 처리 강화
**파일:** `teavm/webapp/index.html`

`Howler.ctx.resume()` 호출 시 `.catch()` 추가하여 오류 발생 시에도 게임이 정상 진행되도록 처리.

---

## 2. 브라우저 네비게이션 오류 처리

### 2-1. bfcache 복원 시 페이지 새로고침
**파일:** `teavm/webapp/index.html`

**문제:** 브라우저에서 다른 페이지로 이동 후 뒤로가기로 돌아오면 WebGL 컨텍스트 관련 오류 발생:
```
Uncaught TypeError: Cannot read properties of null (reading 'pA')
Uncaught TypeError: Cannot read properties of null (reading 'pw')
```

**해결:** `pageshow` 이벤트 리스너 추가. bfcache에서 페이지가 복원된 경우(`event.persisted === true`) 자동으로 페이지 새로고침.

```javascript
window.addEventListener('pageshow', function(event) {
    if (event.persisted) {
        console.log('Page restored from bfcache, reloading...');
        window.location.reload();
    }
});
```

---

## 3. Enter 키 버튼 선택 버그 수정

### 3-1. Window 열림 시 Enter 키 처리 수정
**파일:** `core/.../scenes/CellSelector.java`

**문제:** 게임 진행 중 갑자기 Enter 키로 버튼 선택이 되지 않는 현상 발생. 타겟팅 모드(`isTargeting()`)에서 CellSelector가 Enter 키 이벤트를 소비하여 Window에 전달되지 않음.

**해결:** CellSelector에서 Enter/Numpad Enter 키 처리 시 `GameScene.showingWindow()` 체크 추가. Window가 열려있으면 이벤트를 소비하지 않고 Window에 전달.

```java
if ((event.code == com.badlogic.gdx.Input.Keys.ENTER
    || event.code == com.badlogic.gdx.Input.Keys.NUMPAD_ENTER)
    && GameScene.showingWindow()) {
    return false;  // Window에 이벤트 전달
}
```

---

## 4. 게임 속도 조정

### 4-1. 캐릭터 이동 속도 감소
**파일:** `core/.../sprites/CharSprite.java`

**문제:** 게임 속도가 너무 빨라서 플레이하기 어려움.

**해결:** `DEFAULT_MOVE_INTERVAL` 값을 증가시켜 캐릭터 이동 애니메이션 속도 감소.

```java
// 변경 전
public static final float DEFAULT_MOVE_INTERVAL = 0.0864f;

// 변경 후
public static final float DEFAULT_MOVE_INTERVAL = 0.1f;
```

**효과:** 약 16% 느려진 이동 속도로 게임플레이 안정성 향상.

---

## 5. UI 설정 정리

### 5-1. 시스템 글꼴 설정 제거
**파일:** `core/.../windows/WndSettings.java`

**변경:** 설정 > 인터페이스 탭에서 "시스템 글꼴 사용" 체크박스 제거.

**제거된 코드:**
- `CheckBox chkSystemFont` 필드 선언
- `chkSystemFont` 생성 및 이벤트 핸들러
- `layout()` 메서드의 `chkSystemFont` 배치 코드

---

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `teavm/webapp/index.html` | 모바일 오디오 이벤트 추가, pageshow 핸들러 추가 |
| `core/.../scenes/CellSelector.java` | Window 열림 시 Enter 키 처리 수정 |
| `core/.../sprites/CharSprite.java` | DEFAULT_MOVE_INTERVAL 0.0864f → 0.1f |
| `core/.../windows/WndSettings.java` | 시스템 글꼴 체크박스 제거 |

---

## 빌드 결과

- **HTML5 (Cloudflare Pages):** `teavm/build/dist/webapp/`
- **Android APK (Signed):** `android/build/outputs/apk/release/SacredPixelDungeon-release.apk`
