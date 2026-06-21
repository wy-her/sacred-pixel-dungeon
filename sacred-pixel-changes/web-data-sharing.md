# 웹 데이터 공유

**날짜**: 2026-06-04

## 개요

Sacred Pixel Dungeon 웹 빌드에 URL 기반 데이터 동기화를 추가하여 배지, 랭킹, 저널 데이터를 기기 간에 내보내고 가져올 수 있습니다.

**공식 웹사이트**: `https://sacredpixel.net/`

---

## 변경 사항

### 1. URL 기반 데이터 동기화

#### 1.1 구현된 기능

| 기능 | 설명 |
|------|------|
| **내보내기** | 현재 기기의 배지/랭킹/저널을 압축하여 URL 생성 |
| **가져오기** | URL에서 데이터를 추출하여 병합 또는 덮어쓰기 |
| **URL 자동 감지** | `#d=` 파라미터가 있으면 자동으로 Import 다이얼로그 표시 |
| **미리보기** | 가져오기 전 어떤 데이터가 추가/변경되는지 표시 |

#### 1.2 URL 형식

게임 데이터를 압축 및 인코딩하여 URL로 공유할 수 있습니다.

#### 1.3 병합 정책

- **MERGE_UNION (기본)**:
  - 배지: OR 연산 (양쪽 모두의 배지 획득)
  - 랭킹: 합친 후 점수순 Top-6 (gameID로 중복 제거, Top 5 + 최근 1)
  - 카탈로그/베스티어리: OR 연산 (양쪽 모두의 발견 기록)
  - 도큐먼트: OR 연산, 상태는 MAX (NOT_FOUND < FOUND < READ)

- **OVERWRITE**:
  - 배지/랭킹: 초기화 후 가져온 데이터 적용
  - 카탈로그: `Catalog.reset()` 호출 후 적용
  - 베스티어리: `Bestiary.reset()` 호출 후 적용
  - 도큐먼트: `Document.reset()` 호출 후 적용

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|---------|
| `core/src/main/java/com/sacredpixel/sacredpixeldungeon/Rankings.java` | Record 클래스에 점수 분해 필드, dungeonSeed, 게임 통계 필드 추가 |
| `core/src/main/java/com/sacredpixel/sacredpixeldungeon/windows/WndRanking.java` | Shattered PD 스타일 StatsTab, HEIGHT=144 |
| `core/src/main/java/com/sacredpixel/sacredpixeldungeon/scenes/TitleScene.java` | Data 버튼 추가, Test Level 주석 처리 |
| `core/src/main/java/com/sacredpixel/sacredpixeldungeon/scenes/DataScene.java` | Delete All Data 버튼 추가, 2단계 확인 다이얼로그 |
| `teavm/src/main/java/.../teavm/TeaVMLauncher.java` | WebDataServiceImpl 연결, URL 해시 파라미터 처리 |
| `teavm/src/main/java/.../teavm/web/WebDataExporter.java` | VERSION 0x09, JSON 기반, 3종 최적화, MAX_RANKINGS=6 |
| `teavm/src/main/java/.../teavm/web/WebDataImporter.java` | VERSION 0x09 파싱, JSON 복원, v0x01~v0x06 하위 호환 |
| `teavm/src/main/java/.../teavm/web/WebDataMerger.java` | Top-6 정렬 (점수순, gameID 중복 제거) |
| `teavm/src/main/java/.../teavm/web/WebDataManager.java` | clearAllData() 메서드 추가 (spd_ localStorage + spdDB 삭제) |
| `teavm/src/main/java/.../teavm/web/ItemRegistry.java` | Potions, Scrolls, Seeds, Trinkets ID 등록 |
| `teavm/webapp/index.html` | pako.js 스크립트 로드 |
| `teavm/build.gradle` | pako.min.js 빌드 출력 복사 |

### 신규 파일

**teavm module** (`teavm/src/main/java/.../teavm/web/`):
```
+-- DeathCauseRegistry.java   # 죽음 원인 ID 매핑 (~120 클래스)
+-- ItemRegistry.java         # 아이템 ID 매핑 (v0x06+)
+-- WebDataExporter.java      # 데이터 -> 바이트 직렬화
+-- WebDataImporter.java      # 바이트 -> 데이터 역직렬화
+-- WebDataMerger.java        # 병합 로직 (OR/MAX/Top-6)
+-- WebCompressor.java        # pako.js DEFLATE 래퍼
+-- WebUrlCodec.java          # URL-safe Base64 인코딩
+-- WebDataManager.java       # UI 통합 진입점 + clearAllData()
+-- WebDataServiceImpl.java   # DataScene 브릿지
```

**core module**:
```
core/src/main/java/.../scenes/
+-- DataScene.java            # 데이터 동기화 씬
```

**webapp**:
```
teavm/webapp/scripts/
+-- pako.min.js               # DEFLATE 압축 라이브러리 (46KB)
```

---

## Rankings.Record 필드 변경

### 추가된 필드

```java
// 점수 분해 (URL 내보내기용)
public int progressScore;
public int treasureScore;
public int exploreScore;
public int totalBossScore;
public int totalQuestScore;
public float winMultiplier;
public float chalMultiplier;
public int challenges;

// 시드 (Replay/공유용)
public long dungeonSeed;

// 기록 날짜 (VERSION 0x04에서 export/import)
public String date;  // 표시용 문자열, export시 epoch seconds로 변환

// 게임 통계 (VERSION 0x06에서 export/import)
public int heroSTR;           // 기본 힘 (10-30)
public int strBonus;          // 힘 보너스 (signed)
public int enemiesSlain;      // 처치한 적 수
public int goldCollected;     // 수집한 골드
public int foodEaten;         // 먹은 음식 수
public int itemsCrafted;      // 제작한 아이템 수
```

### 하위 호환성

- `restoreFromBundle()`: 기존 데이터에 신규 필드가 없으면 `contains()` 체크로 기본값 사용
- `hasScoreBreakdown()`: 점수 분해 데이터 존재 여부 확인
- `loadGameData()`: 구버전 gameData에서 신규 필드 마이그레이션

---

## UI 변경

### TitleScene 레이아웃

```
모험을 시작하기  (전체 너비)
랭킹  |  일지
데이터  |  설정
소개  (전체 너비)
```

*참고: Test Level 버튼은 릴리스용으로 주석 처리됨*

### DataScene

**창 너비**: WIDTH_P=147, WIDTH_L=250 (SettingsScene/JournalScene과 동일)

```
+------------------------------------------+
| [<-] 데이터                               |
+------------------------------------------+
|                                          |
|   순위: 8개 기록 (최고 123,456점)         |
|                                          |
|   뱃지: 45/192 획득                       |
|                                          |
|   카탈로그: 250/400                       |
|                                          |
|   도감: 89/200                            |
|                                          |
|   문서: 25/30                             |
|                                          |
|   가이드: 7/14                            |
|                                          |
|   연금술: 5/9                             |
|                                          |
|         [내보내기]                        |
|      [모든 데이터 삭제]                    |
|                                          |
|   가져오기:                               |
|   클립보드에 복사된 URL을 브라우저        |
|   주소창에 직접 붙여넣고 Enter를 누르세요  |
|                                          |
+------------------------------------------+
```

### WndRanking 표시 순서 (Shattered PD 스타일)

**창 크기**: WIDTH=147, HEIGHT=144

```
+-------------------------------------+
| [영웅 아바타] LEVEL 30 WARRIOR      |
+-------------------------------------+
| 2026-05-06                  v3.1.0  |
+-------------------------------------+
| Score                      443,820  | [i]
| Strength                    14 + 2  |
| Game Duration               12,345  |
| Deepest Floor                   26  |
| Dungeon Seed           ABC-DEF-GHI  |
+-------------------------------------+
| Enemies Slain                  523  |
| Gold Collected              12,456  |
| Food Eaten                      18  |
| Items Crafted                   25  |
+-------------------------------------+
```

**info 버튼**: 클릭 시 WndScoreBreakdown 팝업 (점수 분해 표시)

---

## 빌드 설정 (build.gradle)

pako.min.js는 4개 빌드 태스크 모두에 복사 설정 필요:

```groovy
// buildRelease, buildDebug, runRelease, runDebug 각각에 추가
copy {
    from 'webapp/scripts/pako.min.js'
    into "${layout.buildDirectory.get().asFile}/dist/webapp/scripts"
}
```

**주의**: pako.js가 복사되지 않으면 Export 시 `pako not available` 오류 발생

---

## 알려진 문제 및 해결책

| 문제 | 원인 | 해결책 |
|------|------|------|
| `pako not available` | pako.min.js 미복사 | build.gradle에 복사 태스크 추가 |
| Import 후 데이터 미반영 | 캐시된 데이터 사용 | Rankings/Badges reload() 호출 |
| URL 해시 인식 안됨 | 브라우저 캐시 | 하드 리로드 (Ctrl+Shift+R) |

---

## 추가된 메시지 키

### WndRanking (windows.properties / windows_ko.properties)
```properties
# English
windows.wndranking.date=Date
windows.wndranking.turns=Turns

# Korean
windows.wndranking.date=기록 날짜
windows.wndranking.turns=턴 수
```

### DataScene (scenes.properties / scenes_ko.properties)
```properties
# English
scenes.datascene.guide=Guide: %d/%d
scenes.datascene.alchemy=Alchemy: %d/%d
scenes.datascene.lore=Lore: %d/%d
scenes.datascene.import_hint=To import:\nPaste the copied URL...

# Korean
scenes.datascene.guide=가이드: %d/%d
scenes.datascene.alchemy=연금술: %d/%d
scenes.datascene.lore=문서: %d/%d
scenes.datascene.import_hint=가져오기:\n클립보드에 복사된 URL을...
```

---

*버전: 3.3 (v0x09 JSON 기반 데이터 형식, 3종 최적화, TABLE_SIZE=6)*
