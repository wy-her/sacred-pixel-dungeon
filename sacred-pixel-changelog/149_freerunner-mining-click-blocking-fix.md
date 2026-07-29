# 149. 프리러너 채굴 후 클릭 이동 불가 버그 수정

**날짜**: 2026-07-29

## 배경

프리러너가 채굴 후 **클릭으로 이동이 불가능**해지는 버그가 발생했습니다 (이슈 #136). 키보드 이동은 정상 작동하지만 클릭/터치 입력만 막히는 현상이었습니다.

---

## 원인 분석

### 키보드 vs 클릭 입력 경로 차이

| 입력 방식 | 경로 | `hero.ready` 설정 |
|----------|------|------------------|
| 키보드 | `CellSelector.processKeyHold()` → `hero.handle()` → `hero.ready = false` | O |
| 클릭 | `defaultCellListener.onSelect()` → `hero.handle()` | **X (버그)** |

키보드 입력은 `hero.handle()` 후 `hero.ready = false`를 설정하지만, 클릭 입력은 설정하지 않았습니다.

### Actor.process()의 heroWaiting 블로킹 로직

```java
// Actor.java:429-434
boolean heroWaiting = Dungeon.hero.ready;  // hero.ready=true면 heroWaiting=true

if (heroWaiting && current != null
        && current != Dungeon.hero
        && current.time >= heroTime
        && current.actPriority < VFX_PRIO) {
    current = null;  // Actor 선택 취소 → 게임 멈춤
}
```

이 로직은 Hero가 입력을 기다리는 동안 다른 actor가 먼저 행동하지 않도록 방지합니다. 문제는:

1. 클릭 후 `hero.ready = true` 유지
2. `heroWaiting = true`로 평가
3. Momentum 버프 (`actPriority = HERO_PRIO+1`)가 선택되었으나 블로킹됨
4. `current = null` → Hero도, Momentum도 행동 못함

### 버그 발생 시나리오

```
1. 프리러너가 채굴 완료
   └─ Momentum 버프 활성화 (actPriority = HERO_PRIO+1)
   └─ Hero.ready() → hero.ready = true

2. 클릭으로 이동 명령
   └─ defaultCellListener.onSelect() 호출
   └─ hero.handle() → curAction 설정, hero.next() 호출
   └─ hero.ready는 여전히 true (버그!)

3. Actor.process() 실행
   └─ Momentum이 next()에서 선택됨 (actPriority 높음)
   └─ heroWaiting=true → current=null로 블로킹
   └─ Hero.act() 호출 안됨 → 이동 불가!

4. 키보드 이동 시
   └─ processKeyHold()에서 hero.ready=false 설정
   └─ heroWaiting=false → 블로킹 없음 → 정상 작동
```

---

## 변경 사항

### GameScene.java - defaultCellListener.onSelect()

클릭 입력도 키보드와 동일하게 `hero.ready = false` 설정:

```diff
  public static final CellSelector.Listener defaultCellListener = new CellSelector.Listener() {
      @Override
      public void onSelect( Integer cell ) {
          boolean handled = Dungeon.hero.handle( cell );
          if (handled) {
              Dungeon.hero.next();
+             // BUG FIX #136: Match keyboard behavior - set ready=false after handling input
+             // This prevents heroWaiting blocking in Actor.process() when hero has pending action
+             Dungeon.hero.ready = false;
          }
      }
      // ...
  };
```

### 왜 이 수정이 올바른가

1. **키보드 동작과 일치**: `CellSelector.processKeyHold()`가 이미 동일한 패턴 사용
2. **논리적 의미**: `handle()` 성공 = Hero가 행동 시작 = 더 이상 입력 대기 아님
3. **안전성**: `hero.ready`는 `Hero.ready()`에서 다시 `true`로 설정되므로 문제 없음
4. **영향 범위**: 클릭 입력 경로에만 영향, 다른 시스템 변경 없음

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `scenes/GameScene.java` | `defaultCellListener.onSelect()`에서 `hero.ready = false` 추가 |

---

## 영향

- 프리러너 채굴 후 클릭 이동 정상 작동
- 모든 클래스의 채굴 후 클릭 이동에 적용
- 키보드 입력에는 영향 없음 (이미 정상 작동)
- Momentum 외 다른 버프의 actor 선택에도 블로킹 방지

---

## 관련 문서

- #136: 프리러너 채굴 후 이동 불가 (원본 이슈)
- #140: `freerunner-level-transition-timing-fix.md`
- #147: `freerunner-mining-waitingforcallback-fix.md` (다른 원인의 동일 증상)
- `CellSelector.processKeyHold()` - 키보드 입력 처리 (정상 동작 참조)
- `Actor.process():429-434` - heroWaiting 블로킹 로직
