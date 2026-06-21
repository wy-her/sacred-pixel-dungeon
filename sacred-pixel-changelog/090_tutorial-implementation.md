# 090. 튜토리얼 구현

**날짜**: 2026-06-13

## 개요

신규 플레이어를 위한 단계별 튜토리얼 시스템 구현. 기본 조작, 전투, 아이템 사용, 탐색, 기습 공격을 학습.

---

## 변경 사항

### 1. 새로운 파일 추가

| 파일 | 설명 |
|-----|------|
| `tutorial/TutorialManager.java` | 튜토리얼 상태 관리, 액션 처리, 행동 제한 |
| `tutorial/TutorialState.java` | 튜토리얼 상태 enum (25개 상태) |
| `levels/TutorialLevel.java` | 튜토리얼 전용 맵, 몬스터/아이템 스폰 |
| `windows/WndTutorial.java` | 튜토리얼 힌트 윈도우 (OK 버튼 포함) |

### 2. 수정된 기존 파일

| 파일 | 변경 내용 |
|-----|----------|
| `scenes/CellSelector.java` | 튜토리얼 행동 제한 체크 추가 |
| `actors/mobs/Snake.java` | 튜토리얼용 기습 공격 로직 추가 |
| `ui/Window.java` | onConfirm() 수정 - 포커스 없으면 아무것도 안함 |
| `windows/WndStory.java` | 튜토리얼 상태 연동 |
| `scenes/TitleScene.java` | 튜토리얼 시작 버튼 추가 |

---

## 기술적 상세

### 행동 제한 시스템

```java
// TutorialManager.java
public static boolean isMovementRestricted() {
    return state == TutorialState.SURPRISE_GUIDE_SHOWN
        || state == TutorialState.WAIT_FOR_SNAKE
        || state == TutorialState.SNAKE_AT_DOOR;
}
```

- SURPRISE_GUIDE_SHOWN: 가이드 창 열림 → 모든 행동 차단
- WAIT_FOR_SNAKE: 대기만 허용
- SNAKE_AT_DOOR: 공격만 허용

### 기습 공격 메커니즘

**문제**: Snake.defenseSkill()이 INFINITE_EVASION 반환 → 공격 무조건 MISS

**해결**: fieldOfView 체크로 회피/명중 결정
```java
// Snake.java
if (fieldOfView == null || !fieldOfView[enemy.pos]) {
    return 0; // 기습 - 명중 허용
}
return INFINITE_EVASION; // 일반 - 회피
```

### 몬스터 이동 + 문 열기

```java
// TutorialLevel.java - 뱀의 act()
int oldPos = pos;
move(DOOR_POS);           // 위치 변경 + 문 열기
moveSprite(oldPos, pos);  // 애니메이션
```

- move() 내부: occupyCell() → pressCell() → Door.enter()

### Enter 키 처리 통일

```java
// Window.java - onConfirm()
public void onConfirm() {
    if (focusIndex >= 0 && focusIndex < focusableButtons.size()) {
        activateFocused();
    }
    // 포커스 없으면 아무것도 안함 (WndOptions와 동일)
}
```

---

## 레벨 맵 좌표

```
WIDTH = 16, HEIGHT = 12

주요 위치:
- SPAWN_POS = (7, 6) - 영웅 시작
- RAT_SPAWN_POS = (7, 4) - 쥐 스폰
- ALCOVE_POS = (9, 3) - 탐색 페이지
- EXTENDED_ALCOVE_POS = (10, 3) - 뱀 스폰
- DOOR_POS = (11, 3) - 숨겨진 문
- CORRIDOR_POS = (12, 3) - 복도 (기습 트리거)
```

---

## Lessons Learned

1. **키보드/마우스 입력 모두 체크**: CellSelector의 onSelect(), moveFromActions() 둘 다 제한 필요

2. **적 방향 이동 = 공격**: 이동 제한 시에도 적 방향 이동은 허용해야 함

3. **fieldOfView 타이밍**: super.act() 호출 전에만 null, 호출 후 업데이트됨

4. **명중/회피 계산 순서**: defenseSkill() → hit 계산 → damage() 호출. damage()에서 기습 체크하면 이미 늦음

5. **move() + moveSprite()**: 위치 변경과 애니메이션 둘 다 필요

---

## 수정된 파일

| File | Changes |
|------|---------|
| `tutorial/TutorialManager.java` | 신규 - 튜토리얼 상태 관리 |
| `tutorial/TutorialState.java` | 신규 - 상태 enum (25개) |
| `levels/TutorialLevel.java` | 신규 - 튜토리얼 전용 맵 |
| `windows/WndTutorial.java` | 신규 - 힌트 윈도우 |
| `scenes/CellSelector.java` | 튜토리얼 행동 제한 체크 |
| `actors/mobs/Snake.java` | 기습 공격 로직 |
| `ui/Window.java` | onConfirm() 수정 |
| `windows/WndStory.java` | 튜토리얼 상태 연동 |
| `scenes/TitleScene.java` | 튜토리얼 시작 버튼 |

---

## 테스트 항목

- [x] 튜토리얼 시작 및 완료
- [x] 각 상태 전환 정상 작동
- [x] 행동 제한 (이동/대기/공격)
- [x] 기습 공격 성공
- [x] 윈도우 Enter 키 처리

---
