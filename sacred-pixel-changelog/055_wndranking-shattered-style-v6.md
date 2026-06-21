# 055. WndRanking Shattered PD 스타일 + Data Export v0x06

**날짜**: 2026-05-06

## 개요

WndRanking UI를 Shattered PD 스타일로 변경, Rankings.Record에 게임 통계 필드 추가, WebDataExporter/Importer를 v0x06으로 확장, ItemRegistry 확장, DataScene에 Delete All Data 버튼 추가.

---

## 변경 사항

### 1. WndRanking UI 변경 (Shattered PD 스타일)

### 변경된 파일
- `core/.../windows/WndRanking.java`

### 변경 내용

#### 1.1 크기 변경
```java
// Before
private static final int HEIGHT = 220;

// After
private static final int HEIGHT = 144;  // Shattered PD와 동일
```

#### 1.2 StatsTab 완전 재작성

**이전 레이아웃**:
- Date, Turns, Seed
- Score breakdown (Progress, Treasure, Explore, Boss, Quest)
- Final score calculation
- Share Seed / Play Again 버튼

**새 레이아웃 (Shattered PD 스타일)**:
- 날짜 / 버전 (한 줄에 좌우 배치)
- Score + info 버튼 (WndScoreBreakdown 팝업)
- STR (기본 + 보너스)
- Duration (턴 수)
- Depth 또는 Ascent
- Seed 또는 Custom Seed
- Enemies Slain
- Gold Collected
- Food Eaten
- Items Crafted
- Copy Seed 버튼 (Victory 배지 획득 후 활성화)

#### 1.3 ItemsTab QuickSlot 위치 조정
```java
// Before
slot.setRect(pos, HEIGHT - 24, slotWidth, 23);

// After
slot.setRect(pos, 120, slotWidth, 23);  // 고정값 사용
```

#### 1.4 제거된 import
```java
// 제거됨 (더 이상 사용하지 않음)
import com.sacredpixel.sacredpixeldungeon.scenes.DataScene;
import com.sacredpixel.sacredpixeldungeon.scenes.HeroSelectScene;
import com.sacredpixel.sacredpixeldungeon.scenes.InterlevelScene;
import com.sacredpixel.sacredpixeldungeon.ui.ActionIndicator;
import com.sacredpixel.sacredpixeldungeon.GamesInProgress;
```

---

## 2. Rankings.Record 필드 확장

### 변경된 파일
- `core/.../Rankings.java`

### 추가된 필드
```java
// Statistics fields for URL export (v0x06)
public int heroSTR;           // 기본 힘 (10-30)
public int strBonus;          // 힘 보너스 (signed, -128~127)
public int enemiesSlain;      // 처치한 적 수
public int goldCollected;     // 수집한 골드
public int foodEaten;         // 먹은 음식 수
public int itemsCrafted;      // 제작한 아이템 수
```

### submit() 메서드 수정
```java
// Copy statistics for URL export (v0x06)
rec.heroSTR         = Dungeon.hero.STR;
rec.strBonus        = Dungeon.hero.STR() - Dungeon.hero.STR;
rec.enemiesSlain    = Statistics.enemiesSlain;
rec.goldCollected   = Statistics.goldCollected;
rec.foodEaten       = Statistics.foodEaten;
rec.itemsCrafted    = Statistics.itemsCrafted;
```

---

## 3. WebDataExporter/Importer v0x06 확장

### 변경된 파일
- `teavm/.../web/WebDataExporter.java`
- `teavm/.../web/WebDataImporter.java`

### v0x06 주요 변경점

| 항목 | v0x05 | v0x06 |
|------|-------|-------|
| VERSION | 0x05 | 0x06 |
| RECORD_SIZE | 89 bytes | 125 bytes |
| TALENTS_SIZE | 20 bytes (10슬롯) | 32 bytes (16슬롯) |
| ITEMS_SIZE | 12 bytes (6슬롯) | 24 bytes (12슬롯) |
| STATS_SIZE | - | 12 bytes (신규) |
| TOTAL_SIZE | 1096 bytes | 1492 bytes |

### 새 Statistics 필드 레이아웃 (12 bytes)
```
Offset  Size  Field
------  ----  ------------------
0-1     2B    bossScorePrecise (LE)
2-3     2B    questScorePrecise (LE)
4       1B    heroSTR
5       1B    strBonus (signed)
6-7     2B    enemiesSlain (LE)
8-9     2B    goldCollected (LE)
10      1B    foodEaten
11      1B    itemsCrafted
```

### Items 슬롯 배치 (12 슬롯)
| 슬롯 | 내용 |
|------|------|
| 0 | Weapon |
| 1 | Armor |
| 2 | Artifact |
| 3 | Misc |
| 4 | Ring |
| 5 | Trinket |
| 6-11 | QuickSlot[0-5] |

### 하위 호환성
- v0x05 import 지원 (새 필드는 기본값)
- `parseExtendedDataV5()` / `parseExtendedDataV6()` 분리

---

## 4. ItemRegistry 확장

### 변경된 파일
- `teavm/.../web/ItemRegistry.java`

### 추가된 아이템 ID

#### Potions (180-199)
- 180-192: PotionOfHealing, PotionOfStrength, ... PotionOfDivineInspiration

#### Scrolls (200-219)
- 200-213: ScrollOfIdentify, ScrollOfUpgrade, ... ScrollOfAntiMagic

#### Seeds (220-239)
- 220-232: Firebloom, Icecap, ... Mageroyal

#### Trinkets (240-254)
- 240-253: RatSkull, ParchmentScrap, ... SandsOfTime

---

## 5. DataScene Delete All Data 버튼

### 변경된 파일
- `core/.../scenes/DataScene.java`
- `core/.../assets/messages/scenes/scenes.properties`
- `teavm/.../web/WebDataServiceImpl.java`
- `teavm/.../web/WebDataManager.java`

### DataService 인터페이스 추가
```java
/** Clears all browser data (localStorage, IndexedDB, etc.) */
default void clearAllBrowserData() {
    // No-op for non-web platforms
}
```

### UI 추가
- Export 버튼 아래에 "Delete All Data" 버튼 추가
- 2단계 확인 다이얼로그 (실수 방지)

### 삭제 대상 (Sacred PD 데이터만)
- localStorage: `spd_` prefix로 시작하는 키만
- IndexedDB: `spdDB` 데이터베이스만
- 다른 웹사이트 데이터는 영향 없음

### 메시지 키 추가
```properties
scenes.datascene.delete_all=Delete All Data
scenes.datascene.delete_title=Delete All Data
scenes.datascene.delete_warn=This will permanently delete ALL your game data...
scenes.datascene.delete_yes=Delete Everything
scenes.datascene.delete_no=Cancel
scenes.datascene.delete_final_warn=Are you ABSOLUTELY sure?...
scenes.datascene.delete_final_yes=Yes, Delete All
```

---

## URL 길이 변경

| 버전 | Raw | 압축 후 | Base64 | 전체 URL |
|------|-----|---------|--------|----------|
| v0x05 | 1096B | ~280B | ~375자 | ~400자 |
| v0x06 | 1492B | ~380B | ~510자 | ~535자 |
| **증가량** | +396B | +100B | +135자 | +135자 |

---

## 관련 문서

- `Changelog/NEVER-CHANGE-DATA-SYNC.md` - v0x06 레이아웃 문서화
- `게임 기능 수정 - 웹데이터 및 시드 공유.md` - 전체 기능 문서

---
