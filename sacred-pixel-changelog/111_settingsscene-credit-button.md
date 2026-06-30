# 111. SettingsScene 크레딧 버튼 높이 통일

**날짜**: 2026-06-30

## 개요

SettingsScene과 WndSettings의 LangsTab 크레딧 버튼 높이를 통일.

---

## 변경 사항

### UI 개선 (1)

---

### [U-1] SettingsScene.java - 크레딧 버튼 높이 조정

**문제**: SettingsScene의 크레딧 버튼 높이(16px)가 WndSettings의 크레딧 버튼 높이(11px)와 다름

**비교 분석**:

| 항목 | SettingsScene | WndSettings |
|------|---------------|-------------|
| 버튼 높이 | `BTN_HEIGHT` (16) | `LANG_BTN_HEIGHT` (11) |
| 배경 스타일 | `WndLangCredits` 클래스 | `Chrome.Type.TOAST` 인라인 |
| 텍스트 크기 | 6pt | 6pt |

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/scenes/SettingsScene.java`

**수정**: 버튼 높이를 `BTN_HEIGHT`에서 `LANG_BTN_HEIGHT`로 변경

```java
// 기존 코드 (line 1261)
btnCredits.setSize(width, BTN_HEIGHT);

// 수정 코드
btnCredits.setSize(width, LANG_BTN_HEIGHT);
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `scenes/SettingsScene.java:1261` | 크레딧 버튼 높이 BTN_HEIGHT(16) → LANG_BTN_HEIGHT(11) |

---
