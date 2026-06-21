# 054. WndRanking 탭 시스템 및 데이터 Export V5

**날짜**: 2026-04-30

## 개요

WndRanking을 탭 기반 UI로 전면 개편하여 Stats, Talents, Items, Badges, Challenges 5개 탭 구성. 데이터 Export/Import를 VERSION 0x05로 업그레이드하여 per-run 배지, 특성, 아이템 정보 포함.

---

## 변경 사항

### WndRanking 탭 시스템 구현

**이전:**
- 단일 창에 점수 정보만 표시
- WIDTH=147, HEIGHT 가변

**이후:**
- WndTabbed 상속으로 5개 탭 구현 (Shattered PD 3.3.8과 동일)
- WIDTH=115, HEIGHT=144 (Shattered PD와 동일)

**탭 구성:**
1. **Stats Tab** (통계)
   - 점수 breakdown (Progress, Treasure, Exploration, Bosses, Quests)
   - 각 점수별 설명 표시
   - 승리/챌린지 배율, 최종 점수
   - Date, Turns, Seed 정보
   - Share Seed / Play Again 버튼

2. **Talents Tab** (특성)
   - 게임 데이터에서 선택된 특성 목록 표시
   - 특성 이름과 레벨 표시 (Tier별 그룹화)
   - 데이터 없을 시 "No data available" 메시지

3. **Items Tab** (아이템)
   - 장비한 아이템 목록 표시 (무기, 갑옷, 유물, 반지 등)
   - 아이템 아이콘, 이름, 강화 레벨 표시
   - 데이터 없을 시 "No data available" 메시지

4. **Badges Tab** (배지)
   - 해당 런에서 획득한 배지 목록
   - 배지 아이콘과 설명 표시
   - 배지 없을 시 "No badges earned" 메시지

5. **Challenges Tab** (도전 과제)
   - 활성화된 챌린지 목록 표시
   - 챌린지 아이콘과 이름 표시

### 데이터 Export/Import V5 (VERSION 0x05)

**새로운 바이트 레이아웃 (1096 bytes):**
```
Offset  Size    Field
------  ------  -----------------------
0       1       VERSION (0x05)
1       24      Badges bitmap (192 bits)
25      1       Rankings count (0-11)
26      979     Rankings records (11 × 89 bytes)
1005    50      Catalog bitmap (400 bits)
1055    25      Bestiary bitmap (200 bits)
1080    16      Document bitmap (64 pages × 2 bits)
------  ------  -----------------------
Total:  1096 bytes
```

**확장된 Ranking Record (89 bytes):**
```
Byte     Field
------   ------------------
0-32     Basic Record (기존 V4 형식)
33-56    Per-run Badges (24 bytes bitmap)
57-76    Talents (10 slots × 2 bytes)
77-88    Items (6 slots × 2 bytes)
```

**ItemRegistry 추가:**
- 아이템 클래스명 → 1바이트 ID 매핑
- ID 범위: 무기(1-49), 갑옷(70-89), 지팡이(90-119), 반지(120-149), 유물(150-179)

### Import Preview 정렬 수정

- "New High" 항목 앞의 불필요한 스페이스 제거
- `\n  New High:` → `\nNew High:`

### 점수 설명 단축

기존 긴 설명을 간결하게 변경:
```properties
windows.wndranking.progress_desc=Depth × Level
windows.wndranking.treasure_desc=Gold + Items
windows.wndranking.explore_desc=Floors explored
windows.wndranking.boss_desc=Bosses defeated
windows.wndranking.quest_desc=Quests completed
```

### 번역 검수

22개 언어에 대해 새로운 WndRanking 관련 키 추가:
- `no_data`, `no_badges` 등 신규 키 번역
- 점수 설명 단축형 번역

---

## 수정된 파일

| File | Changes |
|------|---------|
| `WndRanking.java` | WndTabbed 기반 5탭 구조로 전면 재작성 |
| `windows.properties` | 점수 설명 단축, no_data/no_badges 키 추가 |
| `windows_*.properties` | 22개 언어 신규 키 번역 |
| `WebDataExporter.java` | VERSION 0x05, 확장 레코드 (89 bytes) |
| `WebDataImporter.java` | VERSION 0x05 파싱, ExtendedRecordData |
| `WebDataServiceImpl.java` | "New High" 정렬 수정 |
| `ItemRegistry.java` | NEW - 아이템 ID 매핑 레지스트리 |
| `NEVER-CHANGE-DATA-SYNC.md` | VERSION 0x05 문서화 |

---

## URL 크기 비교

| 버전 | Raw Bytes | 압축 후 (예상) |
|------|-----------|--------------|
| V4 | 480 bytes | ~300 bytes |
| V5 | 1096 bytes | ~600 bytes |

압축 후 URL 길이 약 600자로 대부분의 메신저/SNS에서 공유 가능한 범위.

## 하위 호환성

- **Import**: VERSION 0x01~0x05 모두 지원
- **Export**: VERSION 0x05로 통일

---
