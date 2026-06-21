# 003. UI 개선 변경사항

**날짜**: 2026-03-09

## 개요

캔버스 전체화면 구현으로 검은 여백 제거, HTML5 게임 프리즈 방지, 캐릭터 선택 화면 가로/세로 통합 레이아웃, 소개 화면 1열 레이아웃 적용.

---

## 수정된 파일

| 파일 | 모듈 | 변경 내용 |
|------|------|-----------|
| `styles.css` | html/webapp | 캔버스 전체화면, 로딩바 크기 수정 |
| `Actor.java` | core | HTML5 게임 프리즈 방지 |
| `HeroSelectScene.java` | core | 가로/세로 통합 레이아웃, 위치 조정 |
| `AboutScene.java` | core | 소개 화면 1열 레이아웃 |

---

## 변경 사항

### 1. 캔버스 전체화면 — 검은 여백 제거

### 파일
- `html/webapp/styles.css`

### 문제
LibGDX GWT가 생성하는 래퍼 요소(table, td, div)가 뷰포트를 채우지 않아 우측/하단에 검은 여백 발생.

### 변경
```css
/* #embed-html 고정 배치 */
#embed-html {
    position: fixed;
    top: 0; left: 0;
    width: 100vw;
    height: 100dvh;
}

/* GWT 래퍼 테이블/셀만 100% 강제 (div 제외 — 프리로더 영향 방지) */
#embed-html table,
#embed-html td {
    width: 100% !important;
    height: 100% !important;
}

/* 캔버스를 뷰포트 전체에 고정 */
canvas {
    position: fixed !important;
    top: 0 !important; left: 0 !important;
    width: 100vw !important;
    height: 100dvh !important;
}
```

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| #embed-html | 미지정 | `position: fixed`, `100vw × 100dvh` |
| GWT 래퍼 | 미지정 | `table`, `td`만 100% 강제 |
| canvas | `width/height: 100%` | `position: fixed`, `100vw × 100dvh` |

- `100dvh`는 모바일 브라우저의 동적 뷰포트 높이를 반영 (주소창 유무 대응)
- `div`를 셀렉터에서 제외하여 프리로더 로딩바(`#preloader-bar-container`)에 `height: 100%`가 적용되지 않도록 수정

---

## 2. HTML5 게임 프리즈 방지

### 파일
- `core/.../actors/Actor.java`

### 문제
`CharSprite.isMoving`이 `true`로 고착되면(PosTweener가 완료 전에 kill되거나 parent가 제거되는 경우), HTML5에서 `Actor.process()`가 매 프레임 break → 모든 액터 행동 불가 → 게임 프리즈.

- 데스크탑: 별도 Actor 스레드에서 `waitOnObject(sprite)`로 대기하므로 다르게 표출
- HTML5: 싱글스레드이므로 `isMoving == true`면 즉시 break → 재시도 반복

### 변경
```java
// 새로 추가된 필드
private static int movingStallFrames = 0;
private static final int MAX_MOVING_STALL_FRAMES = 60; // ~1초 at 60fps

// isMoving 체크 로직 (HTML5 분기)
} else {
    if (((Char) acting).sprite.isMoving) {
        movingStallFrames++;
        if (movingStallFrames > MAX_MOVING_STALL_FRAMES) {
            // 스프라이트 이동 고착 시 강제 해제
            ((Char) acting).sprite.isMoving = false;
            movingStallFrames = 0;
        } else {
            current = null;
            break;
        }
    } else {
        movingStallFrames = 0;
    }
}
```

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| isMoving 고착 시 | 무한 break-retry (프리즈) | 60프레임(~1초) 후 `isMoving = false` 강제 해제 |
| 정상 이동 시 | 매 프레임 break-retry | 동일 (변경 없음) |

---

## 3. 캐릭터 선택 화면 통합 레이아웃

### 파일
- `core/.../scenes/HeroSelectScene.java`

### 문제
1. 가로(landscape) 모드에서 좌측 패널 + 영웅이름/설명이 표시되는 별도 레이아웃 사용 → 세로 모드와 불일치
2. 세로 모드에서 영웅 아이콘 버튼이 화면 하단에 잘려서 표시
3. 타이틀/직업선택/설정/정보 버튼이 영웅 아이콘과 겹침

### 변경

**가로/세로 통합:**
- landscape 전용 레이아웃 분기 전체 제거 (heroName, heroDesc, btnFade 생성 코드 삭제)
- 가로/세로 모두 하단 영웅 아이콘 + 상단 배경아트의 세로형 레이아웃 사용
- WndHeroInfo의 landscape offset 제거

**위치 조정 (원본 대비):**

| 요소 | 원본 오프셋 | 변경 후 오프셋 | 이동량 |
|------|:-----------:|:--------------:|:------:|
| 영웅 아이콘 버튼 (curY) | `h - HEIGHT + 3` | `h - HEIGHT - 8` | 11px ↑ |
| "영웅을 선택하십시오" 타이틀 | `h - HEIGHT - height - 4` | `h - HEIGHT - height - 15` | 11px ↑ |
| 직업선택 박스 (startBtn) | `camH - HEIGHT + 2 - btnH` | `camH - HEIGHT - 14 - btnH` | 16px ↑ |
| 설정 (btnOptions) | `camH - HEIGHT - 16` | `camH - HEIGHT - 27` | 11px ↑ |
| 정보 (infoButton) | startBtn 기준 상대 배치 | 동일 (startBtn 따라 이동) | — |

**제거된 landscape 전용 요소:**
- `heroName` (RenderedTextBlock) — 영웅 이름 표시
- `heroDesc` (RenderedTextBlock) — 영웅 설명 표시
- `btnFade` (IconButton) — UI 숨기기 버튼
- landscape용 배경 위치 조정 로직 (`updateFade()` 내)

---

## 4. 소개 화면 (AboutScene) 1열 레이아웃

### 파일
- `core/.../scenes/AboutScene.java`

### 문제
Portrait/Landscape 모두에서 크레딧 블록이 `colWidth/2` 너비로 2개씩 한 줄에 배치 → 좁은 화면에서 겹침/가독성 저하.

### 변경
- 모든 CreditsBlock을 `colWidth` (135px) 전체 너비로 세로 스택 배치
- landscape/portrait 분기 완전 제거, 항상 1열 레이아웃
- 각 섹션 사이에 기존 `addLine()` 구분선 추가

**배치 구조 (변경 후):**
```
Sacred Pixel Dungeon          ← 전체 너비
────────────────────────────  ← 구분선
Sacred Pixel Dungeon       ← 전체 너비
  Splash Art: Aleksandar      ← 전체 너비 (기존: alex/celesti 나란히)
  Sound Effects: Celesti      ← 전체 너비
  Music: Kristjan             ← 전체 너비
────────────────────────────  ← 구분선
Pixel Dungeon                 ← 전체 너비
  Music: Cube Code            ← 전체 너비
────────────────────────────  ← 구분선
libGDX                        ← 전체 너비
  PD GDX: Edu García          ← 전체 너비 (기존: arcnor/purigro 나란히)
  GDX Help: Kevin MacMartin   ← 전체 너비
────────────────────────────  ← 구분선
Transifex (번역)              ← 전체 너비
────────────────────────────  ← 구분선
Freesound (사운드 라이선스)    ← 전체 너비
```

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| alex/celesti | `colWidth/2` 나란히 | `colWidth` 세로 스택 |
| kristjan | `colWidth/2` 중앙 | `colWidth` 세로 스택 |
| cube | `colWidth/2` 중앙 | `colWidth` 세로 스택 |
| arcnor/purigro | `colWidth/2` 나란히 | `colWidth` 세로 스택 |
| 구분선 | Sacred↔SHPX, Wata, GDX, Transifex 사이만 | 모든 섹션 사이 |
| landscape 분기 | 있음 (2열) | 제거 (항상 1열) |
