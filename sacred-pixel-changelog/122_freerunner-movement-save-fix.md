# 122. 프리러너 이동 버그 수정

## 문제
프리러너 클래스로 플레이 중인 세이브 데이터를 로드하면, 한 번 이동한 후 더 이상 이동이 되지 않는 버그가 있었습니다. 가방이나 다른 상호작용은 정상 작동하며, 다른 클래스에서는 발생하지 않았습니다.

## 원인

### 1. Hero.ready 필드 미직렬화
`Hero.java`의 `ready` 필드가 `storeInBundle()`/`restoreFromBundle()`에서 저장/복원되지 않았습니다.

- `ready = false`일 때 `handle()` 메서드에서 모든 입력이 차단됨
- 게임 로드 후 `ready`가 기본값 `false`로 남아 있어 이동 불가

### 2. Momentum.movedLastTurn 하드코딩
`Momentum.java`의 `restoreFromBundle()`에서 `movedLastTurn = false`로 하드코딩되어 있었습니다.

- 로드 후 첫 `act()` 호출 시 `!movedLastTurn` 조건이 참이 되어 모멘텀 스택이 감소
- 정상 플레이와 다른 동작 유발

## 수정 내용

### Hero.java
- `READY` 상수 추가
- `storeInBundle()`에 `ready` 저장 추가
- `restoreFromBundle()`에 `ready` 복원 추가 (기본값 `false`)

```java
private static final String READY = "ready";

// storeInBundle()
bundle.put( READY, ready );

// restoreFromBundle()
ready = bundle.contains(READY) ? bundle.getBoolean(READY) : false;
```

### Momentum.java
- `MOVED_LAST_TURN` 상수 추가
- `storeInBundle()`에 `movedLastTurn` 저장 추가
- `restoreFromBundle()`에서 `movedLastTurn` 복원 (기본값 `true`로 하위 호환성 유지)

```java
private static final String MOVED_LAST_TURN = "moved_last_turn";

// storeInBundle()
bundle.put(MOVED_LAST_TURN, movedLastTurn);

// restoreFromBundle()
// 기존: movedLastTurn = false;
movedLastTurn = bundle.contains(MOVED_LAST_TURN) ? bundle.getBoolean(MOVED_LAST_TURN) : true;
```

## 기술적 세부사항
- 파일: `core/src/main/java/.../actors/hero/Hero.java`
- 파일: `core/src/main/java/.../actors/buffs/Momentum.java`
- 5명의 에이전트가 4 스프린트에 걸쳐 분석 후 도출한 결론
- 기존 세이브 파일과의 하위 호환성 유지 (기본값 설정)

## 영향
- 프리러너 클래스 세이브 로드 후 이동이 정상 작동
- 모멘텀 스택이 로드 직후 불필요하게 감소하지 않음
- 다른 클래스 및 기존 세이브에 영향 없음
