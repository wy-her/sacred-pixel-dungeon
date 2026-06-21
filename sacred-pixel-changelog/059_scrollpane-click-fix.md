# 059. 스크롤 패널 클릭 감지 수정

**날짜**: 2026-05-16

## 개요

ScrollPane 내부 컴포넌트 클릭이 작동하지 않던 5가지 문제를 단일 근본 원인 수정으로 해결. Focusable 인터페이스 구현 시 isActive() 오버라이드가 부모 체인 검사를 우회하는 문제 발견 및 수정.

---

## 변경 사항

### 수정된 버그

### 영향받은 UI 컴포넌트

| 위치 | 컴포넌트 | 증상 |
|------|----------|------|
| WndRanking → Talents 탭 | TalentsPane | 탤런트 버튼 클릭 무응답 |
| WndRanking → Badges 탭 | BadgesList | 배지 아이콘 클릭 무응답 |
| JournalScene → 확인된 항목 → 문서 | ScrollingGridPane | 문서 아이콘 클릭 무응답 |
| JournalScene → 가이드 탭 | ScrollingListPane | 가이드 아이콘 클릭 무응답 |
| JournalScene/WndJournal → 연금술 탭 | AlchemyTab | 레시피 아이콘 클릭 무응답 |

### 정상 작동하던 컴포넌트 (참고용)
- WndRanking → Items 탭: ScrollPane을 사용하지 않고 ItemButton 직접 사용
- JournalScene → 무기/적 카테고리: 클릭 정상 작동

---

### 근본 원인

### 문제 코드 (수정 전)

```java
// ScrollPane.java - Focusable 인터페이스 구현
@Override
public boolean isActive() {
    return active && content.height() > height;  // super.isActive() 호출 누락!
}
```

### 원인 분석

1. `ScrollPane`이 `Focusable` 인터페이스를 구현하면서 `isActive()`를 오버라이드
2. 하지만 `super.isActive()`를 호출하지 않아 **부모 체인 활성화 검사가 누락**됨
3. `PointerArea.onSignal()`에서 클릭 이벤트 처리 시 `isActive()` 호출
4. `Gizmo.isActive()`는 자신과 모든 부모의 `active` 상태를 확인
5. ScrollPane의 오버라이드가 이 검사를 우회하여 항상 `false` 반환
6. 결과: ScrollPane 내부의 모든 클릭 이벤트가 무시됨

### 디버그 로그 (문제 상황)

```
[PointerController.isActive] INACTIVE! Tracing parent chain:
  - this.active=true
  - 0: TalentsPane.active=true
  - 1: TalentsTab.active=true
  - 2: WndRanking.active=true
  - 3: RankingsScene.active=true
```

모든 부모의 `.active` 필드는 `true`였지만, `isActive()` 메서드는 `false`를 반환.

---

### 수정 내용

### 파일: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/ui/ScrollPane.java`

**변경 전:**
```java
@Override
public boolean isActive() {
    return active && content.height() > height;
}
```

**변경 후:**
```java
@Override
public boolean isActive() {
    // IMPORTANT: Must call super.isActive() for parent chain check
    // The content.height() > height check is for Focusable interface only,
    // but it was breaking PointerArea click detection
    return super.isActive();
}

// For Focusable interface - whether this pane can be keyboard-focused
public boolean canKeyboardFocus() {
    return active && content.height() > height;
}
```

### 설계 결정

1. `isActive()`: Gizmo의 부모 체인 검사를 유지하여 클릭 감지 정상화
2. `canKeyboardFocus()`: 새 메서드로 분리하여 키보드 포커스 조건 별도 관리
   - 컨텐츠가 뷰포트보다 클 때만 키보드 포커스 허용 (스크롤 가능할 때)

---

---

## 영향 범위

### 수정된 동작
- ScrollPane 기반의 모든 UI 컴포넌트에서 마우스 클릭 정상 작동
- TalentsPane, BadgesList, ScrollingGridPane, ScrollingListPane 등

### 변경 없는 동작
- 키보드 네비게이션 (canKeyboardFocus()로 분리됨)
- 스크롤 동작 (ZOOM_IN/ZOOM_OUT, 방향키)
- 드래그 스크롤

---

---

## 테스트 결과

| 테스트 케이스 | 결과 |
|---------------|------|
| WndRanking Talents 탭 탤런트 클릭 | ✅ 정상 |
| WndRanking Badges 탭 배지 클릭 | ✅ 정상 |
| JournalScene 문서 아이콘 클릭 | ✅ 정상 |
| JournalScene 가이드 아이콘 클릭 | ✅ 정상 |
| 연금술 탭 레시피 클릭 | ✅ 정상 |
| 키보드 방향키 스크롤 | ✅ 정상 |
| 마우스 드래그 스크롤 | ✅ 정상 |

---

---

## 수정된 파일

| File | Changes |
|------|---------|
| `core/.../ui/ScrollPane.java` | isActive() 수정, canKeyboardFocus() 추가 |

---

## 관련 문서 업데이트

- `NEVER-CHANGE.md`: ScrollPane.isActive() 수정 금지 사항 추가
- `키보드 접근성 기획 - 반영 결과.md`: canKeyboardFocus() 메서드 문서화

---
