# 147. 프리러너 채굴 후 입력 불가 버그 수정 (HTML5 waitingForCallback)

**날짜**: 2026-07-28

## 배경

프리러너가 채굴 후 입력이 불가능해지는 버그가 있었습니다 (이슈 #136). #140에서 Momentum 타이밍 문제를 수정했으나, HTML5 빌드에서만 발생하는 추가 문제가 있었습니다.

## 원인 분석

### HTML5 빌드의 waitingForCallback 메커니즘

HTML5/TeaVM 빌드에서는 멀티스레딩이 불가능하므로, `Actor.process()`에서 actor가 콜백을 기다릴 때 `waitingForCallback = true`를 설정하여 다음 프레임에서 같은 actor가 다시 선택되는 것을 방지합니다.

```java
// Actor.process()의 기존 로직
if (!doNext){
    if (ThreadCompat.currentThread() == null && current != null) {
        current.waitingForCallback = true;  // 콜백 대기 플래그 설정
    }
}
```

### 문제점

`Hero.act()`는 두 가지 이유로 `false`를 반환합니다:

| 상황 | `act()` 반환값 | `hero.ready` | 의미 |
|------|---------------|--------------|------|
| 애니메이션 콜백 대기 | `false` | `false` | 공격/이동 등 애니메이션 중 |
| **플레이어 입력 대기** | `false` | **`true`** | 사용자 입력 대기 중 |

기존 코드는 `Hero.act()`가 `false`를 반환하면 무조건 `waitingForCallback = true`를 설정했습니다. 그러나 `hero.ready = true`인 경우는 애니메이션 콜백이 아닌 **플레이어 입력**을 기다리는 것이므로, `waitingForCallback`을 설정하면 안 됩니다.

### 버그 발생 시나리오

```
1. 프리러너가 채굴 완료
   └─ Hero.ready() 호출 → hero.ready = true

2. Hero.act() 호출
   └─ hero.ready=true이므로 false 반환 (입력 대기)

3. Actor.process()에서 doNext=false 감지
   └─ 기존: waitingForCallback = true 설정 (버그!)
   └─ Hero가 콜백 대기 상태로 잘못 표시됨

4. 다음 프레임에서 Actor.next() 탐색
   └─ Hero.waitingForCallback=true → Hero 건너뜀
   └─ 입력 불가!
```

---

## 변경 사항

### Actor.process() - Hero 입력 대기 상태 예외 처리

```diff
  if (!doNext){
      //On HTML5, mark the actor as waiting so it isn't re-selected next frame.
-     if (ThreadCompat.currentThread() == null && current != null) {
-         current.waitingForCallback = true;
-     }
+     //IMPORTANT: Don't set waitingForCallback for Hero when hero.ready=true.
+     //hero.ready=true means Hero is waiting for PLAYER INPUT, not an animation callback.
+     //Setting waitingForCallback in this case would block the hero from being selected
+     //even though they're ready to receive input.
+     if (ThreadCompat.currentThread() == null && current != null) {
+         boolean isHeroWaitingForInput = (current == Dungeon.hero && Dungeon.hero.ready);
+         if (!isHeroWaitingForInput) {
+             current.waitingForCallback = true;
+         }
+     }
  }
```

### 왜 안전한가

1. **Hero만 영향**: `Dungeon.hero`와의 비교로 Hero에만 적용
2. **ready 체크**: `hero.ready=true`인 경우에만 예외 처리
3. **기존 동작 유지**:
   - 몬스터: 항상 기존대로 `waitingForCallback` 설정
   - Hero 애니메이션 중 (`ready=false`): 기존대로 `waitingForCallback` 설정
   - Hero 입력 대기 중 (`ready=true`): `waitingForCallback` 설정 안함 (수정)
4. **Native 빌드 무관**: `ThreadCompat.currentThread() == null` 조건으로 HTML5만 영향

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `actors/Actor.java:525-538` | `process()`에서 Hero 입력 대기 상태 예외 처리 추가 |

---

## 영향

- HTML5 빌드에서 프리러너 채굴 후 즉시 이동 가능
- 다른 클래스의 채굴 동작에도 동일하게 적용
- 몬스터나 다른 actor의 동작에는 영향 없음
- Native(Desktop/Android) 빌드에는 영향 없음

---

## 관련 문서

- #136: 프리러너 채굴 후 이동 불가 (원본 이슈)
- #140: Momentum 타이밍 문제 수정 (관련 수정)
- `Actor.process()` - Actor 처리 루프
- `Hero.ready` - Hero 입력 대기 상태 플래그
- `Actor.waitingForCallback` - HTML5 콜백 대기 플래그
