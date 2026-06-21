# 073. 퀘스트 대화 닫기 시 진행

**날짜**: 2026-05-27

## 개요

WndQuest 창이 닫힐 때 퀘스트가 진행되도록 수정. ESC 키 또는 창 바깥 클릭으로 닫을 때도 대화가 "읽음" 처리됨.

---

## 변경 사항

### WndQuest.java

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/windows/WndQuest.java`

**변경 내용**: `onBackPressed()` 오버라이드 추가

```java
// WndQuest: ESC and outside click should advance the dialog (mark as read)
// This ensures quests progress regardless of how the player closes the dialog
@Override
public void onBackPressed() {
    advanceDialog = true;
    hide();
}
```

---

## 배경

### 기존 동작
- **ESC 키**: `onBackPressed()` → `hide()` (advanceDialog = false)
- **창 바깥 클릭**: `onBackPressed()` → `hide()` (advanceDialog = false)
- **창 안쪽 클릭**: 아무 일도 안 일어남 (WndQuest에 버튼 없음)
- **Enter 키**: 아무 일도 안 일어남 (focusableButtons 없음)

### 문제점
- ESC나 바깥 클릭으로 창을 닫으면 퀘스트가 진행되지 않음
- 플레이어가 대화를 읽었는데도 NPC가 계속 같은 대화를 반복
- "읽음" 처리가 안 되어 퀘스트 진행 불가

### 해결
- `onBackPressed()` 오버라이드하여 `advanceDialog = true` 설정
- 이제 ESC/바깥 클릭으로 닫아도 퀘스트 진행됨
- Enter/안쪽 클릭은 기존 동작 유지 (다른 창과 UX 통일)

---

## 관련 상속 구조

```
Window
  └── WndTitledMessage (advanceDialog 필드 보유)
        └── WndQuest (onBackPressed 오버라이드)
```

### Window.java 핵심 메서드
- `onBackPressed()`: ESC 키 및 바깥 클릭 시 호출. 기본 구현은 `hide()` 호출
- `onConfirm()`: Enter 키 시 호출. `focusableButtons`가 비어있으면 호출 안 됨

### WndTitledMessage.java
- `advanceDialog` 플래그 보유
- `hide()` 오버라이드: `advanceDialog == true`이면 `lastMessage = null` 설정하여 진행

---

## 테스트 케이스

- [ ] NPC 대화창에서 ESC 키로 닫기 → 퀘스트 진행됨
- [ ] NPC 대화창에서 바깥 클릭으로 닫기 → 퀘스트 진행됨
- [ ] NPC 대화창에서 안쪽 클릭 → 아무 일도 안 일어남 (기존 동작 유지)
- [ ] NPC 대화창에서 Enter 키 → 아무 일도 안 일어남 (기존 동작 유지)

---

## 수정된 파일

| File | Changes |
|------|---------|
| `core/.../windows/WndQuest.java` | `onBackPressed()` 오버라이드 추가 |

---
