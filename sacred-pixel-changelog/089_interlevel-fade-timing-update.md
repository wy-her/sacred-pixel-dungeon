# 089. InterlevelScene 페이드 타이밍 업데이트

**날짜**: 2026-06-12

## 개요

InterlevelScene의 페이드 타이밍 상수를 조정하고, 26층 최초 방문 시 SLOW_FADE를 적용하도록 변경.

---

## 변경 사항

### 1. 페이드 타이밍 상수 조정

| 상수 | 이전 | 이후 | FADE_IN + FADE_OUT 총 시간 |
|------|------|------|---------------------------|
| `SLOW_FADE` | 1.0초 | **0.9초** | 1.8초 |
| `NORM_FADE` | 0.67초 | **0.7초** | 1.4초 |
| `FAST_FADE` | 0.5초 | 0.5초 (변경 없음) | 1.0초 |

### 2. 주석 명확화

기존 주석이 혼란스러웠던 부분을 `FADE_IN + FADE_OUT = 총 시간` 형식으로 명확하게 수정:

```java
// 변경 전
private static final float SLOW_FADE = 1f; //.2 in, 1.6 steady, .2 out, 2 seconds total
private static final float NORM_FADE = 0.67f; //.2 in, .47 steady, .2 out, 0.87 seconds total
private static final float FAST_FADE = 0.50f; //.2 in, .3 steady, .2 out, 0.7 seconds total

// 변경 후
//slow fade on entering a new region or floor 26
//FADE_IN (0.9s) + FADE_OUT (0.9s) = 1.8s total
private static final float SLOW_FADE = 0.9f;

//norm fade when loading, falling, returning, or descending to a new floor
//FADE_IN (0.7s) + FADE_OUT (0.7s) = 1.4s total
private static final float NORM_FADE = 0.7f;

//fast fade when ascending, or descending to a floor you've been on
//FADE_IN (0.5s) + FADE_OUT (0.5s) = 1.0s total
//currently unused, reserved for potential future use
private static final float FAST_FADE = 0.5f;
```

### 3. 26층 최초 방문 시 SLOW_FADE 적용

`startPostRegionComplete()` 함수에서 26층 (비스토리 층) 최초 방문 시 `NORM_FADE` 대신 `SLOW_FADE`를 사용하도록 변경:

```java
// 변경 전
} else {
    // Non-story floor (26) - keep black background, wait NORM_FADE, then GameScene
    phase = Phase.FADE_OUT;
    timeLeft = NORM_FADE;
}

// 변경 후
} else {
    // Non-story floor (26) - keep black background, use SLOW_FADE for dramatic effect
    phase = Phase.FADE_OUT;
    timeLeft = SLOW_FADE;
}
```

---

## 층별 전환 시간 요약

| 시나리오 | 페이드 타입 | 총 시간 |
|----------|------------|---------|
| 새 게임 시작 (1층) | SLOW_FADE | 1.8초 |
| 6, 11, 16, 21층 최초 방문 | WndRegionComplete + 스토리 | 가변 |
| **26층 최초 방문** | **SLOW_FADE** | **1.8초** |
| 일반 층 하강/낙하/귀환 | NORM_FADE | 1.4초 |
| 재방문/상승 | NORM_FADE | 1.4초 |

---

## 관련 상수

| 상수 | 값 | 용도 |
|------|-----|------|
| `BG_FADE_DURATION` | 0.5초 | 스토리 시퀀스 배경 페이드 |
| `STORY_FADE_DURATION` | 0.8초 | 스토리 텍스트 페이드 |
| `MINIMUM_DISPLAY_TIME` | 0.5초 | 최소 표시 시간 |
| `BTN_INPUT_DELAY` | 0.5초 | Continue 버튼 입력 지연 |

---

## 수정된 파일

| File | Changes |
|------|---------|
| `core/.../scenes/InterlevelScene.java` | 페이드 타이밍 상수 조정, 26층 SLOW_FADE 적용 |

---
