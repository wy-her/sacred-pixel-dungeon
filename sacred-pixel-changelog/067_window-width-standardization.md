# 067. 창 너비 표준화 (mod 6 = 5)

**날짜**: 2026-05-24

## 개요

모든 Window 너비를 **mod 6 = 5** 패턴으로 표준화:
- **Portrait**: 149px
- **Landscape**: 251px

이 패턴은 2-button row와 3-button row에서 정수 픽셀 너비를 보장합니다.

### Button Width 계산

```java
equalW = (width - (curRow.size()-1)) / curRow.size()
```

| Width | 2-Button | 3-Button |
|-------|----------|----------|
| 149   | (149-1)/2 = 74 | (149-2)/3 = 49 |
| 251   | (251-1)/2 = 125 | (251-2)/3 = 83 |

---

## 변경 사항

총 **50+ 파일**에 걸쳐 창 너비가 표준화되었습니다.

---

## 수정된 파일

### Category 1: Fixed Width Windows (149px)

### 단일 너비 상수 창

| File | Before | After |
|------|--------|-------|
| `WndInfoBuff.java` | 120 | 149 |
| `WndInfoCell.java` | 120 | 149 |
| `WndChallenges.java` | 120 | 149 |
| `WndChooseSubclass.java` | 120 | 149 |
| `WndChooseAbility.java` | 120 | 149 |
| `WndHeroInfo.java` | 120 | 149 |
| `WndResurrect.java` | 120 | 149 |
| `WndUpgrade.java` | 120 | 149 |
| `WndRegionComplete.java` | 120 | 149 |
| `WndScoreBreakdown.java` | 120 | 149 |
| `WndScoreBreakdownRecord.java` | 120 | 149 |
| `WndTextInput.java` | 120 | 149 |
| `WndHero.java` | 120 | 149 |
| `WndGame.java` | 120 | 149 |
| `WndGameInProgress.java` | 120 | 149 |
| `WndList.java` | 120 | 149 |
| `WndClericSpells.java` | 140 | 149 |
| `WndRanking.java` | 120 | 149 |
| `WndBadge.java` | 120 | 149 |
| `WndDocument.java` | 120 | 149 |

### Category 2: Portrait/Landscape Windows (149/251)

### WIDTH_P / WIDTH_L 패턴 적용

| File | Before P/L | After P/L |
|------|-----------|-----------|
| `WndInfoItem.java` | 120/120 | 149/251 |
| `WndInfoTalent.java` | 120/120 | 149/251 |
| `WndMessage.java` | 120/120 | 149/251 |
| `WndTitledMessage.java` | 120/120 | 149/251 |
| `WndStory.java` | 120/220 | 149/251 |
| `WndOptions.java` | 120/120 | 149/251 |
| `WndCombo.java` | 120/120 | 149/251 |
| `WndMonkAbilities.java` | 120/120 | 149/251 |
| `WndBlacksmith.java` | 120/120 | 149/251 |
| `WndSupportPrompt.java` | 120/120 | 149/251 |
| `WndVictoryCongrats.java` | 120/250 | 149/251 |
| `WndChangesTabbed.java` | 120/200 | 149/251 |

### Category 3: NPC Dialog Windows (149/251)

### WIDTH 단일상수 → WIDTH_P/WIDTH_L 변경

| File | Before | After P/L |
|------|--------|-----------|
| `WndImp.java` | 120 (fixed) | 149/251 |
| `WndSadGhost.java` | 120 (fixed) | 149/251 |
| `WndWandmaker.java` | 120 (fixed) | 149/251 |

**변경 예시 (WndImp.java):**

```java
// Before
private static final int WIDTH		= 120;

// After
private static final int WIDTH_P    = 149;
private static final int WIDTH_L    = 251;

// Constructor
public WndImp( ... ) {
    int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;
    // ... 이후 WIDTH 대신 width 사용
}
```

### Category 4: Scene-Level Windows (149/251)

### Settings/Journal/Data 관련 장면

| File | Before P/L | After P/L |
|------|-----------|-----------|
| `SettingsScene.java` | 147/250 | 149/251 |
| `WndSettings.java` | 147/250 | 149/251 |
| `WndJournal.java` | 147/250 | 149/251 |
| `JournalScene.java` | 147/250 | 149/251 |
| `DataScene.java` | 147/250 | 149/251 |
| `AmuletScene.java` | 120 | 149 |

### Category 5: Inner Class Windows

### Item 내부 클래스

| File | Class | Before | After |
|------|-------|--------|-------|
| `TrinketCatalyst.java` | WndTrinket | 120 | 149 |
| `StoneOfIntuition.java` | WndGuess | 120 | 149 |
| `StoneOfAugmentation.java` | WndAugment | 120 | 149 |
| `DriedRose.java` | WndGhostHero | 116 | 149 |
| `ScrollOfDivination.java` | WndDivination | 120 | 149 |
| `ScrollOfMetamorphosis.java` | WndMetamorphChoose | hardcoded 120 | 149 |
| `ScrollOfMetamorphosis.java` | WndMetamorphReplace | hardcoded 120 | 149 |

### Scene 내부 클래스

| File | Class | Before | After |
|------|-------|--------|-------|
| `HeroSelectScene.java` | WndRandomize | hardcoded | `int width = 149;` 변수 사용 |
| `SettingsScene.java` | WndToolbarSettings | 147 | 149 |

### Category 6: Inherited Windows (No Change Required)

다음 창들은 부모 클래스의 WIDTH를 상속하므로 별도 변경 불필요:

### WndInfoItem 상속 (149/251 자동 적용)
- `WndUseItem.java`
- `WndTradeItem.java`
- `WndSadGhostItem.java` (WndUseItem 상속)
- `WndImpItem.java` (WndUseItem 상속)

### WndTitledMessage 상속 (149/251 자동 적용)
- `WndInfoArmorAbility.java`
- `WndInfoSubclass.java`
- `WndDust.java`
- `WndLevelSeed.java`
- 기타 메시지/정보 창들

### WndOptions 상속 (149/251 자동 적용)
- `WndKeyBindings.java`
- 기타 옵션 창들

### Key Pattern

### 권장 상수 정의

```java
// Portrait only (대부분의 작은 창)
private static final int WIDTH = 149;

// Portrait + Landscape (큰 정보 창, NPC 대화)
private static final int WIDTH_P = 149;
private static final int WIDTH_L = 251;

// Constructor에서 사용
int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;
```

### mod 6 = 5 공식

모든 창 너비는 다음 조건을 만족:
```
width % 6 == 5
```

가능한 값: 149, 155, 161, ..., 251, 257, ...

**149**와 **251**이 표준으로 선택된 이유:
- 149: 화면 비율에 적합한 portrait 크기
- 251: 화면 비율에 적합한 landscape 크기
- 두 값 모두 2-button, 3-button 레이아웃에서 정확한 정수 너비 제공

### Files NOT Changed (Intentional)

다음 파일들은 특수한 목적으로 다른 너비 사용:

| File | Width | Reason |
|------|-------|--------|
| `WndBag.java` | 동적 | 인벤토리 그리드 크기에 따라 계산 |
| `WndQuickBag.java` | 동적 | 인벤토리 그리드 크기에 따라 계산 |
| `WndAlchemy.java` | 동적 | 연금술 UI 특수 레이아웃 |

### Testing Notes

변경 후 다음 항목 확인:
- [ ] 모든 창의 버튼이 정렬됨 (픽셀 단위 정수 너비)
- [ ] Portrait/Landscape 전환 시 올바른 너비 적용
- [ ] NPC 대화창 텍스트 줄바꿈 정상
- [ ] 내부 클래스 창들도 149px 적용됨
- [ ] Settings/Journal/Data 화면 너비 정상

---
