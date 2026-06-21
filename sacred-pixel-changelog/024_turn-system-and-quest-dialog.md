# 024. 턴 시스템 수정 및 퀘스트 대화 개선

**날짜**: 2026-03-27

## 개요

HTML5 환경의 턴 시스템 버그 수정, 리퍼데몬 도약 바운스 문제 해결, 텔레포트 후 키보드 자동이동 버그 수정, 퀘스트 NPC 대화창 처리 통일.

---

## 변경 사항

### 1. HTML5 턴 시스템 — 빠른 공격 몹 두 번째 공격 전 영웅 입력 대기

**파일:** `actors/hero/Hero.java` (enemyCanAct 조건)

- **문제:** attackDelay 0.5인 몹(리퍼데몬, 몽크, 도둑, 구울)이 첫 공격 애니메이션 중일 때, 영웅이 잘못 `ready()` 호출하여 입력 대기 상태 진입
- **원인:** `enemyCanAct` 체크에서 `!m.isWaitingForCallback()` 조건이 애니메이션 중인 몹을 제외
- **수정 1차:** `m.isWaitingForCallback() || m.cooldown() <= 0`으로 변경
- **수정 2차:** `<= 0`을 `< 0`으로 재수정 — cooldown=0(같은 시간)이면 영웅이 우선순위로 행동해야 함. `<= 0`은 주술사 등 attackDelay=1.0 몹이 2턴 몰아치기하는 부작용 유발
- **수정 3차:** Pushing Actor 대기 체크 추가 — `Actor.all()`에서 Pushing 인스턴스가 있으면 영웅이 `ready()` 호출하지 않도록 함
- **영향:** 턴 인디케이터 불필요 회전 문제도 함께 해결

---

## 2. 리퍼데몬 도약 바운스 — 스프라이트 겹침 및 Pushing 지연

**파일:** `actors/mobs/RipperDemon.java` (도약 콜백)

- **문제 1:** HTML5에서 `heroWaiting`이 `Pushing` Actor 실행을 차단하여 리퍼 스프라이트가 영웅 위치에 남음
- **문제 2:** 영웅이 리퍼의 도약 착지점에 있을 때, Pushing이 다음 턴까지 지연되어 겹침 발생
- **수정:** 리퍼 바운스 Pushing에 `setBlockingVfx()` 적용 — Pushing이 완료될 때까지 모든 Actor 처리 차단
- **결과:** 도약 → 바운스 Pushing 완료 → 영웅 입력/리퍼 공격 순서 보장

---

## 3. 텔레포트 후 키보드 자동이동 — 이전 목적지로 이동

**파일:** `items/scrolls/ScrollOfTeleportation.java`

- **문제:** 텔레포트 함정/아이템 사용 후 키보드 방향키 입력 시 이전 이동 목적지로 자동이동
- **원인:** `interrupt()`가 `lastAction`에 이전 Move 액션을 저장하고, `resume()`이 이를 복원
- **수정:** `teleportChar()`, `teleportToLocation()` 모두에서 `interrupt()` 후 `lastAction = null` 추가
- **영향:** Flash, BeaconOfReturning, StoneOfBlink 등 모든 텔레포트 경유 아이템에 적용

---

## 4. Feint 능력 — FOV 갱신 누락

**파일:** `actors/hero/abilities/duelist/Feint.java`

- **문제:** 듀얼리스트 Feint 능력 사용 후 시야/안개가 갱신되지 않음
- **비교:** 동일 클래스의 Challenge 능력은 정상 호출
- **수정:** 이동 콜백에 `Dungeon.observe()` + `GameScene.updateFog()` 추가

---

## 5. Ghoul attackDelay 하드코딩

**파일:** `actors/mobs/Ghoul.java`

- **문제:** `return 0.5f` 하드코딩으로 Adrenaline 버프가 Ghoul에만 미적용
- **비교:** Monk/Thief/RipperDemon은 `super.attackDelay()*0.5f`로 버프 반영
- **수정:** `return super.attackDelay()*0.5f`로 변경

---

## 6. 퀘스트 NPC 대화창 — advanceDialog 처리 통일

**파일:** `windows/WndQuest.java`

- **문제:** HTML5에서 Enter 키 없이 대화창을 닫을 때 `onBackPressed()` → `advanceDialog = false` → 퀘스트 미부여
- **수정:** `onBackPressed()`를 `onConfirm()`으로 위임 — Enter/ESC/클릭/탭 모두 "읽은 것"으로 처리
- **영향:** Wandmaker, Blacksmith 퀘스트 대화가 어떤 방법으로 닫아도 정상 진행

---

## 7. 창 세로 길이 — 원본 SPD 기준 롤백

**파일:** `windows/WndJournal.java`, `windows/WndHero.java`, `windows/WndHeroInfo.java`

이전 세션(#34)에서 변경된 창 세로 길이를 원본 SPD 값으로 롤백:

| 창 | 속성 | 변경 전 | 롤백 후 (원본) |
|---|------|--------|--------------|
| WndJournal | HEIGHT_P | 198 | **180** |
| WndJournal | HEIGHT_L | 132 | **130** |
| WndHero | HEIGHT | 132 | **120** |
| WndHeroInfo | MIN_HEIGHT | 125 | **125** (변경 없음) |

---

## 수정된 파일

| File | Changes |
|------|----------|
| `actors/hero/Hero.java` | enemyCanAct 조건 수정 (waitingForCallback, cooldown<0, Pushing 체크) |
| `actors/mobs/RipperDemon.java` | 도약 바운스 Pushing에 setBlockingVfx 적용 |
| `actors/mobs/Ghoul.java` | attackDelay 하드코딩 → super.attackDelay()*0.5f |
| `actors/hero/abilities/duelist/Feint.java` | FOV 갱신 추가 |
| `items/scrolls/ScrollOfTeleportation.java` | teleportChar, teleportToLocation에 lastAction=null 추가 |
| `windows/WndQuest.java` | onBackPressed → onConfirm 위임 |
| `windows/WndJournal.java` | HEIGHT_P/L 원본 롤백 |
| `windows/WndHero.java` | HEIGHT 원본 롤백 |
| `windows/WndHeroInfo.java` | MIN_HEIGHT 원본 유지 확인 |
