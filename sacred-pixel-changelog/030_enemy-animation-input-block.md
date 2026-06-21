# 030. 적 애니메이션 중 입력 차단

**날짜**: 2026-03-31

## 개요

적 공격 애니메이션 중 영웅 입력 차단 기능 추가, 타이틀 화면 BGM 개선, 테스트존 BGM 추가.

---

## 변경 사항

### 1. Ripper Demon 버그 수정 - 적 공격 애니메이션 중 영웅 입력 차단

### 문제
- Ripper Demon이 2회 연속 공격하는 동안 영웅이 이동 커맨드를 받아 이동해버림
- 적의 공격 애니메이션이 진행 중인데도 영웅이 움직여서 거리가 벌어지는 현상

### 원인
- 적 몹이 `spend(attackDelay())`로 턴을 소비한 후 애니메이션을 재생하는 동안
- Hero의 턴이 돌아와서 `ready = true` 상태가 됨
- 이 상태에서 유저 입력을 받아 영웅이 이동 가능했음

### 해결

#### Actor.java (line 554-563)
```java
//Returns true if any enemy mob is currently in the middle of an animation (waitingForCallback).
//Used to prevent hero input during enemy attack animations.
public static synchronized boolean anyEnemyAnimating() {
    for (Char ch : chars) {
        if (ch instanceof com.sacredpixel.sacredpixeldungeon.actors.mobs.Mob
                && ch.alignment == Char.Alignment.ENEMY
                && ch.isWaitingForCallback()) {
            return true;
        }
    }
    return false;
}
```

#### CellSelector.java - 6개 위치에 체크 추가

1. **onClick()** (line 91)
```java
if (Actor.anyEnemyAnimating()) return;
```

2. **select()** (line 167-170)
```java
if (Actor.anyEnemyAnimating()) {
    GameScene.cancel();
    return;
}
```

3. **키보드 방향키 이벤트** (line 467)
```java
if (Actor.anyEnemyAnimating()) return true; // Block input during enemy attack animations
```

4. **update() 루프** (line 544, 546)
```java
if ((heldAction1 != SPDAction.NONE || leftStickAction != SPDAction.NONE) && Dungeon.hero.ready
        && !Actor.anyEnemyAnimating()){
    processKeyHold();
} else if (Dungeon.hero.ready && !Actor.anyEnemyAnimating()) {
```

5. **moveFromActions()** (line 559-561)
```java
if (Actor.anyEnemyAnimating()) {
    return false;
}
```

6. **processKeyHold()** (line 679)
```java
if (Actor.anyEnemyAnimating()) return;
```

---

## 2. 타이틀 화면 BGM 개선 (이전 세션에서 구현)

### TitleScene.java
- 화면 어디를 터치/클릭해도 BGM 재생 시작 (브라우저 autoplay 정책 우회)
- `PointerEvent.addPointerListener()` 사용하여 전역 터치 리스너 등록
- 로고 glow 애니메이션 속도 2배 느리게 (1.5f → 0.75f)

### SPDSettings.java
- `fullscreen()` 기본값을 `true`에서 `false`로 변경

---

## 3. 불필요한 파일 정리 (이전 세션)

### 삭제된 디렉토리
- `teavm/src/main/java/com/shatteredpixel/` - Shattered → Sacred 리브랜딩 후 남은 빈 디렉토리

---

## Files Modified

| File | Changes |
|------|---------|
| `core/.../actors/Actor.java` | `anyEnemyAnimating()` 메서드 추가 |
| `core/.../scenes/CellSelector.java` | 6개 위치에 적 애니메이션 체크 추가 |
| `core/.../scenes/TitleScene.java` | BGM 전역 터치 리스너, glow 속도 조절 |
| `core/.../SPDSettings.java` | fullscreen 기본값 false |

---

## Technical Notes

### waitingForCallback 메커니즘
- HTML5/TeaVM 환경에서 애니메이션 콜백 대기 상태를 추적
- `act()`에서 `false` 반환 시 `waitingForCallback = true` 설정
- 애니메이션 완료 후 `next()` 호출 시 플래그 해제
- 이 플래그를 활용하여 적 공격 애니메이션 진행 여부 판단

### 입력 차단 범위
- 마우스/터치 클릭 (`onClick`)
- 셀 선택 (`select`)
- 키보드 방향키 (`keyListener`)
- 키보드 홀드 이동 (`processKeyHold`, `moveFromActions`)
- 컨트롤러 스틱 입력 (`update` 루프)
