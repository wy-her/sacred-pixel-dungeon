# 026. UI 대규모 정비, 턴 시스템 수정, 테스트 레벨 개편

**날짜**: 2026-03-28 ~ 2026-03-29

## 개요

창 가로길이 통일, 폰트 크기 통일, RedButton 전면 정비, statSlot 레이아웃 통일, 턴 시스템 수정, 테스트 레벨 개편.

---

## 변경 사항

### 1. 창 가로길이 통일

| 창 | 세로모드 | 가로모드 |
|----|---------|---------|
| WndSettings | 137 → **147** | 250 (유지) |
| WndJournal | 141 → **147** | 242 → **250** |
| JournalScene (타이틀 저널) | 126 → **147** | 216 → **250** |

---

## 2. 폰트 크기 통일

### 모든 폰트 크기 9+ → 8
- WndBadge 배지 제목: 9 → 8
- WndInfoMob 몬스터 이름: 9 → 8
- ChangeInfo 변경로그 제목: 9 → 8
- InterlevelScene 로딩 텍스트: 9 → 8

### 버튼 폰트 기본값
- RedButton 기본: 9 → **7**
- StyledButton 기본: 9 → **8**

### 제목 서식 통일 (size 8, TITLE_COLOR, 가운데 정렬)
- WndSettings 5개 탭 제목
- WndChallenges "도전" 제목 (12→8)
- WndDailies "일일 도전" 제목 (아이콘 제거 + size 8)
- WndRandomize "랜덤화" 제목 (신규 추가)
- HeroSelectScene "영웅을 선택하십시오" (11→8)
- WndOptions 제목: 좌정렬 → **가운데 정렬** 수정

### 제목 여백 통일
- 제목 위쪽: **2px**
- 제목 아래쪽: WndSettings **title.bottom()+2**, WndChallenges/WndRandomize **+4px**

---

## 3. RedButton 전면 정비

### 아이콘 제거
- WndGame: 설정/도전/시작/랭킹/메뉴 5개 아이콘 제거
- WndGameInProgress: 도전과제/계속/지우기 아이콘 제거

### 높이 통일 → 16px
- WndGame BTN_HEIGHT: 20 → **16**
- WndGameInProgress 버튼: 20/18 → **16**
- WndOptions BUTTON_HEIGHT: 18 → **16**

### 글자크기 자동조정 (RedButton.java)
- `sizeAdjusted` → `adjustedAtWidth` 기반으로 변경
- width가 변경되면 initSize로 리셋 후 재평가
- 기본 7 → 초과 시 6 → 초과 시 5 (최소 initSize-2 또는 5)
- 기본 6 → 초과 시 5 → 초과 시 4
- **동적 크기 버튼 예외**: WndUseItem, WndGameInProgress 도전 버튼은 `multiline=true`로 auto-shrink 비활성화

### multiline 버튼 개선
- 한국어/일본어/중국어: `maxWidth()` 미적용 → 줄바꿈 방지
- 2줄 텍스트 세로 가운데: +2px offset 보정
- WndUseItem 버튼 배치: 복잡한 확장 알고리즘 → **균등 너비 분배**

### WndUseItem 폰트 크기
- 아이템 액션 버튼: 8 → **7** (기본값)

---

## 4. statSlot 2열 레이아웃 통일

### 비율: 67% / 33%
- WndHero: 55% → 65% → 70% → **67%**
- WndGameInProgress: 55% → **67%**
- WndRanking: 55% → **67%**
- WndScoreBreakdown: 70% → **67%**

### 폰트 크기: 라벨 7, 값 7
- WndHero 값 폰트: 8 → **7**
- WndGameInProgress 값 폰트: 8 → **7**

### 천단위 구분기호
- `statSlot(String, int)` 오버로드: `Integer.toString()` → `DateCompat.formatNumber()`
- TeaVM의 `DateCompat.formatNumber()` 구현: `String.valueOf()` → **실제 천단위 구분 로직**
  - 영어/한국어/일본어/중국어: `,` (예: 10,000)
  - 독일어/프랑스어/러시아어 등: `.` (예: 10.000)

---

## 5. WndHeroInfo TalentsPane 높이 최적화

- TalentsPane 높이: 하드코딩 120px → **실제 콘텐츠 높이**
- `talentPane.setRect(..., 0)` → `setSize(width, content().height())`

---

## 6. TalentsPane 구분선 제거

- Tier 간 검은 구분선: `separators.get(i).visible = false`
- 여백(top += 3) 유지

---

## 7. 저널 탭 제목 및 여백

### 제목 여백 통일
- GridHeader centered (도감 대분류): 위 2px, 아래 2px
- GridHeader non-centered (소분류): 위 2px, 아래 2px
- ListTitle (가이드 제목): 위 2px, 아래 2px, 검은 선 제거
- BadgesTab "당신이 획득한 배지": 위 2px

### 도감 문서 가로모드
- `allowHorizontalGrouping` 플래그 추가
- 문서(Lore) 섹션: `false` → 소제목 항상 줄바꿈

### 모험노트
- MIN_GROUP_SIZE: 3 → **4** (아이콘 4개분 공간)
- 세로 구분선 좌측 여백 +2px

### 연금술 버튼/아이콘
- 카테고리 9개 버튼: 세로/가로 모두 1줄 배치
- 아이콘 scale: 0.75f (연금술 + 도감)
- 레시피 줄당 개수: 항상 WIDTH_P(147) 기준

---

## 8. 설정 창 정비

### 가로 구분선 모두 숨김
- DisplayTab/UITab/InputTab/AudioTab/LangsTab: 모든 sep `visible = false`
- UITab `sep1.visible = hasSliders` → `false`

### 툴바 설정
- "툴바 모드" 글자: 8 → **7**
- 모드 버튼 높이: BTN_HEIGHT-2 → **BTN_HEIGHT**
- 모드 버튼 위 여백: GAP → **2*GAP**

### 언어 탭
- Transifex 위 여백: 2px → **4*GAP**
- 언어 목록 순서: 브라우저 기본언어 우선 → **고정 순서** (EN→ES→KO→JA→ZH→...)
- 언어 버튼 너비: `Math.floor()` → **균등 분배** (오른쪽 여백 제거)

---

## 9. 영웅 선택 화면

### fadeout 효과 제거
- `uiAlpha -= Game.elapsed/4f` 삭제

### 시드고정 항목 삭제
- seedButton 블록 전체 제거

### "영웅을 선택하십시오" 위치
- offset: -15 → **-20** (5px 위로)

### 게임 시작 시 customSeed 초기화
- `SPDSettings.customSeed("")` 추가

### 일일도전 아이콘 제거
- WndOptions 아이콘 파라미터 제거
- nowin 안내: WndTitledMessage → WndMessage

### 랜덤화 슬라이더
- 높이: 22 → **28** (WndSettings 스타일)

---

## 10. WndRanking 개편

### [i] 버튼 위치
- 기존: 점수 옆 (`WIDTH - width, pos - 10 - GAP`)
- 변경: 타이틀 옆 (`title.right(), 0`) — WndHero와 동일

### 시드 복사 버튼 → "다시 플레이하기"
- 클릭 시 안내창: 같은 시드 + 같은 영웅으로 다시 플레이
- 경고 문구 (노란색 강조): 배지 미획득, 기록 미반영, 랭킹 최하단
- "다시 플레이하기" / "취소" 버튼
- 실행: customSeed 설정 → 동일 영웅 → InterlevelScene DESCEND

---

## 11. 전 언어 띄어쓰기 축소

- RenderedTextBlock: Western 스페이스 공식을 Asian과 동일하게
- `splitSpaces = true` (항상 분리)
- `scaledSpaceW = charGap - borderExcess` (모든 언어)

---

## 12. WndKeyBindings Enter/ESC 지원

- Enter: 확인 (save + hide)
- ESC/Back: 취소 (hide)
- `onSignal()` 오버라이드 추가

---

## 13. 턴 시스템 수정 (진행 중)

### Hero.enemyCanAct 로직 변경
- `isWaitingForCallback()` 체크 **제거** → `cooldown() < 0`만 체크
- Pushing 체크: 모든 Pushing → **blocking Pushing만** (`isBlockingVfx()`)
- `Pushing.isBlockingVfx()` getter 추가

### 리퍼데몬 (미해결)
- 도약 착지점에 영웅이 있을 때 커맨드 대기 발생
- 의도: 바로 pushing → 공격

### 경비원 (미해결)
- 2턴 연속 공격 + 1턴 공격 안 함 패턴
- 의도: 1턴 1공격 (attackDelay = 1.0)

---

## 14. 테스트 레벨 개편

### 맵
- 45x45 복잡한 Vault Heist → **32x32 단일 30x30 방**

### 몹 (4코너, SLEEPING)
- Spinner → **Guard** (경비원)
- RipperDemon (유지)
- Warlock → **Shaman.RedShaman** (놀 주술사)
- Eye → **Necromancer** (사령술사)

### NPC
- 지팡이깎는노인/대장장이/임프/상인/유령 → **모두 제거**

### 영웅 설정
- HP: 999, STR: 20
- 장비: 특대검+6, 판금갑옷+6
- 유물: 전 종류 11개
- 골드: 10,000

### 영웅 선택
- 타이틀 "Test Zone" → **"Test Level"**
- 클래스 선택 다이얼로그 (6개 클래스)

### VaultLevel HP
- 1000 → **999**

---

## 15. 번역 및 텍스트

### 한국어 변경
- "양조물과 영약" → **"혼합물과 영약"**
- "도전" → **"도전 항목"** (인게임 메뉴)

### 독일어 변경
- "Verbrauchsgegenstände" → **"Verbrauchbares"** (도감 소모품)

### 전 언어 번역 완성
- 21개 언어 vaultlaser.hit 추가
- be(88키)/el(20키)/de/es/it/nl/pl/ru/uk/sv 미번역 값 번역

---

## 수정 파일 목록 (주요)

### Java
- `ui/RedButton.java` — 기본 크기 7, 자동조정 로직
- `ui/StyledButton.java` — 기본 크기 8, multiline Asian 예외, 2줄 세로 보정
- `ui/RenderedTextBlock.java` — 전 언어 Asian 스페이스 적용
- `ui/ScrollingGridPane.java` — allowHorizontalGrouping, MIN_GROUP_SIZE 4, 여백 통일
- `ui/ScrollingListPane.java` — ListTitle 검은선 제거, 여백 통일
- `ui/OptionSlider.java` — maxTxt 오른쪽 정렬
- `ui/TalentsPane.java` — 구분선 숨김
- `windows/WndSettings.java` — 구분선 숨김, 제목 여백, 언어 순서/너비
- `windows/WndJournal.java` — 가로길이, 아이콘 축소, 뱃지 버튼, 제목 여백
- `windows/WndHero.java` — statSlot 67%/33%, formatNumber
- `windows/WndHeroInfo.java` — TalentsPane 높이 최적화
- `windows/WndGameInProgress.java` — statSlot 67%/33%, 버튼 정비
- `windows/WndRanking.java` — statSlot 67%/33%, 다시 플레이하기, [i] 위치
- `windows/WndScoreBreakdown.java` — 67%
- `windows/WndOptions.java` — 제목 가운데 정렬, BUTTON_HEIGHT 16
- `windows/WndChallenges.java` — 제목 size 8, 여백
- `windows/WndDailies.java` — 제목 가운데 정렬, 아이콘 제거
- `windows/WndBadge.java` — 제목 size 8
- `windows/WndInfoMob.java` — 이름 size 8
- `windows/WndGame.java` — 아이콘 제거, BTN_HEIGHT 16
- `windows/WndUseItem.java` — 폰트 7, 균등 너비, multiline
- `windows/WndKeyBindings.java` — Enter/ESC 지원
- `scenes/TitleScene.java` — 버튼 +2px
- `scenes/HeroSelectScene.java` — fadeout 제거, 시드 삭제, 제목 위치
- `scenes/InterlevelScene.java` — VaultLevel HP 999, 로딩 텍스트 8
- `scenes/GameScene.java` — restart/menu 버튼 size 8
- `scenes/JournalScene.java` — 가로길이 통일
- `actors/hero/Hero.java` — enemyCanAct 수정
- `actors/mobs/RipperDemon.java` — 바운스 Pushing 수정
- `effects/Pushing.java` — isBlockingVfx() getter
- `actors/mobs/npcs/*.java` — Quest 필드 public
- `ui/changelist/ChangeInfo.java` — 제목 size 8

### Properties
- 전 언어 번역 파일 다수
- `teavm/DateCompat.java` — formatNumber 천단위 구현
