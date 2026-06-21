# 009. UI 폰트 스케일링 및 퀘스트 토큰 보존 버그 수정

**날짜**: 2026-03-17

## 개요

UI 폰트 스케일링 개선(띄어쓰기 간격의 폰트 크기 비례, 제목 폰트 축소), 임프 퀘스트 토큰 소실 버그 수정, 일지 텍스트 변경.

---

## 변경 사항

### 1. 띄어쓰기 간격 폰트 크기 비례 수정

### 문제
- 제목(size 9)과 상세설명(size 6)의 띄어쓰기 간격이 동일하여, 작은 글씨에서도 큰 글씨와 같은 폭의 공백이 표시됨
- 글자 간 간격 보정값(`-0.667f`)도 폰트 크기와 무관하게 고정

### 수정 내용

**`SPD-classes/.../noosa/RenderedText.java`**
- `spaceXadvance()` 메서드 추가 (폰트의 공백 폭을 외부에서 접근 가능하게 함)

**`core/.../ui/RenderedTextBlock.java`**
- 하드코딩된 `1.667f`(띄어쓰기)와 `0.667f`(글자 간 보정)를 제거
- 논리 폰트 크기(`size × zoom`)에 비례하여 계산하도록 변경:
  ```java
  float logicalSize = size * (zoom != 0 ? zoom : 1);
  float scaledSpaceW = logicalSize * 5f / 18f;
  float charGap = logicalSize / 9f;
  ```
- size 6 기준 기존 값과 동일 (하위 호환), 다른 크기에서 비례 적용:

| 폰트 크기 | scaledSpaceW | charGap |
|-----------|-------------|---------|
| 6 | 1.667px | 0.667px |
| 7 | 1.944px | 0.778px |
| 8 | 2.222px | 0.889px |

---

## 2. 제목 폰트 크기 9 → 8 축소

### 문제
- 제목 폰트(size 9)가 상세설명(size 6)에 비해 너무 커서 균형이 맞지 않음

### 수정 내용
14개 파일에서 `renderTextBlock(..., 9)` 또는 `renderTextBlock(9)` → `8`로 변경:

| 파일 | 변경 수 |
|------|--------|
| `windows/IconTitle.java` | 1 (`FONT_SIZE = 9` → `8`) |
| `windows/WndHeroInfo.java` | 4 |
| `windows/WndOptions.java` | 1 |
| `windows/WndCombo.java` | 1 |
| `windows/WndSettings.java` | 7 |
| `windows/WndTextInput.java` | 1 |
| `windows/WndJournal.java` | 1 |
| `windows/WndMonkAbilities.java` | 1 |
| `windows/WndTabbed.java` | 1 |
| `scenes/AlchemyScene.java` | 1 |
| `scenes/StartScene.java` | 1 |
| `ui/RadialMenu.java` | 1 |
| `ui/TalentsPane.java` | 1 |
| `ui/ScrollingListPane.java` | 2 |

**총 24개 변경**

---

## 3. HeroSelectScene 제목 폰트 크기 12 → 11 축소

### 파일
- `scenes/HeroSelectScene.java` (line 143)

---

## 4. 임프 퀘스트 토큰 소실 버그 수정

### 문제
- 볼트에서 수집한 DwarfToken이 정상 경로(출구 사용)나 탈출수정 사용 시 모두 사라짐
- 원인: `restoreHeroBelongings()`가 현재 인벤토리를 볼트 진입 전 상태로 완전히 교체

### 수정 내용

**`core/.../levels/VaultLevel.java`** (정상 출구)
- `restoreHeroBelongings()` 호출 전 토큰 수량 저장
- 복원 후 토큰을 인벤토리에 다시 추가

**`core/.../items/quest/EscapeCrystal.java`** (탈출수정 사용)
- 동일한 토큰 보존 로직 추가
- `Imp.Quest.setVaultAttempted()` 호출 추가 (누락되어 있었음)

---

## 5. 일지 텍스트 변경

### 파일
- `core/src/main/assets/messages/journal/journal_ko.properties`

### 변경
- `연금술의 기초 및 물약들` → `연금술 기초 및 물약`

---

## 수정된 파일

| File | Changes |
|------|---------|
| `SPD-classes/.../noosa/RenderedText.java` | spaceXadvance() 메서드 추가 |
| `core/.../ui/RenderedTextBlock.java` | 폰트 크기 비례 띄어쓰기 간격 계산 |
| `core/.../windows/IconTitle.java` | FONT_SIZE 9→8 |
| `core/.../windows/WndHeroInfo.java` | 폰트 크기 9→8 (4곳) |
| `core/.../windows/WndOptions.java` | 폰트 크기 9→8 |
| `core/.../windows/WndCombo.java` | 폰트 크기 9→8 |
| `core/.../windows/WndSettings.java` | 폰트 크기 9→8 (7곳) |
| `core/.../windows/WndTextInput.java` | 폰트 크기 9→8 |
| `core/.../windows/WndJournal.java` | 폰트 크기 9→8 |
| `core/.../windows/WndMonkAbilities.java` | 폰트 크기 9→8 |
| `core/.../windows/WndTabbed.java` | 폰트 크기 9→8 |
| `core/.../scenes/AlchemyScene.java` | 폰트 크기 9→8 |
| `core/.../scenes/StartScene.java` | 폰트 크기 9→8 |
| `core/.../scenes/HeroSelectScene.java` | 폰트 크기 12→11 |
| `core/.../ui/RadialMenu.java` | 폰트 크기 9→8 |
| `core/.../ui/TalentsPane.java` | 폰트 크기 9→8 |
| `core/.../ui/ScrollingListPane.java` | 폰트 크기 9→8 (2곳) |
| `core/.../levels/VaultLevel.java` | 토큰 보존 로직 추가 |
| `core/.../items/quest/EscapeCrystal.java` | 토큰 보존 로직 + setVaultAttempted() |
| `core/.../messages/journal/journal_ko.properties` | 텍스트 변경 |
