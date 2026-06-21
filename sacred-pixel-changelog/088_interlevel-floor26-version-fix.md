# 088. InterlevelScene 26층 처리 및 버전 표시 수정

**날짜**: 2026-06-10

## 개요

InterlevelScene의 26층 처리를 6,11,16,21층과 동일하게 WndRegionComplete 방식으로 변경. 랭킹 화면 버전 표시 문제 수정 (버전 형식을 x.x.x로 통일). 게임 버전 관리 가이드 문서 추가.

---

## 변경 사항

### 1. InterlevelScene.java - Fade 타이밍 정리

#### FAST_FADE 주석 추가
```java
//fast fade when ascending, or descending to a floor you've been on
//currently unused, reserved for potential future use
private static final float FAST_FADE = 0.50f;
```

#### SLOW_FADE 설정 제거 (6,11,16,21층)
- 이 층들은 FADE_IN을 건너뛰므로 SLOW_FADE 설정이 무의미
- 기존: floors 6,11,16,21,26에 SLOW_FADE 설정
- 변경: 모두 제거 (주석으로 설명 추가)

```java
//floors 6,11,16,21,26 skip FADE_IN (WndRegionComplete shown first)
//so SLOW_FADE is not applied to any region complete floors
```

### 2. InterlevelScene.java - 26층 WndRegionComplete 적용

#### create() - FADE_IN 건너뛰기에 26층 추가
```java
//Skip fade-in for WndRegionComplete floors (6,11,16,21,26)
boolean isRegionCompleteFloor = isNewFloor && mode == Mode.DESCEND && Dungeon.hero != null
        && (loadingDepth == 6 || loadingDepth == 11 || loadingDepth == 16 || loadingDepth == 21 || loadingDepth == 26);
```

#### proceedAfterLoading() - STAGE_CLEAR에 26층 추가
```java
// Show WndRegionComplete on region complete floors: 6, 11, 16, 21, 26 (first visit only)
if (Dungeon.depth == 6 || Dungeon.depth == 11 || Dungeon.depth == 16 || Dungeon.depth == 21 || Dungeon.depth == 26) {
```

#### startPostRegionComplete() - 스토리 없는 층 처리
26층은 스토리가 없으므로 WndRegionComplete 후 검은 배경 유지 + NORM_FADE 대기 후 GameScene 전환:

```java
} else {
    // Non-story floor (26) - keep black background, wait NORM_FADE, then GameScene
    // Background stays hidden (black) during FADE_OUT
    phase = Phase.FADE_OUT;
    timeLeft = NORM_FADE;
}
```

#### 26층 최종 흐름
1. FADE_IN 건너뜀 → STATIC (배경 숨김)
2. 로딩 완료 → STAGE_CLEAR
3. WndRegionComplete 표시 (검은 배경)
4. 닫으면 → FADE_OUT (NORM_FADE = 0.67초, 검은 화면 유지)
5. GameScene 전환

---

### 3. 게임 버전 형식 수정

#### 문제
- 기존 버전 `"4.0-HTML"`은 정규식 `\d+\.\d+\.\d+`에 매칭되지 않음
- 랭킹 화면에서 버전이 표시되지 않음 (빈 문자열)
- Import된 런은 fallback으로 `"v891"` (정수) 표시

#### 해결
버전 형식을 `x.x.x`로 변경

**build.gradle:**
```groovy
appVersionCode = 900
// appVersionName must be in x.x.x format for RankingsScene version display
appVersionName = '4.0.0'
```

**TeaVMLauncher.java:**
```java
// Game.version must be in x.x.x format for RankingsScene version display
Game.version = "4.0.0";
Game.versionCode = 900;
```

---

### 4. 문서 추가

**docs/guide/version_management.md** 생성:
- 수정해야 할 파일 위치 (build.gradle, TeaVMLauncher.java)
- 버전 형식 요구사항 (`x.x.x` 필수 이유)
- 랭킹 화면 버전 파싱 정규식 설명
- 주의사항 및 변경 이력

---

## 수정된 파일

| File | Changes |
|------|---------|
| `core/.../scenes/InterlevelScene.java` | FAST_FADE 주석, SLOW_FADE 제거, 26층 WndRegionComplete 적용 |
| `build.gradle` | versionCode 900, versionName '4.0.0' |
| `teavm/.../TeaVMLauncher.java` | version "4.0.0", versionCode 900 |
| `docs/guide/version_management.md` | 신규 생성 |

---

## 테스트 항목

- [ ] 26층 진입 시 WndRegionComplete 표시 확인
- [ ] WndRegionComplete 닫은 후 검은 화면 → GameScene 전환 확인
- [ ] 랭킹 화면에서 새 런의 버전 "v4.0.0" 표시 확인
- [ ] 타이틀 화면 버전 "v4.0.0" 표시 확인

---
