# 092. 튜토리얼 숨겨진 문 수정

**날짜**: 2026-06-14

## 개요

튜토리얼 레벨에서 숨겨진 문이 조기 발견되는 버그 수정 및 조기 발견 시 튜토리얼 진행 보장.

---

## 변경 사항

### 문제점

### 1. 인트로 종료 시 비밀문 자동 발견
일반 게임 1층에서 신규 플레이어가 가이드북을 획득하면 `endIntro()`가 호출되어 입구방의 숨겨진 문들을 자동으로 발견시킴. 그러나 이 로직이 튜토리얼 레벨에도 적용되어, 플레이어가 직접 찾아야 할 비밀문까지 자동 발견됨.

**원인 코드 (GameScene.java):**
```java
//clear hidden doors, it's floor 1 so there are only the entrance ones
for (int i = 0; i < Dungeon.level.length(); i++){
    if (Dungeon.level.map[i] == Terrain.SECRET_DOOR){
        Dungeon.level.discover(i);
        discoverTile(i, Terrain.SECRET_DOOR);
    }
}
```

### 2. 조기 발견 시 튜토리얼 진행 불가
플레이어가 `SEARCH_HINT` 단계 이전에 숨겨진 문을 먼저 발견하면, 해당 단계에서 `HIDDEN_DOOR_FOUND` 액션이 무시되어 튜토리얼이 막힘.

## 수정 내용

### 파일 1: GameScene.java

**변경 1:** TutorialLevel import 추가
```java
import com.sacredpixel.sacredpixeldungeon.levels.TutorialLevel;
```

**변경 2:** `endIntro()`에서 튜토리얼 레벨 예외 처리
```java
//clear hidden doors, it's floor 1 so there are only the entrance ones
//skip this in TutorialLevel as it has a secret door the player should find
if (!(Dungeon.level instanceof TutorialLevel)) {
    for (int i = 0; i < Dungeon.level.length(); i++){
        if (Dungeon.level.map[i] == Terrain.SECRET_DOOR){
            Dungeon.level.discover(i);
            discoverTile(i, Terrain.SECRET_DOOR);
        }
    }
}
```

### 파일 2: TutorialManager.java

**변경 1:** Terrain import 추가
```java
import com.sacredpixel.sacredpixeldungeon.levels.Terrain;
```

**변경 2:** `SEARCH_GUIDE_CLOSED` 핸들러에서 문 발견 여부 확인
```java
case SEARCH_GUIDE_CLOSED:
    if (state == TutorialState.SEARCH_GUIDE_SHOWN) {
        // Check if door is already discovered (player found it early)
        if (Dungeon.level.map[TutorialLevel.DOOR_POS] != Terrain.SECRET_DOOR) {
            // Door already found, skip search hint and go directly to DOOR_FOUND
            setState(TutorialState.DOOR_FOUND);
        } else {
            // Show search hint immediately after guide is closed
            setState(TutorialState.SEARCH_HINT);
            GameScene.show(WndTutorial.createSearchHint());
            flashExamine();
        }
    }
    break;
```

## 동작 방식

### 수정 1: 비밀문 자동 발견 방지
| 레벨 타입 | endIntro() 동작 |
|----------|----------------|
| 일반 1층 | 입구방 비밀문 자동 발견 (기존 동작 유지) |
| **튜토리얼** | **비밀문 자동 발견 건너뜀** |

### 수정 2: 조기 발견 시 단계 건너뛰기
| 상황 | 결과 |
|------|------|
| 문이 아직 숨겨져 있음 | 정상 흐름: `SEARCH_HINT` → 탐색 힌트 표시 |
| 문이 이미 발견됨 | 건너뛰기: 바로 `DOOR_FOUND`로 진행 |

---

## 수정된 파일

| File | Changes |
|------|---------|
| `GameScene.java` | endIntro()에서 TutorialLevel 예외 처리 |
| `TutorialManager.java` | SEARCH_GUIDE_CLOSED 핸들러에서 조기 발견 처리 |

---

## 테스트 항목
1. 튜토리얼에서 가이드북 획득 시 비밀문이 자동 발견되지 않는지 확인
2. 정상 흐름: 탐색 힌트 단계에서 의도적 탐색으로 비밀문 발견
3. 조기 발견: 탐색 힌트 이전에 비밀문 발견 시 튜토리얼 진행 확인
4. 일반 게임 1층: 기존 동작(입구방 비밀문 자동 발견) 유지 확인

---
