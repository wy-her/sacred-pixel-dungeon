# 062. UI 포커스 및 DataScene 개선

**날짜**: 2026-05-17

## 개요

WndChallenges 포커스 복원 수정, WndHeroInfo TalentInfoTab 초기 포커스 제거, WndInfoSubclass/WndInfoArmorAbility Enter 선택 수정, DataScene 데이터 정보 표시 개선.

---

## 변경 사항

### WndChallenges 포커스 복원 수정

### 문제
도전과제 창에서 상세 설명 창을 열었다가 닫을 때 이전 포커스 위치가 복원되지 않음.

### 원인
`descriptionWindow.alive` 체크가 작동하지 않음. 로그 분석 결과 `alive=true, parent=false` 상태가 관찰됨.
창이 닫힐 때 `parent`가 `null`이 되지만 `alive`는 `true`로 유지됨.

### 수정
**파일:** `WndChallenges.java`

```java
// 변경 전
if (descriptionWindow != null && !descriptionWindow.alive) {

// 변경 후
if (descriptionWindow != null && descriptionWindow.parent == null) {
```

---

### WndHeroInfo TalentInfoTab 초기 포커스 제거

### 문제
TalentInfoTab 선택 시 자동으로 첫 번째 특성에 포커스가 설정됨.

### 수정
**파일:** `WndHeroInfo.java`

탭 선택 시 `initializeFocus()` 대신 `clearFocus()` 호출하여 초기 포커스 없음 상태로 시작.

```java
// 변경 전
if (talentInfo.talentPane != null) {
    talentInfo.talentPane.initializeFocus();
}

// 변경 후
if (talentInfo.talentPane != null) {
    talentInfo.talentPane.clearFocus();
}
```

---

### WndInfoSubclass, WndInfoArmorAbility Enter 선택 수정

### 문제
1. 키보드로 포커싱은 되지만 Enter로 선택이 안됨
2. Enter를 누르면 창이 닫힘

### 원인
부모 클래스 `WndTitledMessage.onConfirm()`이 `hide()`를 호출하여 창을 닫음.

### 수정
**파일:** `WndInfoSubclass.java`, `WndInfoArmorAbility.java`

`onConfirm()` 오버라이드하여 포커스된 버튼이 있으면 활성화, 없으면 아무 동작 안함.

```java
@Override
public void onConfirm() {
    // If there's a focused button, activate it instead of closing
    if (focusIndex >= 0 && focusIndex < focusableButtons.size()) {
        activateFocused();
    }
    // Don't close the window on Enter when there are focusable elements
}
```

---

### WndTabbed 하위 창 열림 시 탭 전환 차단 강화

### 문제
WndInfoSubclass/WndInfoArmorAbility에서 특성 설명 창이 열려 있을 때 Space바를 누르면 WndHeroInfo의 탭이 변경됨.

### 수정
**파일:** `WndTabbed.java`

`tabListener`에 `isTopmost()` 체크 추가.

```java
// 변경 전
if (!WndTabbed.this.active) return false;

// 변경 후
if (!WndTabbed.this.active || !WndTabbed.this.isTopmost()) return false;
```

---

### DataScene 데이터 정보 표시 개선

### 변경 사항

**파일:** `DataScene.java`

1. **statSlot 패턴 적용**: 단일 RenderedTextBlock 대신 각 항목을 개별 RenderedTextBlock으로 렌더링하여 `GAP = 4` 픽셀 여백 적용

2. **가져오기 힌트 글자 크기**: 6pt → 7pt

3. **WndImportPreview**: preview 문자열을 `\n`으로 분리하여 각 줄을 개별 렌더링하고 GAP 여백 적용

```java
// 새로 추가된 메서드
private float createSummaryStats(float x, float startY, int maxWidth) { ... }
private float statSlot(float x, float pos, int maxWidth, String text) { ... }
```

---

---

## 수정된 파일

| File | Changes |
|------|---------|
| `WndChallenges.java` | 포커스 복원 조건 수정 (`parent == null`) |
| `WndHeroInfo.java` | TalentInfoTab 초기 포커스 제거 |
| `WndInfoSubclass.java` | `onConfirm()` 오버라이드 |
| `WndInfoArmorAbility.java` | `onConfirm()` 오버라이드 |
| `WndTabbed.java` | `isTopmost()` 체크 추가 |
| `DataScene.java` | statSlot 패턴, 글자 크기 증가 |

---

## 관련 문서

- `키보드 접근성 기획 - 반영 결과.md` 업데이트 필요 (항목 29, 30 추가)

---
