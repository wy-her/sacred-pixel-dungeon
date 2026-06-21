# 087. 스토리 페이드 타이밍 조정

**날짜**: 2026-06-07

## 개요

스토리 시퀀스 페이드 타이밍 증가 (더 부드러운 전환) 및 버전명 INDEV로 변경 (테스트용).

---

## 변경 사항

### 1. 스토리 페이드 타이밍 조정

**파일**: `core/.../scenes/InterlevelScene.java`

### 문제

스토리 층(1, 6, 11, 16, 21층)에서 페이드-인/아웃이 너무 빠르게 느껴짐.

기존 `SLOW_FADE`(1.0초)는 스토리 층에서 사용되지 않고, 실제로는 `BG_FADE_DURATION`(0.3초)과 `STORY_FADE_DURATION`(0.5초)이 적용됨.

### 수정 내용

```java
// 변경 전
private static final float BG_FADE_DURATION = 0.3f;
private static final float STORY_FADE_DURATION = 0.5f;

// 변경 후
private static final float BG_FADE_DURATION = 0.5f;
private static final float STORY_FADE_DURATION = 0.8f;
```

### 변경 후 타이밍

| 단계 | 변경 전 | 변경 후 |
|------|---------|---------|
| 배경 페이드-인 | 0.3초 | **0.5초** |
| 스토리 페이드-인 | 0.5초 | **0.8초** |
| 스토리 페이드-아웃 | 0.5초 | **0.8초** |
| 배경 페이드-아웃 | 0.3초 | **0.5초** |
| **합계** (클릭 제외) | 1.6초 | **2.6초** |

### 2. 버전명 INDEV 변경

**파일**: `teavm/.../TeaVMLauncher.java`

테스트 빌드 식별을 위해 버전명 변경.

```java
// 변경 전
Game.version = "4.0-HTML";

// 변경 후
Game.version = "4.0-INDEV";
```

### 참고: SLOW_FADE와의 관계

`SLOW_FADE`(1.0초)는 스토리 층에서 `fadeTime`으로 설정되지만, 실제 FADE_IN 페이즈가 스킵되어 사용되지 않음:

```java
// InterlevelScene.java:341-348
if (isStoryFloor && isNewFloor && mode == Mode.DESCEND && Dungeon.hero != null) {
    //Skip fade-in for story floors (6,11,16,21) - WndRegionComplete shown first
    phase = Phase.STATIC;  // ← FADE_IN 페이즈를 건너뜀
    ...
}
```

대신 STATIC 페이즈에서 `BG_FADE_DURATION`과 `STORY_FADE_DURATION`으로 자체 페이드를 처리함.

---

## 수정된 파일

| File | Changes |
|------|---------|
| `scenes/InterlevelScene.java` | BG_FADE_DURATION 0.5f, STORY_FADE_DURATION 0.8f |
| `teavm/.../TeaVMLauncher.java` | Game.version = "4.0-INDEV" |

---
