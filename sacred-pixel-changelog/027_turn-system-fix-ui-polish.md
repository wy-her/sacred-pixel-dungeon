# 027. 턴 시스템 근본 수정, UI 추가 정비

**날짜**: 2026-03-29

## 개요

경비원 2턴 공격 버그와 리퍼악마 도약 버그 근본 해결, UI 추가 정비, 테스트 레벨 개편.

---

## 변경 사항

### 1. 턴 시스템 근본 수정 (Bug 1 & 2 해결)

### Bug 1: 경비원 2턴 공격 + 1턴 건너뛰기 — 해결

**원인:** `Hero.act()`의 `enemyCanAct` 로직이 `actPriority = MOB_PRIO - 1`로 영웅 우선순위를 낮추고 `return true`하여, Actor.process()의 선택 루프에서 영웅이 반복적으로 건너뛰어지면서 몹이 추가 행동 기회를 얻음.

**수정:** `enemyCanAct` 블록 **전체 제거**. 영웅은 자신의 시간에 단순히 `ready()`만 호출.

```java
// Before (문제의 원인)
} else if (!ready) {
    boolean enemyCanAct = false;
    // blocking Pushing 체크, cooldown < 0 체크...
    if (enemyCanAct) {
        actPriority = MOB_PRIO - 1;  // 우선순위 조작
        return true;                  // 영웅 양보
    }
    actPriority = HERO_PRIO;
    ready();

// After
} else if (!ready) {
    ready();
```

**파일:** `actors/hero/Hero.java`

---

### Bug 2: 리퍼악마 도약 + 영웅 겹침 시 커맨드 대기 — 해결

**원인 1:** 리퍼 도약 실행 시 `spend()`를 호출하지 않고 `return false` → 리퍼의 time 미전진 → 영웅이 리퍼보다 먼저 Actor.process()에서 선택 → `ready()` 호출 → `heroWaiting = true` → 리퍼의 후속 행동(Pushing, 공격) 차단 → 교착

**수정 1:** `sprite.jump()` 전에 `spend(attackDelay())` 추가하여 리퍼의 time을 전진시킴.

```java
// Before
sprite.jump(pos, leapPos, callback);
return false;  // spend 없이 콜백 대기

// After
spend(attackDelay());  // 시간 소비하여 영웅보다 time이 앞서도록
sprite.jump(pos, leapPos, callback);
return false;
```

**파일:** `actors/mobs/RipperDemon.java`

**원인 2:** `heroWaiting` 가드가 VFX 우선순위 Actor(Pushing)까지 차단하여, 리퍼 바운스 Pushing이 완료되지 못함.

**수정 2:** VFX 우선순위 Actor는 `heroWaiting` 가드에서 예외 처리.

```java
// Before
if (heroWaiting && current != null && current.time >= heroTime) {
    current = null;  // 모든 Actor 차단
}

// After
if (heroWaiting && current != null
        && current.time >= heroTime
        && current.actPriority < VFX_PRIO) {  // VFX Actor 예외
    current = null;
}
```

**파일:** `actors/Actor.java`

---

### Actor.process() heroWaiting 로직 개선

**이전:** 영웅이 입력 대기 시 **모든** Actor 처리 중단
**변경:** 영웅 시간 이전의 Actor + VFX 우선순위 Actor는 계속 처리 허용

```java
// heroTime 추적 추가
float heroTime = Float.MAX_VALUE;
if (actor == Dungeon.hero) {
    heroWaiting = true;
    heroTime = actor.time;
}

// 시간 기반 + VFX 예외 가드
if (heroWaiting && current != null
        && current.time >= heroTime
        && current.actPriority < VFX_PRIO) {
    current = null;
}
```

**파일:** `actors/Actor.java`

---

## 2. UI 추가 정비

### WndUseItem 아이템 액션 버튼
- 버튼 배치: 복잡한 최단-확장 알고리즘 → **같은 줄 균등 너비 분배**
- 동적 크기 버튼(reqWidth 기반): `multiline=true`로 auto-shrink 비활성화
- WndGameInProgress 도전 버튼: 동일하게 `multiline=true` 적용

### multiline 버튼 개선
- 한국어/일본어/중국어: `maxWidth()` 미적용 → 줄바꿈 방지
- 2줄 텍스트 세로 가운데: **+2px** offset 보정 (`nLines > 1` 체크)

### WndOptions 제목 가운데 정렬
- 기존: `tfTitle.setPos(MARGIN, pos)` — 좌정렬
- 변경: `tfTitle.setPos((width - tfTitle.width()) / 2f, pos)` — **가운데 정렬**

### 설정 > 언어 탭 버튼 균등 분배
- 기존: `Math.floor()` → 나머지 픽셀이 오른쪽 여백으로 축적
- 변경: `width * colIdx / cols` 방식 → 각 열 너비 균등, 오른쪽 여백 없음

### WndSettings UITab 여백 수정
- `sep2.y = title.bottom() + 3*GAP` → `title.bottom() + 2` (hasSliders=false 분기)

### WndChallenges/WndRandomize 제목 아래 여백
- 2px → **4px** (제목과 콘텐츠 간 적절한 간격)

### 한국어 텍스트 변경
- WndGame "도전" → **"도전 항목"**

### 타이틀 화면
- "Test Zone" → **"Test Level"**

---

## 3. 테스트 레벨 개편

### 몹 변경
- Spinner(거미) → **Guard** (경비원)
- Warlock(드워프주술사) → **Shaman.RedShaman** (놀 주술사)
- Eye(악마의 눈) → **Necromancer** (사령술사)
- RipperDemon 유지

### NPC 전부 제거
- 유령, 지팡이깎는노인, 대장장이, 임프, 상인 모두 제거

---

## 4. Pushing.isBlockingVfx() 추가

- `effects/Pushing.java`에 `isBlockingVfx()` getter 추가
- `blockingVfx` 필드의 읽기 접근 제공

---

## 수정 파일 목록

| 파일 | 변경 내용 |
|------|----------|
| `actors/hero/Hero.java` | enemyCanAct 블록 전체 제거 |
| `actors/Actor.java` | heroWaiting 로직: heroTime 추적 + VFX 예외 |
| `actors/mobs/RipperDemon.java` | 도약 전 spend(attackDelay()) 추가 |
| `effects/Pushing.java` | isBlockingVfx() getter 추가 |
| `windows/WndUseItem.java` | 균등 너비 분배, multiline auto-shrink 비활성화 |
| `windows/WndGameInProgress.java` | 도전 버튼 multiline=true |
| `windows/WndOptions.java` | 제목 가운데 정렬 |
| `windows/WndSettings.java` | UITab sep2 여백 수정, 언어 버튼 균등 분배 |
| `windows/WndChallenges.java` | 제목 아래 여백 4px |
| `ui/StyledButton.java` | multiline Asian 줄바꿈 방지, 2줄 세로 보정 |
| `scenes/HeroSelectScene.java` | WndRandomize 제목 여백 4px |
| `messages/windows/windows_ko.properties` | "도전" → "도전 항목" |
