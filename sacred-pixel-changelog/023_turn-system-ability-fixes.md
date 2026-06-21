# 023. 턴 시스템 및 영웅 능력 버그 수정

**날짜**: 2026-03-27

## 개요

턴 시스템 및 영웅 능력 버그 수정.

---

## 변경 사항

#### HTML5 턴 시스템 — 빠른 공격 몹 두 번째 공격 전 영웅 입력 대기
- **파일:** `Hero.java` (enemyCanAct 조건)
- **문제:** attackDelay 0.5인 몹(리퍼데몬, 몽크, 도둑, 구울)이 첫 공격 애니메이션 중일 때, 영웅이 잘못 `ready()` 호출하여 입력 대기 상태 진입
- **원인:** `enemyCanAct` 체크에서 `!m.isWaitingForCallback()` 조건이 애니메이션 중인 몹을 제외
- **수정:** `m.isWaitingForCallback() || m.cooldown() <= 0`으로 변경 — 애니메이션 중인 적도 "행동 가능"으로 간주
- **영향:** 턴 인디케이터가 불필요하게 회전하던 문제도 함께 해결

#### 리퍼데몬 도약 바운스 — 스프라이트 겹침
- **파일:** `RipperDemon.java` (도약 콜백)
- **문제:** HTML5에서 `heroWaiting`이 `Pushing` Actor 실행을 차단하여 리퍼 스프라이트가 영웅 위치에 남음
- **원인:** 위 Hero.act() enemyCanAct 버그의 파생 문제
- **수정:** enemyCanAct 수정으로 근본 원인 해결, Pushing 애니메이션 정상 복원

#### 텔레포트 후 키보드 자동이동 — 이전 목적지로 이동
- **파일:** `ScrollOfTeleportation.java` (teleportChar, teleportToLocation)
- **문제:** 텔레포트 함정/아이템 사용 후 키보드 방향키 입력 시 이전 이동 목적지로 자동이동
- **원인:** `interrupt()`가 `lastAction`에 이전 Move 액션을 저장하고, `resume()`이 이를 복원
- **수정:** `teleportChar()`, `teleportToLocation()` 모두에서 `interrupt()` 후 `lastAction = null` 추가
- **영향:** Flash, BeaconOfReturning, StoneOfBlink 등 모든 텔레포트 경유 아이템에 적용

#### Feint 능력 — FOV 갱신 누락
- **파일:** `Feint.java` (이동 콜백)
- **문제:** 듀얼리스트 Feint 능력 사용 후 시야/안개가 갱신되지 않음
- **비교:** 동일 클래스의 Challenge 능력은 정상 호출
- **수정:** 이동 콜백에 `Dungeon.observe()` + `GameScene.updateFog()` 추가

#### Ghoul attackDelay 하드코딩
- **파일:** `Ghoul.java` (attackDelay)
- **문제:** `return 0.5f` 하드코딩으로 Adrenaline 버프가 Ghoul에만 미적용
- **비교:** Monk/Thief/RipperDemon은 `super.attackDelay()*0.5f`로 버프 반영
- **수정:** `return super.attackDelay()*0.5f`로 변경

### UI/UX

#### 퀘스트 NPC 대화창 — advanceDialog 처리 통일
- **파일:** `WndQuest.java`
- **문제:** HTML5에서 Enter 키 없이 대화창을 닫을 때 `onBackPressed()` → `advanceDialog = false` → 퀘스트 미부여
- **수정:** `onBackPressed()`를 `onConfirm()`으로 위임 — Enter/ESC/클릭/탭 모두 "읽은 것"으로 처리
- **영향:** Wandmaker, Blacksmith 퀘스트 대화가 어떤 방법으로 닫아도 정상 진행

### Test Level

#### 테스트존 NPC 배치
- Ghost, Wandmaker 추가 (입구방 좌우)
- 4개 퀘스트 NPC 모두 초기 상태 (given=false)로 설정
- Blacksmith/Imp 기존 완료 상태에서 초기 상태로 변경

#### 테스트존 적 배치 분리
- 리퍼데몬, 경비, 주술사, 거미를 각각 Room D/E/B/A에 분리 배치
- 모든 테스트 적: SLEEPING 상태 + viewDistance=3으로 방 이탈 방지
- 입구방에서 적 제거 — NPC만 남김
