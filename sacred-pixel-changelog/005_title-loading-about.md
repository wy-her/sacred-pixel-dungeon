# 005. 타이틀·로딩·소개 화면 개선

**날짜**: 2026-03-12

## 개요

타이틀 화면(배경 변경, 글로우 애니메이션 제거), 로딩 화면(로고 크기 통일), 소개 화면(텍스트 폭 축소, GitHub 링크 추가), Fireball 효과(픽셀아트 필터) 개선.

---

## 수정된 파일

| 파일 | 모듈 | 변경 내용 |
|------|------|-----------|
| `TitleScene.java` | core | 배경 Archs 적용, 글로우 애니메이션 제거, 디버그 로그 추가 |
| `AboutScene.java` | core | 텍스트 상자 폭 축소, 들여쓰기 추가, GitHub 링크 추가 |
| `Fireball.java` | core | GL_NEAREST 텍스처 필터 적용 (픽셀아트 느낌) |
| `banners.png` | core/assets/interfaces | TITLE_PORT 영역을 TITLE_GLOW_PORT 40% 합성으로 교체 |
| `HtmlLauncher.java` | html | 프리로더 로고 크기 계산을 PixelScene과 동일하게 수정 |
| `index.html` | html/webapp | JS 로고 크기 계산 제거 (HtmlLauncher로 이관) |
| `styles.css` | html/webapp | 프리로더 스타일 추가 (프로그레스 바, pixelated 렌더링) |
| `banner.png` | html/webapp | 타이틀 화면과 동일한 합성 로고 이미지로 교체 |

---

## 변경 사항

### 1. 타이틀 화면 (TitleScene.java)

### 배경 변경
- **변경 전**: 검은색 단색 (ColorBlock)
- **변경 후**: Archs 패턴 배경

### 글로우 애니메이션 제거
- TITLE_GLOW_PORT를 사용한 밝기 변동 애니메이션 완전 제거
- 대신 banners.png의 TITLE_PORT 영역 자체를 TITLE_GLOW_PORT 40% 합성본으로 교체하여 일정한 두께감 유지

### Fireball 유지
- 위치·크기·색상 변경 없음
- GL_NEAREST 필터만 추가 (Fireball.java에서 적용)

### 디버그 로그 추가
- `DeviceCompat.log()`로 Game.width, Game.height, density, defaultZoom, 로고 크기 출력
- 브라우저 콘솔에서 `TitleScene` 태그로 확인 가능

---

## 2. 로딩 화면 (HtmlLauncher.java)

### 로고 이미지 통일
- `html/webapp/banner.png`를 타이틀 화면과 동일한 합성 이미지(84×52px)로 교체
- CSS `image-rendering: pixelated` 적용

### 로고 크기 계산 수정
- **변경 전**: `devicePixelRatio`를 density로 사용 → 타이틀 화면과 크기 불일치
- **변경 후**: PixelScene의 zoom 계산을 정확히 복제

핵심 차이점:
- GWT에서 `Game.density` = **0.625** (100/160, GWT fallback 값). `devicePixelRatio`가 아님
- `usePhysicalPixels=true`이므로 `Game.width/height` = 물리 픽셀 (CSS 픽셀 × DPR)
- 최종 `<img>` 크기는 CSS 픽셀로 변환: `logoW_css = 93.1 * zoom / dpr`

```java
// HtmlLauncher.computeLogoSize() 핵심 로직
float gameW = sw * dpr;  // 물리 픽셀
float gameH = sh * dpr;
float density = 100f / 160f;  // 0.625 (GWT 고정값)
int maxZoom = Math.max(2, (int)Math.min(gameW / minW, gameH / minH));
int zoom = Math.min(Math.max(2, (int)Math.ceil(density * scaleFactor)), maxZoom);
int logoW = Math.round(93.1f * zoom / dpr);  // CSS 픽셀
```

---

## 3. 소개 화면 (AboutScene.java)

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| 텍스트 상자 가로 폭 | colWidth = 200 | colWidth = 180 (10% 축소) |
| 텍스트 정렬 | 좌측 정렬 | non-large 항목에 6px 들여쓰기 추가 |
| GitHub 링크 | 없음 | Sacred PD 설명 아래 dummy.github.com 링크 추가 |

---

## 4. Fireball 효과 (Fireball.java)

- 텍스처 필터를 기본(Linear)에서 `GL_NEAREST`로 변경
- 확대 시 픽셀아트 느낌 유지
- fireball-tall.png 사용 (변경 없음)

```java
texture( "effects/fireball-tall.png" );
texture.filter( GL20.GL_NEAREST, GL20.GL_NEAREST );
```

---

## 5. 에셋 변경

### banners.png (core/assets/interfaces)
- TITLE_PORT 영역(0,0,84,52)을 TITLE_PORT + TITLE_GLOW_PORT 40% 합성본으로 교체
- 글씨가 약간 더 두꺼워진 효과

### banner.png (html/webapp)
- 위와 동일한 합성 이미지 (84×52px)
- 로딩 화면과 타이틀 화면에서 동일한 로고 표시
