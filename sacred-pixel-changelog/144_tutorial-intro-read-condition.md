# 144. 튜토리얼 인트로 읽음 조건 수정

**날짜**: 2026-07-26

## 배경

저널 카탈로그의 Region Introduction 문서에서 "Tutorial" (7번 welcome) 페이지가 튜토리얼 플레이 여부와 관계없이 항상 "읽음" 상태로 표시되었다. 다른 INTROS 페이지들(Dungeon~Halls)은 해당 지역에 진입해야 읽음 처리되는 것과 달리, Tutorial만 하드코딩되어 있었다.

---

## 변경 사항

### 1. 초기 상태를 다른 INTROS 페이지와 동일하게 변경

```diff
# Document.java:299
- INTROS.pagesStates.put("Tutorial", READ);
+ INTROS.pagesStates.put("Tutorial", debug ? READ : NOT_FOUND);
```

### 2. 튜토리얼 격리 상태에서도 읽음 상태 유지

튜토리얼 모드는 저널 상태를 격리(백업 → 초기화 → 복원)하므로, 튜토리얼 스토리 표시 시 백업된 상태에도 읽음을 기록해야 한다.

```java
// TutorialManager.java - 새 메서드 추가
public static void markTutorialStoryRead() {
    // 현재 상태에 기록
    Document.INTROS.readPage("Tutorial");

    // 격리 모드라면 백업 상태에도 기록
    if (savedJournalState != null && savedJournalState.contains("documents")) {
        Bundle docsBundle = savedJournalState.getBundle("documents");
        Bundle introsBundle;
        if (docsBundle.contains("INTROS")) {
            introsBundle = docsBundle.getBundle("INTROS");
        } else {
            introsBundle = new Bundle();
            docsBundle.put("INTROS", introsBundle);
        }
        introsBundle.put("Tutorial", Document.READ);
    }
}
```

### 3. 튜토리얼 스토리 표시 시 메서드 호출

```diff
# InterlevelScene.java:367-374
  if (tutorialLevel && mode == Mode.DESCEND) {
      isStoryFloor = true;
      createStoryElements("Tutorial");
+     TutorialManager.markTutorialStoryRead();
  }
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `journal/Document.java:299` | Tutorial 초기 상태를 `debug ? READ : NOT_FOUND`로 변경 |
| `tutorial/TutorialManager.java:156-175` | `markTutorialStoryRead()` 메서드 추가 |
| `scenes/InterlevelScene.java:373` | 튜토리얼 스토리 표시 시 메서드 호출 |

---

## 동작 변경

| 시나리오 | 변경 전 | 변경 후 |
|----------|---------|---------|
| 게임 최초 실행 | READ | NOT_FOUND |
| 튜토리얼 미플레이 | READ (카탈로그에 표시) | NOT_FOUND (카탈로그에 미표시) |
| 튜토리얼 플레이 | READ | READ |
| 튜토리얼 종료 후 | READ | READ (백업 상태에서 유지) |
| 디버그 모드 | READ | READ |

---

## 영향

- 튜토리얼을 플레이하지 않은 사용자는 카탈로그에서 Tutorial 항목을 볼 수 없음
- 튜토리얼을 플레이한 사용자만 해당 문서가 "읽음"으로 표시됨
- 다른 INTROS 페이지들과 동일한 동작 방식으로 통일

---

## 관련 코드

- `Document.java:293-299` - INTROS 페이지 초기화
- `TutorialManager.java:97-142` - 튜토리얼 저널 격리/복원 로직
- `InterlevelScene.java:367-374` - 튜토리얼 스토리 표시
