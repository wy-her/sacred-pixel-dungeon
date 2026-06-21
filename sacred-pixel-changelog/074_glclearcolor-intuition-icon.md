# 074. glClearColor 수정 및 UI 개선

**날짜**: 2026-05-27

## 개요

Scene 전환 시 발생하던 깨진 화면(쓰레기 픽셀) 문제 수정 및 UI 개선.

---

## 변경 사항

### 1. Game.java — glClearColor 설정 추가

**파일**: `SPD-classes/src/main/java/com/watabou/noosa/Game.java`

**문제**: Scene 전환 시 검은 화면에 무작위 흰색/회색 픽셀이 잠깐 나타나는 현상

**원인**:
- `glClear(GL_COLOR_BUFFER_BIT)` 호출 시 `glClearColor`가 설정되지 않음
- WebGL에서 초기화되지 않은 GPU 메모리 값이 화면에 표시됨

**해결**: 3곳에 `glClearColor(0, 0, 0, 1)` 추가

```java
// 1. create() - 앱 시작 시
@Override
public void create() {
    // Set clear color to black to prevent garbage pixels during scene transitions
    Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
    // ...
}

// 2. resize() - GL 컨텍스트 복원 시
if (versionContextRef != Gdx.graphics.getGLVersion()) {
    versionContextRef = Gdx.graphics.getGLVersion();
    Gdx.gl.glClearColor(0f, 0f, 0f, 1f); // Reset clear color after context restore
    // ...
}

// 3. render() - 렌더링 중 GL 컨텍스트 복원 시
if (versionContextRef != Gdx.graphics.getGLVersion()) {
    versionContextRef = Gdx.graphics.getGLVersion();
    Gdx.gl.glClearColor(0f, 0f, 0f, 1f); // Reset clear color after context restore
    // ...
}
```

---

### 2. StoneOfIntuition.java — 추측 버튼 아이콘 제거

**파일**: `core/.../items/stones/StoneOfIntuition.java`

**변경**: WndGuess 내 RedButton(guess)에서 아이콘 제거

```java
// 제거된 코드
guess.icon( new ItemSprite(item) );
```

이제 추측 버튼은 텍스트만 표시됨.

---

### 3. Android 앱 아이콘 크기 조정

**파일**: `capacitor-app/android/app/src/main/res/mipmap-*/ic_launcher.png`, `ic_launcher_round.png`

**변경**: 앱 아이콘 내 로고 비율을 100%에서 85%로 축소

- 로고가 캔버스의 85%를 차지 (15% 여백)
- 검은 배경 유지
- NearestNeighbor 보간법으로 픽셀아트 유지

---

## 테스트 케이스

### glClearColor
- [ ] 타이틀 화면 진입 시 깨진 화면 없음
- [ ] Scene 전환 시 (StartScene → InterlevelScene 등) 쓰레기 픽셀 없음
- [ ] 브라우저 탭 전환 후 복귀 시 깨진 화면 없음

### 감정의 돌
- [ ] WndGuess 열기 → 추측 버튼에 아이콘 없이 텍스트만 표시

### Android 앱 아이콘
- [ ] 앱 아이콘이 이전보다 약간 작은 로고로 표시됨

---

## 수정된 파일

| File | Changes |
|------|---------|
| `SPD-classes/.../Game.java` | 3곳에 `glClearColor(0,0,0,1)` 추가 |
| `core/.../items/stones/StoneOfIntuition.java` | WndGuess 추측 버튼 아이콘 제거 |
| `capacitor-app/.../mipmap-*/ic_launcher*.png` | 앱 아이콘 크기 85%로 조정 |

---
