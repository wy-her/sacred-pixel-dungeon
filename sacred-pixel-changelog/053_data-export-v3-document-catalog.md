# 053. 데이터 Export V4 - 날짜 필드 및 일일 챌린지 제거

**날짜**: 2026-04-26

## 개요

데이터 Export/Import 시스템을 VERSION 0x04로 업그레이드하여 기록 날짜(recordDate) 필드 추가. 웹 게임의 특성상 의미 없는 일일 챌린지 시스템 완전 제거.

---

## 변경 사항

### 데이터 포맷 업그레이드 (VERSION 0x03 → 0x04)

**주요 변경:**
- recordDate 필드 추가 (랭킹 기록 날짜를 export/import 시 보존)
- customSeed 플래그 추가 (bit2 in flags byte)

**새로운 바이트 레이아웃 (480 bytes):**
```
Offset  Size   Field
------  -----  -----------------------
0       1      VERSION (0x04)
1       24     Badges bitmap (192 bits)
25      1      Rankings count (0-11)
26      363    Rankings records (11 × 33 bytes)  ← 확장 (29→33 bytes)
389     50     Catalog bitmap (400 bits)
439     25     Bestiary bitmap (200 bits)
464     16     Document bitmap (64 pages × 2 bits)
------  -----  -----------------------
Total:  480 bytes
```

**Ranking Record Layout (33 bytes):**
```
Byte    Field
----    ------------------
0       heroClass (0-3)
1       armorTier (0-6)
2       herolevel (1-30)
3       deathCauseId (0-255)
4       depth (1-26)
5       flags (bit0: win, bit1: ascending, bit2: customSeed)
6-7     progressScore (LE, 0-65535)
8-9     treasureScore (LE, 0-65535)
10-11   exploreScore (LE, 0-65535)
12      bossScore (/100, 0-255)
13      questScore (/100, 0-255)
14      multipliers (high nibble: win×10, low nibble: chal×4)
15-18   duration (int LE, game turn time)
19-26   dungeonSeed (long LE)
27-28   challenges (short LE, bit flags)
29-32   recordDate (int LE, epoch seconds)  ← 신규
```

### 일일 챌린지 시스템 완전 제거

**제거 이유:**
웹 게임 특성상 일일 챌린지가 의미 없음 - 다른 브라우저에서 실행하면 같은 일일 챌린지 시드로 무제한 도전 가능.

**삭제된 파일:**
- `WndDailies.java` - 일일 챌린지 기록 창

**제거된 코드:**
- `Rankings.java`: `latestDaily`, `latestDailyReplay`, `dailyScoreHistory`, `Record.daily` 필드
- `Dungeon.java`: `daily`, `dailyReplay` 플래그 및 관련 로직
- `GamesInProgress.java`: `Info.daily` 필드
- `SPDSettings.java`: `lastDaily()` 메서드
- `RankingsScene.java`: 일일 챌린지 버튼 및 UI
- `TitleScene.java`, `StartScene.java`, `HeroSelectScene.java`: 일일 플래그 리셋
- `WndHero.java`, `WndGameInProgress.java`: 일일 표시 코드
- `Bones.java`: 일일 체크 로직
- `WelcomeScene.java`: `latestDaily` 마이그레이션 코드
- `Icons.java`: `runTypeOfsX/Y` 일일 로직 단순화

### WndRanking 개선

**추가된 표시 항목:**
- 기록 날짜 (턴 수 위에 표시)
- 시드 고정 여부 (customSeed 있을 경우 "예" 표시)

**UI 변경:**
- WIDTH: 120 → 147 (다른 Scene과 통일)
- 표시 순서 변경: Date → Turns → Seed → Seed Fixed (턴 수를 날짜와 시드 사이로 이동)
- 시드 공유 시 URL은 클립보드에만 복사, 다이얼로그에는 URL 미표시

**버그 수정:**
- `startNewGameWithSeed()`: `Dungeon.hero = null`, `InterlevelScene.curTransition = null` 추가 (다시 플레이 오류 수정)

**추가된 메시지 키:**
```properties
# English
windows.wndranking.date=Date
windows.wndranking.seed_fixed=Seed Fixed
windows.wndranking.yes=Yes

# Korean
windows.wndranking.date=기록 날짜
windows.wndranking.seed_fixed=시드 고정
windows.wndranking.yes=예
windows.wndranking.turns=턴 수  # 턴 → 턴 수
```

### DataScene 개선

**UI 변경:**
- WIDTH: 140→147 (P), 200→250 (L) - 다른 Scene과 통일

**infoText 확장:**
- Guide, Alchemy, Lore 항목 추가
- 순서: Rankings → Badges → Catalog → Bestiary → Lore → Guide → Alchemy

**importHint 스타일 변경:**
- 폰트 크기: 5pt → 6pt
- 색상: 회색(0xAAAAAA) → 노란색(0xFFFF44)
- 줄바꿈: "가져오기:" 또는 "To import:" 다음에 개행 추가

**WndImportPreview 변경:**
- 버튼 레이아웃: 3개 버튼 가로 배치 → 세로 3줄 (각 버튼 전체 너비)
- previewText 순서: DataScene infoText와 동일 (Rankings → Badges → Catalog → Bestiary → Lore → Guide → Alchemy)
- Guide/Lore/Alchemy 분리 표시 (기존: "Guide/Lore: N pages" 통합)

**추가된 메시지 키:**
```properties
# English
scenes.datascene.guide=Guide: %d/%d
scenes.datascene.alchemy=Alchemy: %d/%d
scenes.datascene.lore=Lore: %d/%d

# Korean
scenes.datascene.guide=가이드: %d/%d
scenes.datascene.alchemy=연금술: %d/%d
scenes.datascene.lore=문서: %d/%d
```

### TitleScene 변경

- Test Level 버튼 주석 처리 (릴리스용)
- 중첩 주석 오류 수정 (`/* */` → `//` 스타일)

### 빌드 설정 변경

- 버전: `3.3.8-HTML5-INDEV` → `3.3.8-HTML5` (릴리스 빌드용)
- `TeaVMClassRegistry.java`: `WndDailies` 등록 제거

### 하위 호환성

- VERSION 0x01, 0x02, 0x03 import 계속 지원
- 새 URL은 VERSION 0x04로 export
- 이전 버전 import 시 recordDate는 현재 날짜로 설정

---

## 수정된 파일

| File | Changes |
|------|---------|
| `WebDataExporter.java` | VERSION 0x04, recordDate 필드, RECORD_SIZE 33 bytes |
| `WebDataImporter.java` | VERSION 0x04 파싱, recordDate import |
| `WebDataMerger.java` | Document merge/apply |
| `WebDataManager.java` | guideCount/alchemyCount/loreCount 필드 |
| `WebDataServiceImpl.java` | Preview 순서 변경 |
| `TeaVMClassRegistry.java` | WndDailies 등록 제거 |
| `Rankings.java` | daily 관련 필드/로직 제거 |
| `Dungeon.java` | daily/dailyReplay 플래그 제거 |
| `GamesInProgress.java` | Info.daily 필드 제거 |
| `SPDSettings.java` | lastDaily() 메서드 제거 |
| `Catalog.java` | reset() 메서드 추가 |
| `Bestiary.java` | reset() 메서드 추가 |
| `Document.java` | reset() 메서드 추가 |
| `DataScene.java` | WIDTH 변경, Guide/Alchemy/Lore 추가 |
| `TitleScene.java` | Test Level 버튼 주석 처리 |
| `RankingsScene.java` | 일일 챌린지 UI 제거 |
| `WndRanking.java` | WIDTH 147, startNewGameWithSeed 버그 수정 |
| `Icons.java` | runTypeOfsX/Y 단순화 |
| `Bones.java` | daily 체크 제거 |
| `WndDailies.java` | DELETED - 일일 챌린지 기록 창 |
| `windows*.properties` | date, seed_fixed, yes 키 추가 |
| `scenes*.properties` | guide, alchemy, lore 키 추가 |
| `build.gradle` | 버전 변경 |
| `NEVER-CHANGE-DATA-SYNC.md` | VERSION 0x04 문서화 |

---

## 데이터 항목 수 현황

| 항목 | 이전 제한 | 새 제한 | 실제 항목 수 |
|-----|---------|--------|------------|
| Badges | 192 | 192 | ~150 |
| Catalog | 400 | 400 | ~310 |
| Bestiary | 200 | 200 | ~143 |
| Document | 64 | 64 | 59 페이지 |
| Record Size | 29 bytes | 33 bytes | +4 bytes (date) |

## 버전 이력

| 버전 | 크기 | 설명 |
|------|------|------|
| 0x01 | 233 bytes | 초기 버전, 15 bytes/record |
| 0x02 | 387 bytes | duration, seed, challenges 추가, 29 bytes/record |
| 0x03 | 436 bytes | Catalog 확장, Document 추가, customSeed 플래그 |
| 0x04 | 480 bytes | recordDate 추가, 33 bytes/record |

---
