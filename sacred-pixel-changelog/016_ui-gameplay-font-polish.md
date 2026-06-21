# 016. UI 폴리시, 게임플레이 수정, 폰트 개선, 빌드 시스템 수정

**날짜**: 2026-03-25

## 개요

조작키 설정 창 개선, 타이틀 화면 버튼 폭 통일, 게임 속도 조정, 다트 함정 VFX 블로커 시스템, 시스템 글꼴 토글 복원, 폰트 렌더링 개선, 브라우저 줌 차단, 빌드 시스템 수정 등 다수의 UI/게임플레이/폰트/빌드 개선사항 적용.

---

## 변경 사항

### 1. 조작키 설정 창 (WndKeyBindings)

### 1-1. 창 가로 길이 축소
**파일:** `core/.../windows/WndKeyBindings.java`

- `WIDTH = 168` → **164** (4px 축소)
- 열 비율/구분선은 WIDTH 기반 파생이므로 자동 조정

### 1-2. 헤더 및 키 이름 세로 정렬 보정
**파일:** `core/.../windows/WndKeyBindings.java`

2줄로 넘어가는 텍스트에 대해 RenderedTextBlock의 bottomPad 보정 적용:
- 헤더(ttlKey1/2/3): `nLines > 1`일 때 `Math.round(5 * 0.5f) / 2f` 패딩 보정
- BindingItem 키 이름(key1Name/2/3): 동일한 보정 적용
- 기존에 액션 이름(actionName)에만 적용되던 보정을 키 이름 열에도 확장

### 1-3. 버튼 폰트 크기 통일
**파일:** `core/.../windows/WndKeyBindings.java`

"기본 조작키", "확인", "취소", "해제" 버튼: size 9 → **7** (WndSettings TEXT_SIZE와 동일)

---

## 2. 타이틀 화면 (TitleScene)

### 2-1. 버튼 가로 폭 통일
**파일:** `core/.../scenes/TitleScene.java`

- 가로/세로 모드 분기 제거: `landscape() ? MIN_WIDTH_L-6 : MIN_WIDTH_P+6` → **`MIN_WIDTH_P+27`** (162px, 가로/세로 통일)
- Fireball 확장 로직 제거: `(buttonAreaWidth + fireballTotalWidth) / 2f` 삭제 → 버튼 폭 고정

---

## 3. 게임 속도 (timeScale)

### 3-1. Game.timeScale 조정 및 원복
**파일:** `SPD-classes/.../Game.java`

- `timeScale = 1f` → `0.85f` → `0.92f` → `0.95f` → **`1.0f`** (원복)
- 시도한 값들 중 원본 속도(1.0f)가 가장 적합한 것으로 판단
- line 73(초기값), line 310, line 319(씬 전환 리셋) 모두 동일 값

---

## 4. 층간 화면 (InterlevelScene)

### 4-1. 배경 패닝 효과 제거
**파일:** `core/.../scenes/InterlevelScene.java`

세로 모드에서 배경 이미지가 좌우로 패닝되는 효과 전체 삭제 (15줄).
배경은 정지 상태로 유지.

---

## 5. 다트 함정 — 데미지 착탄 시 적용 + VFX 블로커

### 5-1. 글로벌 VFX 블로커 시스템
**파일:** `core/.../actors/Actor.java`

- `vfxBlockers` 정적 카운터 추가: `addVfxBlocker()` / `removeVfxBlocker()`
- `process()` 시작 시 `vfxBlockers > 0`이면 모든 액터 처리 일시정지
- `clear()`에서 카운터 리셋

### 5-2. WornDartTrap — 데미지를 미사일 착탄 콜백으로 이동
**파일:** `core/.../levels/traps/WornDartTrap.java`

**변경 전:** 데미지 동기 적용 → 미사일 애니메이션 fire-and-forget (빈 콜백)
**변경 후:**
- FOV 내: `addVfxBlocker()` → MissileSprite 발사 → 콜백에서 데미지 적용 + `removeVfxBlocker()`
- FOV 밖: 즉시 데미지 적용 (시각 효과 없음)

### 5-3. PoisonDartTrap — 동일 패턴 적용
**파일:** `core/.../levels/traps/PoisonDartTrap.java`

WornDartTrap과 동일한 VFX 블로커 + 착탄 콜백 데미지 패턴. Poison 버프도 콜백 내에서 적용.

### 5-4. GrimTrap — 동일 패턴 적용
**파일:** `core/.../levels/traps/GrimTrap.java`

MagicMissile 사용. VFX 블로커 + 착탄 콜백 데미지 패턴.

---

## 6. 시스템 글꼴 토글 복원

### 6-1. WndSettings UITab — 체크박스 재추가
**파일:** `core/.../windows/WndSettings.java`

- `CheckBox chkSystemFont` 필드 + `ColorBlock sep2` 구분선 추가
- `createChildren()`에서 체크박스 생성 (btnToolbarSettings 앞)
- `layout()`에서 배치
- 번역 키 `system_font`은 22개 언어 모두 기존재

### 6-2. TeaVMPlatformSupport — systemfont 플래그 분기
**파일:** `teavm/.../TeaVMPlatformSupport.java`

```java
if (systemfont) {
    // "systemfont" 포함 파일명 → FreeTypeFontGenerator에서 system-ui 폰트 선택
    new FreeTypeFontGenerator(Gdx.files.internal("fonts/neodunggeunmo_systemfont.woff2"));
} else {
    new FreeTypeFontGenerator(Gdx.files.internal("fonts/NeoDunggeunmoPro-Regular.woff2"));
}
```

### 6-3. FreeTypeFontGenerator (TeaVM) — fontFamily 분기
**파일:** `teavm/.../freetype/FreeTypeFontGenerator.java`

- `useSystemFont` 필드 추가
- 생성자에서 파일명 `"systemfont"` 포함 시:
  - `fontFamily = "system-ui, -apple-system, 'Segoe UI', Roboto, 'Noto Sans', sans-serif"`
  - `useSystemFont = true`
- 미포함 시: `fontFamily = "'NeoDunggeunmoPro', 'Noto Sans', sans-serif"` (기본)
- `skipBinarization` 파라미터: `isSystemFont` (fontFamily에 "system-ui" 포함 여부)로 전달

---

## 7. 설정 버튼 텍스트 크기 축소

### 7-1. WndSettings TEXT_SIZE 상수 추가
**파일:** `core/.../windows/WndSettings.java`

`private static final int TEXT_SIZE = 7;` (기본 9에서 2pt 축소)

### 7-2. 전체 탭 버튼/체크박스 적용 (언어 설정 제외)
**파일:** `core/.../windows/WndSettings.java`

| 컴포넌트 | 변경 전 | 변경 후 |
|----------|---------|---------|
| CheckBox (전체) | 기본 size 9 | TEXT_SIZE (7) |
| RedButton (toolbar, split, group, center, key/controller bindings) | 기본 size 9 | TEXT_SIZE (7) |
| 언어 설정 버튼 | size 6 | 변경 없음 |

### 7-3. CheckBox size 파라미터 생성자 추가
**파일:** `core/.../ui/CheckBox.java`

```java
public CheckBox( String label, int size ) {
    super( label, size );
    icon( Icons.get( Icons.UNCHECKED ) );
}
```

---

## 8. 인터페이스 확대 슬라이더

### 8-1. maxDefaultZoom -1 적용
**파일:** `core/.../scenes/PixelScene.java`

```java
// Before:
maxDefaultZoom = (int)Math.min(w/minWidth, h/minHeight);
// After:
maxDefaultZoom = (int)Math.min(w/minWidth, h/minHeight) - 1;
maxDefaultZoom = Math.max(2, maxDefaultZoom);
```

과도한 확대 방지. 경계 케이스에서 슬라이더 숨김.

### 8-2. 기본값 2x 보장
**파일:** `core/.../windows/WndSettings.java`

```java
optUIScale.setSelectedValue(SPDSettings.scale() == 0 ? 2 : PixelScene.defaultZoom);
```

---

## 9. Pretendard 폰트 제거 및 NeoDunggeunmoPro 이동

### 9-1. Pretendard 파일 삭제
- `core/src/main/assets/fonts/pretendard.otf` — 삭제
- `desktop/src/main/assets/fonts/pretendard.otf` — 삭제
- `teavm/bin/main/fonts/pretendard.otf` — 삭제

### 9-2. NeoDunggeunmoPro를 core/assets/fonts/에 복사
- `teavm/webapp/fonts/NeoDunggeunmoPro-Regular.woff2` → `core/src/main/assets/fonts/` 복사

### 9-3. 전 플랫폼 소스코드 참조 변경
| 파일 | 변경 전 | 변경 후 |
|------|---------|---------|
| DesktopPlatformSupport.java | `"fonts/pretendard.otf"` | `"fonts/NeoDunggeunmoPro-Regular.woff2"` |
| AndroidPlatformSupport.java | `"fonts/pretendard.otf"` | `"fonts/NeoDunggeunmoPro-Regular.woff2"` |
| HtmlPlatformSupport.java (GWT) | `"fonts/pretendard.otf"` | `"fonts/NeoDunggeunmoPro-Regular.woff2"` |
| IOSPlatformSupport.java | `"fonts/pretendard.otf"` | `"fonts/NeoDunggeunmoPro-Regular.woff2"` |
| TeaVMPlatformSupport.java | `"fonts/pretendard.otf"` / `"fonts/pretendard_systemfont.otf"` | `"fonts/NeoDunggeunmoPro-Regular.woff2"` / `"fonts/neodunggeunmo_systemfont.woff2"` |

---

## 10. 폰트 렌더링 개선

### 10-1. 폰트 테두리 — HTML5 전용 borderWidth 비활성화 후 재활성화
**파일:** `SPD-classes/.../PlatformSupport.java`

- 최종: `if (border)` (모든 플랫폼에서 테두리 활성화)
- `borderWidth = parameters.size / 10f` (원본 값)

### 10-2. Alpha binarization 임계값 조정
**파일:** `teavm/.../freetype/FreeTypeFontGenerator.java`

- 임계값: `128` → **`64`** (테두리 픽셀 더 많이 보존)
- `lineWidth = Math.max(1, Math.round(borderWidth * 2))` (정수 반올림, Canvas2D 서브픽셀 AA 최소화)
- 시스템 폰트(`skipBinarization=true`)일 때는 binarization 건너뜀

---

## 11. 브라우저 줌 차단

### 11-1. JavaScript 이벤트 차단
**파일:** `teavm/webapp/index.html`

- `wheel` + `ctrlKey`: `e.preventDefault()` (Ctrl+스크롤 줌 차단)
- `gesturestart`/`gesturechange`/`gestureend`: `e.preventDefault()` (Safari 핀치 줌 차단)
- `touchend` 더블탭 줌 차단: 300ms 내 연속 터치 방지

### 11-2. CSS touch-action
**파일:** `teavm/webapp/index.html`

`#canvas` 스타일에 `touch-action: manipulation;` 추가

---

## 12. 빌드 시스템 수정

### 12-1. runDebug/runRelease에 커스텀 webapp 파일 복사 추가
**파일:** `teavm/build.gradle`

**문제:** `runDebug`/`runRelease` 태스크에 `doLast` 블록이 없어서, TeaVM 컴파일러가 생성한 기본 index.html이 커스텀 index.html을 덮어씀.

**수정:** 두 태스크에 `doFirst` 블록 추가:
- `webapp/index.html` → 빌드 출력 복사
- `webapp/styles.css` → 빌드 출력 복사
- `gwt/webapp/fonts/` → 빌드 출력 복사
- `gwt/webapp/banner.png` → 빌드 출력 복사
- `preload.txt` 음악 프리로드 비활성화

**참고:** `doFirst`는 TeaVM 컴파일보다 먼저 실행되므로, `runDebug`/`runRelease` 사용 시 TeaVM이 다시 index.html을 덮어씀. **해결:** `buildDebug` 실행 후 `python -m http.server 8080`으로 서빙하는 방식이 안정적.

---

## 13. CharSprite — finishMotion() 메서드 추가

### 13-1. public 메서드 추가
**파일:** `core/.../sprites/CharSprite.java`

```java
public void finishMotion() {
    if (motion != null) {
        motion.killAndErase();
        motion = null;
    }
    isMoving = false;
}
```

진행 중인 이동 애니메이션을 즉시 완료하는 유틸리티 메서드. Actor.java 등 외부에서 호출 가능.

---

## 미해결 이슈

### 영웅 연속이동 시 적 공격 애니메이션 일괄 처리
- **증상:** 영웅이 연속으로 여러 타일을 이동하는 동안, 적이 영웅을 공격할 때 수 턴치 공격이 한번에 발생. 개별 공격 애니메이션이 표시되지 않음.
- **원인:** HTML5 싱글스레드에서 Actor.process()가 매 프레임 동기 호출. 적의 attack 애니메이션은 `isMoving` 체크에 걸리지 않음.
- **시도한 접근법:** 글로벌 isMoving return, per-actor finishMotion(), per-actor break+stall, Mob-only break — 모두 부작용 발생
- **현재 상태:** 원래의 stall 기반 방식으로 복원 (Changelog 10 수준)
- **향후 계획:** 별도 세션에서 HTML5 싱글스레드 환경에 맞는 근본적 아키텍처 설계로 접근

---

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `core/.../windows/WndKeyBindings.java` | WIDTH 164, 세로 정렬 보정, 버튼 폰트 7pt |
| `core/.../scenes/TitleScene.java` | 버튼 폭 MIN_WIDTH_P+27, 가로/세로 통일, fireball 확장 제거 |
| `SPD-classes/.../Game.java` | timeScale 1.0f |
| `core/.../scenes/InterlevelScene.java` | 배경 패닝 삭제 |
| `core/.../actors/Actor.java` | vfxBlockers 시스템, stall 기반 isMoving 복원 |
| `core/.../levels/traps/WornDartTrap.java` | VFX 블로커 + 착탄 콜백 데미지 |
| `core/.../levels/traps/PoisonDartTrap.java` | VFX 블로커 + 착탄 콜백 데미지 |
| `core/.../levels/traps/GrimTrap.java` | VFX 블로커 + 착탄 콜백 데미지 |
| `core/.../windows/WndSettings.java` | 시스템 폰트 체크박스, TEXT_SIZE 7, 버튼 크기 통일, maxDefaultZoom 기본값 |
| `core/.../ui/CheckBox.java` | size 파라미터 생성자 추가 |
| `core/.../scenes/PixelScene.java` | maxDefaultZoom -1 |
| `teavm/.../TeaVMPlatformSupport.java` | systemfont 플래그 분기 |
| `teavm/.../freetype/FreeTypeFontGenerator.java` | fontFamily 분기, binarization 임계값 64, 정수 lineWidth |
| `SPD-classes/.../PlatformSupport.java` | borderWidth 전 플랫폼 활성화 |
| `core/.../sprites/CharSprite.java` | finishMotion() 메서드 추가 |
| `teavm/webapp/index.html` | 브라우저 줌 차단, touch-action |
| `teavm/build.gradle` | runDebug/runRelease doFirst 블록 추가 |
| `desktop/.../DesktopPlatformSupport.java` | pretendard → NeoDunggeunmoPro 참조 |
| `android/.../AndroidPlatformSupport.java` | pretendard → NeoDunggeunmoPro 참조 |
| `gwt/.../HtmlPlatformSupport.java` | pretendard → NeoDunggeunmoPro 참조 |
| `ios/.../IOSPlatformSupport.java` | pretendard → NeoDunggeunmoPro 참조 |
| `core/src/main/assets/fonts/pretendard.otf` | 삭제 |
| `desktop/src/main/assets/fonts/pretendard.otf` | 삭제 |
| `core/src/main/assets/fonts/NeoDunggeunmoPro-Regular.woff2` | 신규 (teavm/webapp/fonts/에서 복사) |
