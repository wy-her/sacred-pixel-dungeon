# 001. 폰트 시스템 분석

**날짜**: 2026-03-07

## 개요

HTML5 포팅된 SPD는 **두 가지 독립적인 폰트 렌더링 시스템**을 사용한다. 각각의 작동 방식과 폰트 변경 방법이 완전히 다르다.

---

## 1. BitmapText (픽셀 폰트)

| 항목 | 내용 |
|------|------|
| 에셋 | `core/src/main/assets/fonts/pixel_font.png` (1024×8px) |
| 지원 문자 | Latin ASCII 95자 (`BitmapText.Font.LATIN_FULL`) |
| 사용처 | 메뉴, 버튼, 데미지 숫자, 통화 표시, HUD 라벨 등 (~38곳) |
| 폰트 파일 의존성 | **없음** — .otf/.ttf와 완전히 무관 |

### 작동 방식

1. `PixelScene.create()` 에서 `pixel_font.png`를 `TextureCache`로 로드
2. `Font.colorMarked(texture, 0x00000000, LATIN_FULL)` 호출
3. 내부의 `splitBy()` 알고리즘이 PNG를 수평 스캔하여 **투명(alpha=0) 열**을 구분자로 각 글자의 경계(UV 좌표)를 자동 감지
4. 렌더링 시 해당 UV 좌표로 텍스처에서 글자를 잘라 OpenGL 쿼드에 매핑

```java
// PixelScene.java:154
pixelFont = Font.colorMarked(
    TextureCache.get(Assets.Fonts.PIXELFONT), 0x00000000, BitmapText.Font.LATIN_FULL);
pixelFont.baseLine = 6;
pixelFont.tracking = -1;
```

### 폰트 변경 방법

`pixel_font.png` 스프라이트 시트 자체를 교체해야 한다. 교체 시 다음 포맷을 준수해야 함:
- 각 글자 사이에 **투명(alpha=0) 열**이 최소 1px 존재
- 글자 순서: `LATIN_FULL` 문자열 순서 (공백부터 `~`까지)
- `colorNotMatch()` 메서드가 alpha=0 픽셀을 구분자로 인식

---

## 2. RenderedText (FreeType/Canvas2D 폰트)

| 항목 | 내용 |
|------|------|
| 에셋 | `core/src/main/assets/fonts/pretendard.otf` (320KB) |
| 지원 문자 | 전체 유니코드 (한글, CJK 포함) |
| 사용처 | 아이템 설명, 대화, 긴 텍스트, UI 윈도우 등 (~169곳) |
| 폰트 파일 의존성 | 플랫폼별로 다름 (아래 참조) |

### 플랫폼별 렌더링 방식

| 플랫폼 | 방식 |
|--------|------|
| Desktop/Android/iOS | FreeType 라이브러리로 .otf 파일을 직접 로드하여 글리프 래스터화 |
| **HTML5** | **Canvas2D** `ctx.fillText()`로 렌더링 — .otf 파일을 로드하지 않고 CSS font-family 사용 |

### HTML5 RenderedText 파이프라인

```
HtmlPlatformSupport.setupFontGenerators()
  → new FreeTypeFontGenerator(Gdx.files.internal("fonts/pretendard.otf"))
  → [super-source] FreeTypeFontGenerator 생성자
      → 파일명에 "pixel" 포함 여부만 검사
      → fontFamily CSS 문자열 결정 (실제 .otf 파일은 로드 안 함)
  → generateFont() 호출 시 CanvasFontData 생성
  → Canvas2D ctx.font = "18px 'PretendardStd', 'Noto Sans', sans-serif"
  → ctx.fillText()로 글리프를 Pixmap(텍스처 아틀라스)에 렌더링
```

**핵심**: HTML5에서 `pretendard.otf` 파일은 GWT 에셋으로 복사만 되고 **실제로 사용되지 않는다**. 폰트 선택은 CSS font-family 문자열로만 결정된다.

---

## 웹폰트 적용 절차 (RenderedText)

HTML5에서 커스텀 폰트를 RenderedText에 적용하려면 다음 5가지가 모두 필요하다:

### 1단계: 폰트 파일 배치
```
html/webapp/fonts/MyFont.otf
```

### 2단계: @font-face 선언 (`html/webapp/styles.css`)
```css
@font-face {
    font-family: 'MyFont';
    src: url('fonts/MyFont.otf') format('opentype');
    font-weight: normal;
    font-style: normal;
    font-display: block;
}
```

### 3단계: FontFace API 프리로드 + GWT 스크립트 지연 로드 (`html/webapp/index.html`)
```html
<script>
(function() {
    function startGame() {
        var s = document.createElement('script');
        s.src = 'html/html.nocache.js';
        document.body.appendChild(s);
    }
    var font = new FontFace('MyFont', 'url(fonts/MyFont.otf)');
    font.load().then(function(loaded) {
        document.fonts.add(loaded);
        startGame();
    }).catch(function(err) {
        console.error('Font load failed:', err);
        startGame();
    });
})();
</script>
```
> Canvas2D는 웹폰트 로딩을 기다리지 않으므로, **폰트 로드 완료 후 GWT 스크립트를 로드**해야 한다.

### 4단계: super-source fontFamily 변경
```
html/src/main/java/.../super/com/badlogic/gdx/graphics/g2d/freetype/FreeTypeFontGenerator.java
```
```java
fontFamily = "'MyFont', 'Noto Sans', sans-serif";
```

### 5단계: GWT 클린 리빌드
```bash
rm -rf html/build
./gradlew html:dist
```
> super-source 변경을 Gradle이 감지하지 못할 수 있으므로 `html/build` 삭제 후 재빌드 권장

---

## 현재 적용 상태 (PretendardStd-Thin)

| 변경 파일 | 내용 |
|-----------|------|
| `html/webapp/fonts/PretendardStd-Thin.otf` | 폰트 파일 |
| `html/webapp/styles.css` | `@font-face` 선언 |
| `html/webapp/index.html` | FontFace API 프리로드 + GWT 지연 로드 |
| `FreeTypeFontGenerator.java` (super-source) | fontFamily에 `'PretendardStd'` 추가 |

- **RenderedText**: PretendardStd 적용됨 ✅
- **BitmapText**: 미적용 ❌ (pixel_font.png 기반이라 별도 작업 필요)

> 참고: PretendardStd-Thin은 기본 sans-serif와 시각적으로 매우 유사하다. serif 폰트로 테스트하여 파이프라인 정상 동작을 확인함.

---

## BitmapText 폰트 변경 시도 기록

### 시도한 접근: 런타임 Pixmap 생성

`PlatformSupport.generatePixelFontPixmap()` 메서드를 추가하고, `HtmlPlatformSupport`에서 JSNI Canvas2D로 PretendardStd 글리프를 Pixmap에 렌더링한 뒤 `PixelScene`에서 `colorMarked()`에 전달하는 방식.

### 실패 원인

- 게임 로딩 99%에서 멈춤 (PixelScene.create() 초기화 실패 추정)
- Canvas2D 안티앨리어싱이 투명 구분 열에 번져 `splitBy()` 알고리즘이 글자 경계를 찾지 못하는 것으로 추정
- 임시 캔버스 분리 렌더링 + 3px 구분 열 확대로도 해결 안 됨

### 대안 접근법 (미시도)

1. **오프라인 PNG 생성**: Python/Pillow로 PretendardStd 기반 `pixel_font.png`를 생성하여 에셋 교체
2. **BitmapText → RenderedText 전환**: ~38곳의 BitmapText 사용 코드를 RenderedText로 변경 (매우 침습적)
3. **빌드 태스크**: HTML5 빌드 시 copyAssets 이후 `pixel_font.png`를 덮어쓰는 Gradle 태스크 추가

---

## 핵심 파일 경로

| 파일 | 역할 |
|------|------|
| `core/src/main/assets/fonts/pixel_font.png` | BitmapText 스프라이트 시트 |
| `core/src/main/assets/fonts/pretendard.otf` | RenderedText 폰트 (Desktop/Android에서 사용) |
| `html/webapp/fonts/PretendardStd-Thin.otf` | HTML5 웹폰트 |
| `html/webapp/styles.css` | @font-face 선언 |
| `html/webapp/index.html` | FontFace 프리로드 + GWT 로드 |
| `html/.../super/.../FreeTypeFontGenerator.java` | HTML5 Canvas2D 폰트 렌더링 (super-source) |
| `html/.../HtmlPlatformSupport.java` | HTML5 플랫폼 지원 (폰트 생성기 설정) |
| `core/.../scenes/PixelScene.java` | 두 폰트 시스템 초기화 |
| `SPD-classes/.../noosa/BitmapText.java` | BitmapText 클래스 + Font.colorMarked/splitBy |
| `SPD-classes/.../utils/PlatformSupport.java` | 폰트 생성기 추상 클래스 |

---

## 주의사항

- `SPDSettings.systemFont()` 설정은 **RenderedText에만** 영향을 준다. BitmapText는 항상 `pixel_font.png` 사용
- HTML5에서 `pretendard.otf` 에셋 파일을 교체해도 효과 없음 — super-source가 파일명만 검사하기 때문
- 한글/CJK 텍스트는 항상 RenderedText로 렌더링됨 (BitmapText는 ASCII만 지원)
