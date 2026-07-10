# 116. INTROS 문서 레이스 컨디션 수정

**날짜**: 2026-07-09

## 개요

스토리 화면(InterlevelScene) 표시 중 앱이 종료되면 INTROS 문서가 저장되지 않는 버그 수정. `readPage()` 호출 시점을 버튼 클릭에서 스토리 생성 시점으로 변경.

---

## 증상

- 특정 층(1, 6, 11, 16, 21층) 진입 시 스토리가 표시됨
- 스토리 화면에서 "Continue" 버튼을 누르기 전에 앱이 종료되면
- 다음 접속 시 해당 층의 스토리 문서(INTROS)가 저널에 등록되지 않음
- 예: Halls 스토리를 보다가 앱 종료 → 저널에 Halls 문서 없음

---

## 문제 분석

### 원인: 레이스 컨디션

```
Timeline (버그 상황):

Turn T: 21층 진입
├─ InterlevelScene.update() 실행
│  └─ showStory() 호출
│     └─ Statistics.deepestFloor = 21 업데이트 ✓
│     └─ createStoryElementsInternal() 호출
│        └─ 스토리 UI 생성 (버튼, 텍스트)
│        └─ readPage()는 btnContinue.onClick()에서만 호출 예정
│
├─ 스토리 표시 중... (사용자가 읽는 중)
│
└─ 앱 강제 종료 (광고 에러, 시스템 종료 등)
   └─ Document.INTROS 상태: FOUND (READ 아님) ❌
   └─ deepestFloor는 이미 21 → 다음 접속 시 스토리 재표시 조건 불충족
```

### 스토리 표시 조건

`InterlevelScene.java`에서 스토리 표시 조건:
```java
if (Statistics.deepestFloor < loadingDepth) {
    showStory();  // deepestFloor 업데이트 + 스토리 표시
}
```

- `deepestFloor`가 먼저 업데이트되어 스토리 재표시 불가
- `readPage()`가 호출되지 않아 문서 미등록

---

## 변경 사항

### Bug Fix

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/scenes/InterlevelScene.java`

**수정**: `readPage()` 호출 시점을 스토리 생성 시점으로 이동

```java
// 기존 코드 (버그)
private void createStoryElementsInternal(String storyText, int pageIdx) {
    // 스토리 UI 생성
    RenderedTextBlock text = PixelScene.renderTextBlock(storyText, 6);
    // ...

    btnContinue = new RedButton("Continue") {
        @Override
        protected void onClick() {
            Document.INTROS.readPage(pageIdx);  // ← 버튼 클릭 시에만 호출
            fadeOutStory();
        }
    };
    // ...
}

// 수정 코드
private void createStoryElementsInternal(String storyText, int pageIdx) {
    // Mark story as read immediately when displayed, to prevent loss if app closes before button click
    Document.INTROS.readPage(pageIdx);  // ← 스토리 생성 즉시 호출

    // 스토리 UI 생성
    RenderedTextBlock text = PixelScene.renderTextBlock(storyText, 6);
    // ...

    btnContinue = new RedButton("Continue") {
        @Override
        protected void onClick() {
            // readPage() 제거 (이미 호출됨)
            fadeOutStory();
        }
    };
    // ...
}
```

### 키보드 리스너 중복 호출 제거

```java
// 기존: keyListener 내 readPage() 중복 호출
Game.platform.setOnscreenKeyboardVisible(false, new Runnable() {
    @Override
    public void run() {
        Document.INTROS.readPage(pageIdx);  // ← 중복 제거
        fadeOutStory();
    }
});

// 수정: readPage() 제거 (createStoryElementsInternal 시작에서 이미 호출)
Game.platform.setOnscreenKeyboardVisible(false, new Runnable() {
    @Override
    public void run() {
        fadeOutStory();
    }
});
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `scenes/InterlevelScene.java:773-822` | `readPage()` 호출 시점 변경 (스토리 생성 즉시) |

---

## 수정 효과

| 항목 | Before | After |
|------|--------|-------|
| `readPage()` 호출 시점 | 버튼 클릭/키 입력 시 | 스토리 표시 즉시 |
| 앱 강제 종료 시 | 문서 미등록 (FOUND) | 문서 등록 (READ) |
| 문서 상태 | 버튼 클릭 필요 | 스토리 표시만으로 충분 |

---

## 관련 동작

### 다른 Document들과의 차이

| Document 타입 | READ 전환 시점 | 비고 |
|---------------|----------------|------|
| INTROS | 스토리 표시 시 (수정 후) | 자동 표시되는 스토리 |
| ALCHEMY_GUIDE | WndStory 닫을 때 | 사용자가 열어본 가이드 |
| ADVENTURERS_GUIDE | 읽기 행위 시 | 아이템 조사 등 |

INTROS는 자동으로 표시되므로 "본 것 = 읽은 것"으로 처리하는 것이 합리적.

---
