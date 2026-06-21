# 058. 데이터 내보내기 VERSION 0x09 - JSON 최적화

**날짜**: 2026-05-08

## 개요

Rankings 데이터 내보내기 방식을 완전히 재설계하여 원본 run record의 **모든 정보**를 보존하면서 URL 길이를 최소화합니다. gameData를 JSON으로 직접 저장하고 3가지 최적화 적용.

---

## 변경 사항

### VERSION 0x09 포맷 도입

이전 버전들(v0x01~v0x06)은 고정 크기 바이너리 레코드로 저장했지만, 이 방식은:
- 아이템의 강화레벨, 저주, 마법 등 부가 정보 손실
- Talents의 세부 정보 손실
- gameData Bundle의 일부 필드만 추출

VERSION 0x09는 **gameData를 JSON으로 직접 저장**하여 모든 정보를 보존합니다.

### Rankings TABLE_SIZE 변경

```java
// Before: Top 10 + 1 (most recent)
public static final int TABLE_SIZE = 11;

// After: Top 5 + 1 (most recent) for shorter URL
public static final int TABLE_SIZE = 6;
```

- URL 길이를 줄이기 위해 저장 레코드 수 축소
- 최근 run은 항상 보존 (6번째 슬롯)
- 기존 코드는 주석 처리로 원복 가능

### JSON 최적화 3종 적용

#### Option 1: JSON Key Shortening (40+ 키)
```java
"__className" → "_c"
"quantity" → "q"
"level" → "l"
"weapon" → "w"
"armor" → "a"
"artifact" → "ar"
"talents" → "ta"
// ... 40+ more mappings
```

#### Option 2: Class Name Shortening
```java
"com.sacredpixel.sacredpixeldungeon." → "~."
```

#### Option 3: Backpack Trimming
- Backpack items 중 QuickSlot에 할당된 것만 보존
- 나머지 인벤토리 아이템 제거
- 장비 슬롯(weapon, armor, artifact, misc, ring)은 유지

## VERSION 0x09 Layout

```
Offset  Size    Field
──────  ──────  ───────────────────────────
0       1       VERSION (0x09)
1       24      Global Badges bitmap (192 bits)
25      50      Catalog bitmap (400 bits)
75      25      Bestiary bitmap (200 bits)
100     16      Document bitmap (64 pages × 2 bits)
116     1       Rankings count (0-6)
117     2       JSON length (LE)
119     var     Optimized JSON array
──────  ──────  ───────────────────────────
Total:  ~2000 bytes (variable)
```

### Global Section (116 bytes, fixed)
- VERSION: 0x09
- Badges: 24 bytes (192 badges)
- Catalog: 50 bytes (400 items)
- Bestiary: 25 bytes (200 entities)
- Document: 16 bytes (64 pages × 2 bits)

### Rankings Section (variable)
- Count: 1 byte (0-6)
- JSON Length: 2 bytes LE
- JSON Array: Optimized gameData JSON

## Expected URL Length

| Content | Raw Bytes | Compressed | Base64 |
|---------|-----------|------------|--------|
| 6 records (full JSON) | ~3000 B | ~800 B | ~1100 chars |
| With optimizations | ~2000 B | ~600 B | ~800 chars |
| Final URL | - | - | ~850 chars |

---

## 수정된 파일

| File | Changes |
|------|---------|
| `WebDataExporter.java` | VERSION 0x09, KEY_MAP 40+ mappings, JSON 최적화 |
| `WebDataImporter.java` | REVERSE_KEY_MAP, restoreJson(), createRecordFromGameData() |
| `WebDataMerger.java` | Top-6 변경, 루프 제한 6 |
| `Rankings.java` | TABLE_SIZE = 6 (Top 5 + 1) |
| `WndRanking.java` | BadgesTab 클릭 가능 그리드, 키보드 내비게이션 |

---

### 세부 변경 사항

#### WebDataExporter.java (Complete Rewrite)
- `VERSION = 0x09`
- `MAX_RANKINGS = 6`
- `KEY_MAP`: 40+ key shortening mappings
- `CLASS_PREFIX/CLASS_SHORT`: Class name shortening
- `injectRecordFields()`: Inject Record-level fields into JSON
- `optimizeJson()`: Apply all 3 optimizations
- `trimBackpack()`: Keep only QuickSlot items

#### WebDataImporter.java (Complete Rewrite)
- `REVERSE_KEY_MAP`: Reverse mappings for restoration
- `restoreJson()`: Expand shortened keys and class names
- `createRecordFromGameData()`: Extract injected Record-level fields
- Legacy version support (v0x01~v0x06) maintained

#### WndRanking.java BadgesTab 변경
- Import된 배지를 클릭 가능한 `BadgeButton` 그리드로 변경
- 이전: 텍스트 리스트 (클릭 불가)
- 이후: 아이콘 그리드 (클릭 시 WndBadge 팝업)
- `layoutBadgeGrid()`: 그리드 레이아웃 계산
- `moveFocus()`, `activateFocused()`: 키보드 내비게이션 지원

#### Badges 탭 키보드 네비게이션 활성화
- `currentBadgesTab`, `badgesComponent` 필드 추가
- `RankingTab.select()`: Badges 탭 선택 시 `BadgesGrid`/`BadgesList`의 `setKeyboardActive(true)` 호출
- `setupKeyListener()`: Badges 탭 visible 시 키보드 이벤트 처리
  - Full game data: `BadgesGrid`/`BadgesList`의 자체 키보드 리스너 활용
  - Imported data: 커스텀 `BadgesTab`의 `moveFocus()`, `activateFocused()` 호출
- `destroy()`: 윈도우 닫을 때 `setKeyboardActive(false)` 정리

## Backward Compatibility

### Import (하위 호환)
- v0x01~v0x08: 기존 파싱 로직 유지
- v0x09: 새 JSON 기반 파싱

### Export (상위 버전만)
- 항상 v0x09로 내보내기
- 이전 버전 형식으로 내보내기 불가

## Migration Notes

1. **기존 URL Import**: v0x01~v0x08 URL은 계속 지원
2. **새 URL Export**: 항상 v0x09 형식
3. **Rankings 6개 제한**: 기존 11개 → 6개로 축소

## Testing Checklist

- [x] Export generates valid URL
- [x] Import v0x09 restores all gameData
- [x] Import legacy versions (v0x01~v0x08) works
- [x] WndRanking displays all data correctly
- [x] Items tab shows equipment with enchantments/curses
- [x] Talents tab shows full talent tree
- [x] Badges tab shows per-run badges
- [x] Badges tab: 클릭 시 WndBadge 팝업 표시
- [x] Badges tab: 키보드 네비게이션 지원 (화살표 키 + Enter)
- [x] Rankings list: 사망 원인(cause) 정상 표시 (Record-level 필드 주입으로 해결)
- [x] RankingsScene에서 표시하는 모든 필드 정상 표시 (depth, level, heroClass 등)

---
