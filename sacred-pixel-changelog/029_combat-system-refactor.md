# 029. 전투 시스템 리팩터링 및 UI 레이아웃 통일

**날짜**: 2026-03-29

---

## 1. 전투 시스템 — 공격 커맨드 무한 대기 수정

### 문제
적 공격 커맨드를 연속 입력하거나, 몹이 영웅을 공격하는 타이밍에 영웅이 행동 중이면 턴 인디케이터가 무한히 회전하며 게임이 멈춤.

### 원인
`Mob.doAttack()`에서 `Dungeon.hero.interrupt()`를 호출하면 영웅의 `sprite.idle()`이 실행되어 진행 중인 공격 애니메이션을 덮어씀. `onAttackComplete()` 콜백이 유실되어 `waitingForCallback = true`, `ready = false` 상태가 영구 지속.

### 수정 (2개)

**Hero.interrupt() — 콜백 유실 즉시 복구**
```java
public void interrupt() {
    // ... 기존 로직 ...
    if (waitingForCallback) {
        waitingForCallback = false;  // 콜백이 유실되므로 즉시 해제
    }
}
```
**파일:** `actors/hero/Hero.java`

**Actor.process() — 3초 안전장치**
```java
if (hero.waitingForCallback && !hero.ready) {
    heroStuckFrames++;
    if (heroStuckFrames > 180) {  // ~3초
        hero.waitingForCallback = false;
        heroStuckFrames = 0;
    }
}
```
**파일:** `actors/Actor.java`

---

## 2. 전투 시스템 — 공격 버튼 연속 클릭 방지

### 문제
AttackIndicator (F키/공격 아이콘)를 빠르게 연속 클릭하면 턴 인디케이터 무한 회전.

### 수정
`AttackIndicator.onClick()` → 원본 SPD 3.3.8과 동일하게 복원 (`hero.handle()` + `hero.next()`) + 클릭 후 `enable(false)` 설정하여 `hero.ready` 시까지 재클릭 차단.

**파일:** `ui/AttackIndicator.java`

---

## 3. 전투 시스템 — 적 애니메이션 버스트 수정

### 문제
영웅이 "행동 계속하기"(resume)로 연속 이동 중 적의 공격을 받아도 이동이 멈추지 않음. 영웅이 멀리 이동한 후 적이 한번에 여러 칸을 빠르게 따라오는 "미끄러짐" 현상 발생.

### 원인
`Hero.resume()`에서 `damageInterrupt = false`로 설정하여, 행동 계속하기 중에는 적 공격에 의한 이동 중단(`interrupt()`)이 비활성화됨. 원본 SPD에서도 동일한 코드이나, Desktop의 별도 Actor 스레드로 인해 문제가 보이지 않음.

### 수정
```java
public void resume() {
    curAction = lastAction;
    lastAction = null;
    damageInterrupt = true;  // false → true: 행동 계속하기 중에도 적 공격 시 이동 중단
    next();
}
```
**파일:** `actors/hero/Hero.java`

---

## 4. 탐색 포커싱 잔류 수정

### 문제
탐색 버튼을 2번 눌러 주변 탐색 후 타일 포커싱 이펙트(키보드 커서)가 맵에 계속 남아있음.

### 수정
`informer.onSelect(null)` → `GameScene.cancel()` 변경 — 키보드 커서 + 셀 선택 상태를 정상 초기화.

**파일:** `ui/Toolbar.java`

---

## 5. 계단 키보드 이동

### 추가 기능
`WAIT_OR_PICKUP` 키(S/Numpad5)로 계단 타일에서 층 이동 가능.

### 동작 우선순위
아이템 획득 > 계단 이동 > 대기

### 조작키 설정 텍스트
- EN: "Wait / Stairs / Pick Up"
- KO: "대기 / 계단 이동 / 줍기"
- 21개 언어 번역 완료

**파일:** `ui/Toolbar.java`, `SPDAction.java`, `windows/windows_*.properties`

---

## 6. 시드 고정 아이콘 오류 수정

### 문제
"다시 플레이하기"로 시드 고정 게임 시작 후, 타이틀로 돌아와 새 게임을 시작하면 영웅 선택 화면의 게임 설정 아이콘이 녹색(시드 고정)으로 표시됨.

### 수정
`HeroSelectScene.create()`에서 `SPDSettings.customSeed("")` 호출하여 시드 초기화.

**파일:** `scenes/HeroSelectScene.java`

---

## 7. "다시 플레이하기" 개선

### 슬롯 만석 안내
6개 슬롯이 모두 찬 경우 안내 메시지 표시, 게임 미시작.
- KO: "동시 진행 가능한 게임의 개수는 최대 6개이며, 이미 6개의 게임이 진행 중입니다. 다시 플레이하려면 진행 중 게임을 하나 이상 삭제하세요."
- 22개 언어 번역 완료

### 도전항목 적용
원본 게임의 도전항목(`Dungeon.challenges`)을 그대로 적용하여 다시 플레이.

**파일:** `windows/WndRanking.java`, `windows/windows_*.properties`

---

## 8. WndRegionComplete — 지역 완료 통계 창

### 새 기능
보스 처치 후 6/11/16/21층 진입 시 통계 요약 창 표시.

### 디스플레이
- 영웅 아바타 + 레벨/클래스
- 통계: 힘, 소요시간, 깊이, 처치, 소지금, 식사, 연금술

**파일:** `windows/WndRegionComplete.java` (신규), `scenes/InterlevelScene.java`, `teavm/TeaVMClassRegistry.java`

---

## 9. 전체화면 토글 구현

### 문제
설정 > 화면설정 > 전체화면 옵션이 HTML5에서 동작하지 않음.

### 수정
`TeaVMPlatformSupport.updateSystemUI()`에 HTML5 Fullscreen API 구현.
- `document.documentElement.requestFullscreen()` / `document.exitFullscreen()`
- `@JSBody` 네이티브 메서드로 구현

**파일:** `teavm/TeaVMPlatformSupport.java`

---

## 10. 폰트 크기 5 검은 점 수정

### 문제
시드 값 등 폰트 크기 5에서 글자 아래에 검은 점 표시.

### 원인
Canvas 2D `strokeText()`의 1px 테두리가 글리프 높이를 초과.

### 수정
`glyphH = pad + fontSize + pad + ceil(borderWidth) + 1` — 하단 1px 여유 추가.

**파일:** `teavm/FreeTypeFontGenerator.java`

---

## 11. 조작키 변경

### TAG_RESUME 기본키
`Input.Keys.B` → `Input.Keys.GRAVE` (` 키)

### TAG_LOOT 텍스트
- "계단 이동 / 줍기" → "줍기" (계단 로직 롤백)
- TAG_LOOT에서 계단 이동 로직 제거 (LootIndicator에서 롤백)

### WAIT 텍스트
- KO: "1턴 대기" → "대기"

**파일:** `SPDAction.java`, `ui/LootIndicator.java`, `windows/windows_*.properties`

---

## 12. "새 게임" 버튼 노란 글자 제거

WndGame의 "새 게임(start)" 버튼에서 `textColor(Window.TITLE_COLOR)` 삭제.

**파일:** `windows/WndGame.java`

---

## 13. 연금술 가이드 레시피 순서 변경

### 페이지 5 (폭탄 개조)
서리/화염/연막/재성장 → null → 양/소음/섬광/신성 → null → 비전/파편

### 페이지 7 (혼합물과 영약)
불안정한/눈보라/지옥불/물/산성/전격 → null → 영약 8종 (기존 순서 유지)

**파일:** `ui/QuickRecipe.java`

---

## 14. 레이아웃 정비

### 가로모드 창 폭 통일
WndJournal, WndSettings, JournalScene: 모두 **250px**

### 저널 모험노트 배치
- 세로모드: 1줄에 **2항목** (MIN_GROUP_SIZE=72, width=147)
- 가로모드: 1줄에 **3항목** (MIN_GROUP_SIZE=72, width=250)
- 균등 열 배치: `colWidth = width / maxGroups`

### 저널 지침서 배치
- 세로/가로 모두 1줄 1항목 (ScrollingListPane 2열 비활성화)

### 저널 구분선
- 세로 구분선: 줄 간 끊김 수정 (`groupBottom` 계산에 +2 gap 포함)
- 모든 구분선 색상: `0xFF222222` (조작키 설정과 동일)

### StartScene 슬롯 버튼
- SLOT_WIDTH: 120 → **135**

### 언어 버튼 포커스 색상 보존
RedButton에 `savedTextColor` 필드 + `textColor()` 오버라이드 → auto-shrink 후 색상 복원.

**파일:** `ui/RedButton.java`

---

## 15. 긴 버튼 텍스트 단축 — 타이틀 화면

| 언어 | 키 | 이전 | 변경 |
|------|---|------|------|
| ES | Rankings | Clasificaciones | **Récords** |
| PT | Rankings | Classificações | **Ranking** |
| TR | Rankings | Sıralamalar | **Sıralama** |
| NL | Settings | Instellingen | **Opties** |
| SV | Settings | Inställningar | **Alternativ** |

**파일:** `scenes/scenes_*.properties`, `Changelog/translation_guide.md`

---

## 16. 번역

### 전수 검토
- 210개 missing keys 수정 (WndRegionComplete 9키×21언어 + replay_full 1키×21언어)
- 조작키 텍스트 21개 언어 업데이트 (wait_or_pickup, tag_loot, wait)

### 연금술 가이드 텍스트
- "에너지와 음식" 설명에서 중간 문장 추가 후 삭제 (최종: 원본 2단락 유지)
- replay_full 메시지 문구 변경 (22개 언어)

---

## 17. RightClickMenu

- 제목: 노란색(TITLE_COLOR) 복원
- 버튼: 흰색(0xFFFFFF) + `multiline=true` (auto-shrink 비활성화)
- 구분선: `separator.visible = false`

**파일:** `ui/RightClickMenu.java`

---

## 수정 파일 목록

### Java (주요)
| 파일 | 변경 |
|------|------|
| `actors/hero/Hero.java` | interrupt() 콜백 복구, resume() damageInterrupt=true |
| `actors/Actor.java` | heroStuckFrames 안전장치 |
| `actors/Char.java` | wasRecentlyVisible 추가 후 롤백 (최종: 변경 없음) |
| `ui/AttackIndicator.java` | 원본 SPD 복원 + enable(false) |
| `ui/Toolbar.java` | 탐색 examining 리셋, 계단 이동 |
| `ui/ScrollingGridPane.java` | 균등 열 배치, 세로 구분선 높이 수정 |
| `ui/ScrollingListPane.java` | 2열 비활성화 |
| `ui/RedButton.java` | savedTextColor 색상 보존 |
| `ui/RightClickMenu.java` | 제목 노란색, 버튼 흰색, 구분선 숨김 |
| `ui/QuickRecipe.java` | 폭탄/혼합물 레시피 순서 변경 |
| `windows/WndRegionComplete.java` | 신규 — 지역 완료 통계 |
| `windows/WndRanking.java` | 슬롯 만석 체크, 도전항목 적용 |
| `windows/WndGame.java` | 새 게임 노란색 제거 |
| `windows/WndSettings.java` | WIDTH_L=250 |
| `windows/WndJournal.java` | WIDTH_L=250 |
| `scenes/InterlevelScene.java` | WndRegionComplete 연동 |
| `scenes/HeroSelectScene.java` | 시드 초기화 |
| `scenes/JournalScene.java` | WIDTH_L=250 |
| `scenes/StartScene.java` | SLOT_WIDTH=135 |
| `teavm/TeaVMPlatformSupport.java` | 전체화면 API |
| `teavm/FreeTypeFontGenerator.java` | glyphH +1 |
| `teavm/TeaVMClassRegistry.java` | WndRegionComplete 등록 |
| `SPDAction.java` | TAG_RESUME 기본키 변경 |

### Properties
- `windows/windows_*.properties` — WndRegionComplete, replay_full, 조작키 텍스트
- `scenes/scenes_*.properties` — 타이틀 버튼 단축
- `journal/journal_*.properties` — 연금술 가이드 텍스트
- `Changelog/translation_guide.md` — 타이틀 버튼 단축 항목 추가
