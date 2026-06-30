# 115. 일지 Intros "Dungeon" 초기 상태 수정

**날짜**: 2026-06-30

## 개요

일지(Journal)의 INTROS 문서에서 "Dungeon" 항목의 초기 상태를 `READ`에서 `NOT_FOUND`로 변경. 던전 입장 시 발견하도록 수정.

---

## 변경 사항

### Document.java

---

### [D-1] 문제점

"Dungeon" 항목이 초기 상태가 `READ`로 하드코딩되어 있어, 게임 데이터가 없는 최초 플레이 상태에서도 일지에서 해당 항목이 이미 열람 가능한 상태였음.

Sacred Pixel Dungeon에서는 던전 최초 입장 시 인트로 메시지로 발견되도록 구현되어 있으나, 초기 상태가 `READ`여서 처음부터 보이는 문제 발생.

---

### [D-2] 수정 내용

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/journal/Document.java`

```java
// 변경 전
INTROS.pagesStates.put("Dungeon",   READ);

// 변경 후
INTROS.pagesStates.put("Dungeon",   debug ? READ : NOT_FOUND);
```

**참고**: "Tutorial" 항목은 기본 제공 정보로서 `READ` 상태를 유지.

---

### [D-3] 동작 변경

| 빌드 | Dungeon | Tutorial |
|------|---------|----------|
| 릴리스 | 던전 입장 시 발견 | 즉시 열람 가능 (기본 제공) |
| 디버그 | 즉시 열람 가능 | 즉시 열람 가능 |

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `journal/Document.java` | Dungeon 초기 상태 READ → NOT_FOUND |

---
