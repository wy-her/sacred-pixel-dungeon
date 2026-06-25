# 107. 튜토리얼 인트로 스토리 추가

**날짜**: 2026-06-25

## 개요

튜토리얼 진입 시 인터레벨 씬에 게임 소개 및 로그라이크 장르 특징을 설명하는 인트로 스토리를 추가.

---

## 변경 사항

### Features (1)

---

### [F-1] 튜토리얼 인트로 스토리 추가

**배경**: 기존에는 튜토리얼 진입 시 스토리 없이 바로 게임이 시작되었음. 신규 플레이어에게 게임의 특징을 미리 안내할 필요가 있음.

**구현 내용**:

1. **Document.java** - INTROS에 "Tutorial" 페이지 추가
2. **journal.properties** - 영문 스토리 텍스트 추가
3. **journal_ko.properties** - 한국어 스토리 텍스트 추가
4. **InterlevelScene.java** - 튜토리얼 진입 시 스토리 표시 로직 추가

**스토리 내용 (한국어)**:
```
세이크리드 픽셀 던전에 오신 것을 환영합니다!

이 게임은 로그라이크 장르로, 죽으면 처음부터 다시 시작합니다.
레벨, 아이템, 적은 매번 새롭게 생성됩니다.

실패를 두려워하지 마세요 - 매 시도에서 배우는 것이 핵심입니다.
신중하게 탐험하고 던전의 모든 상황에 적응하세요.

이 튜토리얼에서 기본을 배웁니다. 행운을 빕니다!
```

**스토리 내용 (영문)**:
```
Welcome to Sacred Pixel Dungeon!

This is a roguelike game where death is permanent and every run is unique.
Levels, items, and enemies are randomly generated.

Don't fear failure - learning from each attempt is key.
Explore carefully and adapt to what the dungeon throws at you.

This tutorial covers the basics. Good luck!
```

**지원 언어**: 23개 (en, ko, de, es, fr, it, ja, zh, zh-hant, pt, ru, pl, nl, tr, sv, be, cs, el, eo, hu, in, uk, vi)

---

## 코드 변경 상세

### Document.java

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/journal/Document.java`

INTROS 페이지 목록 맨 뒤에 "Tutorial" 추가:

```java
INTROS.pagesStates.put("Dungeon",   READ);
INTROS.pagesStates.put("Sewers",    debug ? READ : NOT_FOUND);
INTROS.pagesStates.put("Prison",    debug ? READ : NOT_FOUND);
INTROS.pagesStates.put("Caves",     debug ? READ : NOT_FOUND);
INTROS.pagesStates.put("City",      debug ? READ : NOT_FOUND);
INTROS.pagesStates.put("Halls",     debug ? READ : NOT_FOUND);
INTROS.pagesStates.put("Tutorial",  READ);  // 신규 추가
```

### InterlevelScene.java

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/scenes/InterlevelScene.java`

1. 튜토리얼과 테스트 레벨 분리:
```java
// 기존
boolean isTutorialOrTestLevel = tutorialLevel || testLevel;

// 변경
boolean isTestLevelOnly = testLevel && !tutorialLevel;
```

2. 튜토리얼 스토리 생성 로직 추가:
```java
if (tutorialLevel && mode == Mode.DESCEND) {
    // Tutorial: show "Tutorial" intro story
    isStoryFloor = true;
    createStoryElements("Tutorial");
}
```

3. `createStoryElements(String pageName)` 오버로드 추가:
```java
private void createStoryElements(String pageName) {
    int pageIdx = Document.INTROS.pageIdx(pageName);
    createStoryElementsInternal(Document.INTROS.pageBody(pageName), pageIdx);
}

private void createStoryElementsInternal(String storyText, int pageIdx) {
    // 기존 createStoryElements(int region) 로직을 여기로 이동
    // ...
}
```

---

## 동작 흐름

```
튜토리얼 시작
    ↓
InterlevelScene.create()
    ↓
배경: entrance.jpg + SLOW_FADE (0.9초)
    ↓
createStoryElements("Tutorial") 호출
    ↓
FADE_IN → STATIC (스토리 표시)
    ↓
"계속" 버튼 클릭
    ↓
스토리 페이드 아웃 → 배경 페이드 아웃
    ↓
TutorialLevel 시작
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `journal/Document.java` | INTROS에 "Tutorial" 페이지 추가 |
| `messages/journal/journal.properties` | Tutorial 영문 스토리 텍스트 |
| `messages/journal/journal_*.properties` (23개) | 23개 언어 스토리 텍스트 (~20-25% 간결화) |
| `scenes/InterlevelScene.java` | 튜토리얼 스토리 표시 로직, createStoryElements 오버로드 |

---
