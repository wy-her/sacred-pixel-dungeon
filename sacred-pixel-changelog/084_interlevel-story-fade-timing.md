# 084. 인터레벨 씬 및 스토리 표시 페이드 타이밍 개선

**날짜**: 2026-06-06

## 개요

InterlevelScene의 배경 및 스토리 페이드 타이밍을 조정하고, 1층 진입 시 두 개의 스토리(던전/하수도)에 대한 배경 전환 로직을 구현.

---

## 변경 사항

### 1. 페이드 타이밍 상수 조정

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/scenes/InterlevelScene.java`

```java
// 변경 전
private static final float MINIMUM_DISPLAY_TIME = 1.0f;

// 변경 후
private static final float BG_FADE_DURATION = 0.2f;      // 배경 페이드 시간
private static final float STORY_FADE_DURATION = 0.3f;   // 스토리 페이드 시간
private static final float MINIMUM_DISPLAY_TIME = 0.5f;  // 일반층 최소 표시 시간 (1.0 → 0.5)
```

### 2. 1층 배경 전환 로직 (ENTRANCE → SEWERS)

1층 진입 시 두 개의 스토리가 표시됨:
- **첫 번째 스토리**: 던전 입구 (`ENTRANCE` 배경)
- **두 번째 스토리**: 하수도 진입 (`SEWERS` 배경)

```java
// 1층 특수 처리: 첫 번째 스토리 후 배경 전환
if (depth == 1 && !secondStoryShown) {
    // ENTRANCE → SEWERS 크로스페이드
    bgFadingOut = true;  // 현재 배경 페이드아웃
    bgTransitioning = true;
}
```

### 3. 배경 페이드 상태 변수 추가

```java
private boolean bgFadingIn = false;      // 배경 페이드인 중
private boolean bgFadingOut = false;     // 배경 페이드아웃 중
private boolean bgTransitioning = false; // 배경 전환 중 (ENTRANCE→SEWERS)
private float bgFadeTime = 0f;           // 페이드 경과 시간
```

### 4. ENTRANCE 배경 에셋 추가

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/Assets.java`

```java
public static final String ENTRANCE = "splashes/entrance.jpg";
```

### 5. 파일명 오타 수정

```
변경 전: core/src/main/assets/splashes/enterence.jpg
변경 후: core/src/main/assets/splashes/entrance.jpg
```

**관련 파일 업데이트**:
- `capacitor-app/android/app/src/main/assets/public/assets/preload.txt`

---

## 페이드 시퀀스

### 일반 층 (2층 이상)

```
[진입] → 배경 페이드인(0.2s) → 최소 표시(0.5s) → 배경 페이드아웃(0.2s) → [게임]
```

### 스토리가 있는 층 (6, 11, 16, 21층)

```
[진입] → 배경 페이드인(0.2s) → 스토리 페이드인(0.3s) → [클릭 대기]
      → 스토리 페이드아웃(0.3s) → 배경 페이드아웃(0.2s) → [게임]
```

### 1층 (두 개의 스토리)

```
[진입] → ENTRANCE 배경 페이드인(0.2s) → 던전 스토리 페이드인(0.3s) → [클릭]
      → 던전 스토리 페이드아웃(0.3s) → ENTRANCE 페이드아웃(0.2s)
      → SEWERS 배경 페이드인(0.2s) → 하수도 스토리 페이드인(0.3s) → [클릭]
      → 하수도 스토리 페이드아웃(0.3s) → SEWERS 페이드아웃(0.2s) → [게임]
```

---

## 관련 층별 스토리 및 배경

| 층 | 스토리 | 배경 |
|----|--------|------|
| 1 | INTRO_DUNGEON + INTRO_SEWERS | ENTRANCE → SEWERS |
| 6 | INTRO_PRISON | SEWERS |
| 11 | INTRO_CAVES | PRISON |
| 16 | INTRO_CITY | CAVES |
| 21 | INTRO_HALLS | CITY |

---

## 수정된 파일

| File | Changes |
|------|---------|
| `scenes/InterlevelScene.java` | 페이드 타이밍 상수 조정, 배경 전환 로직 |
| `Assets.java` | ENTRANCE 배경 에셋 추가 |
| `splashes/entrance.jpg` | 파일명 오타 수정 (enterence → entrance) |

---
