# 060. SettingsScene 클릭 감지 수정

**날짜**: 2026-05-17

## 개요

SettingsScene에서 특정 UI 요소(전체화면 버튼, 슬라이더, 조작키 버튼)가 마우스 클릭에 반응하지 않던 문제 수정. ScrollPane과 동일한 패턴의 isActive() 오버라이드 버그.

---

## 변경 사항

### 수정된 버그

### 영향받은 UI 요소

| 탭 | 컴포넌트 | 증상 |
|---|---|---|
| 화면설정 (Display) | 전체화면 체크박스 | 클릭 무응답 |
| 인터페이스 설정 (UI) | 인터페이스 모드 슬라이더 | 클릭 무응답 |
| 인터페이스 설정 (UI) | 인터페이스 확대 슬라이더 | 클릭 무응답 |
| 입력 설정 (Input) | 조작키 설정 버튼 | 클릭 무응답 |

### 정상 작동하던 요소 (참고용)
- 화면설정 탭: 밝기, 시각 격자, 카메라 추적, 화면 흔들림 슬라이더
- 오디오 탭: 음악/효과음 슬라이더
- 언어 탭: 언어 버튼들

---

### 근본 원인 1: OptionSlider.isActive() 오버라이드 버그

### 문제 코드 (수정 전)

```java
// OptionSlider.java
@Override
public boolean isActive() {
    return active;  // super.isActive() 호출 누락!
}
```

### 원인 분석
- ScrollPane과 동일한 패턴의 버그
- `super.isActive()` 호출 누락으로 부모 체인 검사가 수행되지 않음
- PointerArea 클릭 감지 시 isActive()가 false 반환

### 수정 코드

```java
@Override
public boolean isActive() {
    // IMPORTANT: Must call super.isActive() for parent chain check
    // This is required for PointerArea click detection to work properly
    return super.isActive();
}
```

---

### 근본 원인 2: ItemButton.isActive() 오버라이드 버그

### 문제 코드 (수정 전)

```java
// ItemButton.java
@Override
public boolean isActive() {
    return active && visible;  // super.isActive() 호출 누락!
}
```

### 수정 코드

```java
@Override
public boolean isActive() {
    // IMPORTANT: Must call super.isActive() for parent chain check
    // This is required for PointerArea click detection to work properly
    return super.isActive() && visible;
}
```

---

---

## 수정된 파일

| File | Changes |
|------|---------|
| `core/.../ui/OptionSlider.java` | isActive()에 super.isActive() 호출 추가 |
| `core/.../ui/ItemButton.java` | isActive()에 super.isActive() 호출 추가 |
| `core/.../scenes/SettingsScene.java` | 탭 레이아웃 x 좌표 수정 |

---

### SettingsScene 레이아웃 수정 (보조)

SettingsScene의 탭 layout() 메서드에서 sep.x 좌표 설정 추가:

```java
// 수정 전
sep1.size(width, 1);
sep1.y = title.bottom() + 2;

// 수정 후
sep1.size(width, 1);
sep1.x = x;  // 추가
sep1.y = title.bottom() + 2;
```

영향받은 탭: DisplayTab, UITab, InputTab, AudioTab, LangsTab

---

## 테스트 결과

| 테스트 케이스 | 결과 |
|---------------|------|
| 화면설정 탭 전체화면 버튼 클릭 | ✅ 정상 |
| 인터페이스 모드 슬라이더 클릭 | ✅ 정상 |
| 인터페이스 확대 슬라이더 클릭 | ✅ 정상 |
| 조작키 설정 버튼 클릭 | ✅ 정상 |
| 오디오 탭 슬라이더 클릭 | ✅ 정상 |
| 언어 탭 버튼 클릭 | ✅ 정상 |

---

## 관련 버그 패턴

이 버그는 Changelog #83 (ScrollPane.isActive())와 동일한 패턴:

| 클래스 | 수정 전 | 수정 후 | Changelog |
|--------|---------|---------|-----------|
| ScrollPane | `return active && content.height() > height` | `return super.isActive()` | #83 |
| OptionSlider | `return active` | `return super.isActive()` | #84 |
| ItemButton | `return active && visible` | `return super.isActive() && visible` | #84 |

**공통 원인:** Focusable 인터페이스 구현 시 isActive()를 오버라이드하면서 Gizmo의 부모 체인 검사를 우회

---

## NEVER-CHANGE 업데이트

`NEVER-CHANGE.md`에 다음 항목 추가 필요:
- OptionSlider.isActive(): super.isActive() 호출 제거 금지
- ItemButton.isActive(): super.isActive() 호출 제거 금지

---
