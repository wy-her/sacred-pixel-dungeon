# 052. WndRanking 개선 및 시드 공유 기능

**날짜**: 2026-04-22

## 개요

WndRanking 창을 간소화하고 점수 세부사항을 표시하도록 개선. 시드 공유 기능 추가하여 URL 형식으로 시드, 직업, 도전 설정 공유 가능.

---

## 변경 사항

### WndRanking 점수 표시 개선

### 점수 세부사항 표시
- **진행도 (Progression)**: 최대 도달 층수와 영웅의 레벨에 기반
- **보물 (Treasure)**: 수집한 골드와 소유한 아이템 가치에 기반
- **탐험 (Exploration)**: 탐험한 층 기준, 아이템/숨겨진 문/퍼즐 놓치면 감점
- **보스 처치 (Bosses)**: 처치한 보스 수 기준, 피할 수 있는 공격 맞으면 감점
- **퀘스트 (Quests)**: 완료한 퀘스트 수 기준

### 점수 계산 표시
```
기본 점수: 113,800
× 승리 배율: 2.0
× 도전 배율: 1.95
─────────────────
최종 점수: 125,430
```

### 결과 표시 (종류별)
- **승리**: "Victory! Obtained the Amulet of Yendor." (녹색)
- **귀환 완료**: "Ascended! Escaped the dungeon with the Amulet." (녹색)
- **사망**: "Died on floor X. Killed by Y." (빨간색)
- **귀환 중 사망**: "Died on floor X while ascending. Killed by Y." (빨간색)

### 추가 정보
- 게임 시간 (H:MM:SS 형식)
- 던전 시드 (ABC-DEF-GHI 형식)

---

### 시드 공유 기능

### "시드 공유" 버튼
클릭 시 시드 URL을 클립보드에 복사 (다이얼로그에는 URL 미표시, 복사 완료 메시지만 표시)

**URL 형식:**
```
https://sacredpixel.net/?seed=ABC-DEF-GHI&class=warrior&challenges=15
```

### 시드 URL 파싱 (수신 측)
브라우저에서 시드 URL 접속 시:
1. URL 쿼리 파라미터 파싱 (seed, class, challenges)
2. 확인 다이얼로그 표시:
   - "이 시드로 새 게임을 시작할까요?"
   - 시드: ABC-DEF-GHI
   - 직업: 전사
   - 도전: 3
3. "게임 시작" 클릭 시 해당 설정으로 새 게임 시작

---

### "다시 플레이" 버튼

클릭 시:
1. 빈 저장 슬롯 찾기
2. 동일한 시드, 직업, 도전 항목으로 새 게임 시작
3. 슬롯 없으면 "빈 저장 슬롯이 없습니다" 메시지

---

---

## 수정된 파일

| File | Changes |
|------|---------|
| `core/.../windows/WndRanking.java` | 전면 재작성, 점수 세부사항 및 시드 공유 |
| `core/.../scenes/TitleScene.java` | 시드 URL 파싱 추가 |
| `core/.../scenes/DataScene.java` | DataService 시드 메서드 추가 |
| `teavm/.../web/WebUrlCodec.java` | 시드 URL 파라미터 메서드 |
| `teavm/.../web/WebDataServiceImpl.java` | 시드 메서드 구현 |
| `windows.properties` | 점수 및 시드 관련 키 추가 |
| `windows_ko.properties` | 한국어 번역 추가 |
| `scenes.properties` | 시드 URL 관련 키 추가 |
| `scenes_ko.properties` | 한국어 번역 추가 |

---

### 추가 메시지 키

### WndRanking (windows.properties)
```properties
windows.wndranking.base_score=Base Score
windows.wndranking.final_score=Final Score
windows.wndranking.result_won=Victory! Obtained the Amulet of Yendor.
windows.wndranking.result_ascended=Ascended! Escaped the dungeon with the Amulet.
windows.wndranking.result_died=Died on floor %1$d. Killed by %2$s.
windows.wndranking.result_died_ascending=Died on floor %1$d while ascending. Killed by %2$s.
windows.wndranking.cause_unknown=unknown
windows.wndranking.date=Date
windows.wndranking.seed_fixed=Seed Fixed
windows.wndranking.yes=Yes
```

### WndRanking (windows_ko.properties)
```properties
windows.wndranking.date=기록 날짜
windows.wndranking.seed_fixed=시드 고정
windows.wndranking.yes=예
```

### TitleScene (scenes.properties)
```properties
scenes.titlescene.seed_url_title=Seed URL Detected
scenes.titlescene.seed_url_found=Start a new game with this seed?\n\nSeed: %1$s\nClass: %2$s\nChallenges: %3$s
scenes.titlescene.seed_url_start=Start Game
scenes.titlescene.seed_url_cancel=Cancel
scenes.titlescene.seed_url_no_slot=No empty save slot available.
```

---

### Export/Import 형식 확장 (VERSION 0x04)

### 변경 내용
WndRanking에 표시되는 모든 데이터를 export/import에 포함하도록 Record 형식 확장.

### 최신 Record 형식 (33 바이트, VERSION 0x04)
```
Offset  Size   Field
------  -----  -----------------------
0       1      heroClass (0-3)
1       1      armorTier (0-6)
2       1      herolevel (1-30)
3       1      deathCauseId (0-255)
4       1      depth (1-26)
5       1      flags (bit0: win, bit1: ascending, bit2: customSeed)
6       2      progressScore (LE, 0-65535)
8       2      treasureScore (LE, 0-65535)
10      2      exploreScore (LE, 0-65535)
12      1      bossScore (/100, 0-255)
13      1      questScore (/100, 0-255)
14      1      multipliers (high nibble: win×10, low nibble: chal×4)
15      4      duration (int, game turn time)
19      8      dungeonSeed (long, LE)
27      2      challenges (short, bit flags, LE)
29      4      recordDate (int, epoch seconds, LE)  ← VERSION 0x04 신규
------  -----  -----------------------
Total:  33 bytes
```

### 전체 Export 크기
- VERSION 0x01: 233 바이트
- VERSION 0x02: 387 바이트
- VERSION 0x03: 436 바이트
- VERSION 0x04: 480 바이트 (최신)

### 하위 호환성
- Import 시 VERSION 0x01, 0x02, 0x03, 0x04 모두 지원
- 이전 버전 import 시 해당 버전에 없는 필드는 기본값으로 설정

### 추가된 Record 필드
- `duration` (float): 게임 턴 시간 (Statistics.duration에서 복사)
- `dungeonSeed` (long): 던전 시드 (Rankings.dat에 저장)
- `challenges` (int): 도전 항목 비트 플래그 (Rankings.dat에 저장)

### 변경된 파일
- `core/src/main/java/com/sacredpixel/sacredpixeldungeon/Rankings.java`
  - Record 클래스에 `duration` 필드 추가
  - submit()에서 Statistics.duration 복사
  - storeInBundle/restoreFromBundle에서 duration 저장/로드
- `teavm/src/main/java/com/sacredpixel/sacredpixeldungeon/teavm/web/WebDataExporter.java`
  - VERSION 0x01 → 0x02
  - RECORD_SIZE 15 → 29
  - encodeRecord()에서 duration/dungeonSeed/challenges 인코딩
- `teavm/src/main/java/com/sacredpixel/sacredpixeldungeon/teavm/web/WebDataImporter.java`
  - VERSION 0x01, 0x02 모두 지원
  - parseRecord()에서 버전에 따라 새 필드 파싱

---

### WndRanking 표시 순서 변경

### 변경 내용
- 게임 시간을 H:MM:SS 형식에서 턴 숫자로 변경 (예: `12,345`)
- 던전 시드와 턴을 맨 위로 이동 (영웅 정보 바로 아래)
- 그 다음 점수 세부사항 표시

### 표시 순서
1. 영웅 아바타 및 타이틀
2. 기록 날짜 (NEW)
3. 던전 시드, 시드 고정 여부 (NEW), 턴
4. 점수 세부사항 (진행도, 보물, 탐험, 보스, 퀘스트)
5. 기본 점수, 배율, 최종 점수
6. 결과 (승리/사망)
7. 버튼 (시드 공유, 다시 플레이)

### 메시지 키 변경
- `windows.wndranking.duration` → `windows.wndranking.turns`
- 영어: "Duration" → "Turns"
- 한국어: "게임 시간" → "턴"
