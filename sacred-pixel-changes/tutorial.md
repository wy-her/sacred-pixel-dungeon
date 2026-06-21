# 튜토리얼 구현

**날짜**: 2026-06-04

## 개요

신규 플레이어를 위한 단계별 튜토리얼입니다. 전사로 시작하여 기본 조작, 전투, 아이템 사용, 탐색, 기습 공격을 배웁니다.

---

## 변경 사항

### 튜토리얼 흐름 (현재 구현)

#### 페이즈 1: 시작 및 가이드북

1. **튜토리얼 레벨 진입** -> GUIDEBOOK_PLACED
   - 전사로 시작
   - 가이드북이 방에 배치됨

2. **가이드북 획득** -> INTRO_SHOWN
   - '소개' 스토리 윈도우 표시 (WndStory)

3. **스토리 윈도우 닫기** -> GUIDEBOOK_PICKED
   - STORY_WINDOW_CLOSED 액션 트리거

#### 페이즈 2: UI 탐색

4. **저널 힌트** -> JOURNAL_HINT_SHOWN
   - "저널 버튼을 눌러보세요" (WndTutorial)
   - 저널 버튼 깜빡임

5. **저널 열기** -> JOURNAL_OPENED
   - 저널 창 열림

6. **영웅 정보 힌트** -> HERO_INFO_HINT_SHOWN
   - "영웅정보 버튼을 눌러보세요" (WndTutorial)
   - 영웅 정보 버튼 깜빡임

7. **영웅 정보 열기** -> HERO_INFO_OPENED
   - 영웅 정보 창 열림

#### 페이즈 3: 전투

8. **쥐 스폰** -> RAT_SPAWNED
   - 체력 1인 쥐가 영웅 근처에 스폰

9. **조사 가이드** -> EXAMINE_GUIDE_SHOWN
   - '조사' 스토리 윈도우 표시 (WndStory, Document.GUIDE_EXAMINING)

10. **조사 힌트** -> EXAMINE_HINT_SHOWN
    - "돋보기 버튼으로 조사하세요" (WndTutorial)
    - 돋보기 버튼 깜빡임

11. **쥐 조사** -> RAT_EXAMINED
    - 쥐를 조사함

12. **전투 힌트** -> COMBAT_HINT_SHOWN
    - "적을 공격하세요" (WndTutorial)
    - 공격 버튼 깜빡임

13. **쥐 처치** -> RAT_KILLED
    - 쥐 사망

#### 페이즈 4: 아이템 사용

14. **두루마리 스폰** -> SCROLL_SPAWNED
    - 공포의 두루마리 스폰

15. **두루마리 힌트** -> SCROLL_HINT_SHOWN
    - "두루마리를 사용하세요" (WndTutorial)

16. **두루마리 사용** -> SCROLL_USED
    - 감정 가이드 표시 (WndStory, Document.GUIDE_IDING)

17. **물약 스폰** -> POTION_SPAWNED
    - 정화의 물약 스폰

18. **물약 힌트** -> POTION_HINT_SHOWN
    - "물약을 사용하세요" (WndTutorial)

19. **물약 사용** -> POTION_USED
    - 물약 사용 완료

#### 페이즈 5: 탐색 및 기습 공격

20. **탐색 페이지 스폰** -> SEARCH_PAGE_SPAWNED
    - '탐색' 페이지가 알코브에 배치

21. **탐색 가이드** -> SEARCH_GUIDE_SHOWN
    - '탐색' 스토리 윈도우 표시 (WndStory, Document.GUIDE_SEARCHING)

22. **탐색 힌트** -> SEARCH_HINT_SHOWN
    - "돋보기 버튼을 2번 눌러 탐색하세요" (WndTutorial)

23. **숨겨진 문 발견** -> DOOR_FOUND
    - 탐색으로 비밀 문 발견

24. **기습 가이드** -> SURPRISE_GUIDE_SHOWN (이동 제한 시작)
    - '기습' 스토리 윈도우 표시 (WndStory, Document.GUIDE_SURPRISE_ATKS)

25. **뱀 대기** -> WAIT_FOR_SNAKE (대기만 허용)
    - 뱀이 (10,3)에 스폰
    - "대기하세요" (WndTutorial)
    - 대기 버튼 깜빡임

26. **뱀이 문에 도착** -> SNAKE_AT_DOOR (공격만 허용)
    - 뱀이 (11,3)으로 이동하며 문 열기
    - "적을 공격하세요" (WndTutorial)

27. **뱀 처치** -> SNAKE_KILLED
    - 기습 공격으로 뱀 즉사

28. **완료** -> COMPLETED
    - 완료 메시지 (WndTutorial)
    - 타이틀 화면으로 이동

---

## 맵 좌표

```
WIDTH = 16, HEIGHT = 12

+----------------------------+
|                            |
|        [쥐](7,4)           |
|                            |
|  [페이지](9,3) [뱀](10,3) [문](11,3) [복도](12,3)
|                            |
|        [영웅](7,6)         |
|                            |
+----------------------------+
```

| 상수 | 좌표 | 설명 |
|-----|------|------|
| SPAWN_POS | (7, 6) | 영웅 시작 위치 |
| RAT_SPAWN_POS | (7, 4) | 쥐 스폰 위치 |
| ALCOVE_POS | (9, 3) | 탐색 페이지 위치 |
| EXTENDED_ALCOVE_POS | (10, 3) | 뱀 스폰 위치 |
| DOOR_POS | (11, 3) | 숨겨진 문 위치 |
| CORRIDOR_POS | (12, 3) | 복도 (기습 트리거) |

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|---------|
| `core/src/main/java/com/sacredpixel/sacredpixeldungeon/tutorial/TutorialManager.java` | 상태 관리, 액션 처리, 행동 제한 |
| `core/src/main/java/com/sacredpixel/sacredpixeldungeon/tutorial/TutorialState.java` | 상태 enum 정의 |
| `core/src/main/java/com/sacredpixel/sacredpixeldungeon/levels/TutorialLevel.java` | 맵 생성, 몬스터/아이템 스폰 |
| `core/src/main/java/com/sacredpixel/sacredpixeldungeon/windows/WndTutorial.java` | 튜토리얼 힌트 (OK 버튼) |
| `core/src/main/java/com/sacredpixel/sacredpixeldungeon/windows/WndStory.java` | 가이드 문서 (버튼 없음) |
| `core/src/main/java/com/sacredpixel/sacredpixeldungeon/ui/CellSelector.java` | 입력 제한 |
| `core/src/main/java/com/sacredpixel/sacredpixeldungeon/actors/mobs/Snake.java` | 기습 공격 로직 |

---

## 행동 제한 규칙

| 상태 | 이동 | 대기 | 공격 |
|-----|-----|-----|-----|
| SURPRISE_GUIDE_SHOWN | X | X | X |
| WAIT_FOR_SNAKE | X | O | X |
| SNAKE_AT_DOOR | X | X | O |
| 기타 | O | O | O |

---

## 교훈

### 1. 기습 공격 감지

**문제**: defenseSkill()에서 INFINITE_EVASION 반환 -> damage() 호출 전에 MISS

**해결**: fieldOfView 체크를 defenseSkill()에서 수행
```java
if (fieldOfView == null || !fieldOfView[enemy.pos]) {
    return 0; // 기습 - 명중
}
return INFINITE_EVASION; // 일반 - 회피
```

### 2. 몬스터 이동 + 문 열기

```java
int oldPos = pos;
move(DOOR_POS);           // 위치 변경 + 문 열기
moveSprite(oldPos, pos);  // 애니메이션
```

### 3. fieldOfView 타이밍

- super.act() 호출 전: fieldOfView = null
- super.act() 호출 후: fieldOfView 업데이트됨
- 기습 공격을 위해 뱀은 문에 도착 후 super.act() 호출하지 않음

### 4. 입력 제한 체크 포인트

- `CellSelector.onSelect()`: 마우스/터치 클릭
- `CellSelector.moveFromActions()`: 키보드 방향키
- 둘 다 체크해야 완전한 제한 가능

### 5. 창 Enter 키 처리

```java
public void onConfirm() {
    if (focusIndex >= 0 && focusIndex < focusableButtons.size()) {
        activateFocused();
    }
    // 포커스 없으면 아무것도 안함
}
```

---

## 테스트 체크리스트

- [ ] 튜토리얼 시작 (타이틀 화면)
- [ ] 가이드북 획득 -> 인트로 표시
- [ ] 저널/영웅정보 버튼 깜빡임 및 열기
- [ ] 쥐 스폰 및 조사/공격
- [ ] 두루마리/물약 사용
- [ ] 탐색 페이지 획득 -> 탐색 가이드
- [ ] 숨겨진 문 발견
- [ ] 기습 가이드 -> 이동 제한
- [ ] 대기 -> 뱀 이동
- [ ] 기습 공격 성공 (뱀 즉사)
- [ ] 완료 메시지 -> 타이틀 복귀

---
