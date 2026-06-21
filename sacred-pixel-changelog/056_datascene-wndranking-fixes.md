# 056. DataScene 및 WndRanking 버그 수정

**날짜**: 2026-05-07

## 개요

타이틀 화면 레이아웃 변경, DataScene UI 개선, WndRanking 관련 버그 수정 및 다국어 번역 업데이트. 첫 플레이 시 가이드 표시 방식 변경.

---

## 변경 사항

### Title Screen Button Layout Reorganization

**File:** `TitleScene.java`

버튼 레이아웃을 다음과 같이 변경:
- **Row 1:** 모험을 시작하기 (전체 너비)
- **Row 2:** 데이터 동기화 (전체 너비)
- **Row 3:** 랭킹, 일지 (반반)
- **Row 4:** 설정, 소개 (반반)
- **Row 5:** Test Level (디버그 모드에서만, 전체 너비)

### Title Screen Data Button Icon Change

**File:** `TitleScene.java`

'데이터' 버튼 아이콘을 `Icons.DATA` → `Icons.CHANGES`로 변경 (원본 Shattered Pixel Dungeon의 '변경' 버튼 아이콘과 동일).

### Keyboard Focus Order Update

**File:** `TitleScene.java`

`focusableButtons` 순서를 새 레이아웃에 맞게 업데이트:
```java
focusableButtons.add(btnPlay);
focusableButtons.add(btnData);
focusableButtons.add(btnRankings);
focusableButtons.add(btnJournal);
focusableButtons.add(btnSettings);
focusableButtons.add(btnAbout);
```

### DataScene Icon Consistency

**File:** `DataScene.java`

- 화면 타이틀 아이콘을 `Icons.DATA` → `Icons.CHANGES`로 변경
- 가져오기 미리보기 창(WndImportPreview) 타이틀 아이콘 제거

### WndRanking Stats Tab UI Improvements

**File:** `WndRanking.java`

- Play Again 버튼 아이콘 제거
- GAP 조건을 import된 레코드에서도 동일하게 적용하여 레이아웃 일관성 유지

### First-Play Guide Display Change

**File:** `GameScene.java`

첫 플레이 시 저널 버튼 깜빡임 대신 가이드가 바로 WndStory 창으로 화면에 표시되도록 변경.

### WndRanking ScoreBreakdown Text Shortening

**Files:** `windows_*.properties` (22 languages)

ScoreBreakdown 창에서 텍스트 오버플로우 문제 해결:
- "Win Multiplier" → "Win Bonus"
- "Challenge Multiplier" → "Challenge Bonus"

모든 22개 언어에서 동일하게 축약 적용.

### Translation Updates

**Files:** `scenes_*.properties`, `windows_*.properties`

- 타이틀 화면 '데이터' 버튼 텍스트: "Data" → "Data Sync" (모든 언어)
- WndRanking '다시 플레이' → '다시 플레이하기' (한국어)
- DataScene 관련 키 누락 번역 추가 (22개 언어)

---

## Technical Details

### WndRanking Import Data Issue (Investigated)

Import 후 특성/아이템/뱃지 탭에서 "no data available" 오류 발생 원인 분석:

**Root Cause:** `WebDataExporter`에서 `r.gameData`가 null일 때 `encodePerRunBadges/Talents/Items`가 빈 값(0xFF)으로 채워짐.

**Affected Scenario:**
1. 기존에 import된 레코드(gameData=null)를 다시 export하면
2. Extended data가 모두 0xFF/0x00 placeholder로 채워지고
3. 이를 다시 import하면 빈 데이터가 복원됨

**Solution Required:** `WebDataExporter`에서 `r.gameData`가 null일 때 `r.importedBadges/Talents/Items`를 대신 사용하도록 수정 필요.

---

## 수정된 파일

| File | Changes |
|------|---------|
| `core/.../scenes/TitleScene.java` | 버튼 레이아웃, 아이콘, 포커스 순서 변경 |
| `core/.../scenes/GameScene.java` | 첫 플레이 가이드 WndStory로 표시 |
| `core/.../scenes/DataScene.java` | 아이콘 변경, 미리보기 창 타이틀 아이콘 제거 |
| `core/.../windows/WndRanking.java` | Play Again 버튼 아이콘 제거, GAP 조건 |
| `scenes_*.properties` | 데이터 버튼 텍스트 변경 (22개 파일) |
| `windows_*.properties` | ScoreBreakdown 텍스트 축약 (22개 파일) |

---
