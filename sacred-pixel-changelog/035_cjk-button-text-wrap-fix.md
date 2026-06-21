# 035. CJK 버튼 텍스트 줄바꿈 수정

**날짜**: 2026-04-02

## 개요

CJK 언어(한국어, 일본어, 중국어)에서 버튼 텍스트가 줄바꿈되지 않는 문제 수정.

---

## 변경 사항

### 문제

텐구의 가면 착용 후 세부 직업 선택, 드워프 왕관 착용 후 갑옷 특성 선택 창에서:
- CJK 언어(한국어, 일본어, 중국어)의 버튼 텍스트가 줄바꿈되지 않음
- 텍스트가 한 줄로 표시되어 버튼 영역을 넘어감
- 버튼 크기가 동적으로 조절되지 않음
- 라틴어 기반 언어(영어 등)는 정상 작동

---

## 원인

**파일:** `core/src/main/java/com/sacredpixel/sacredpixeldungeon/ui/StyledButton.java:73`

```java
// 문제 코드
if (multiline && !Messages.lang().isAsian()) {
    text.maxWidth((int)(width - componentWidth - bg.marginHor() - 2));
}
```

`!Messages.lang().isAsian()` 조건으로 인해 CJK 언어에서는 `maxWidth`가 설정되지 않음.
- `maxWidth`가 없으면 텍스트가 줄바꿈 기준을 알 수 없음
- 결과적으로 텍스트가 한 줄로 렌더링됨

---

## 수정

```java
// 변경 전
if (multiline && !Messages.lang().isAsian()) {

// 변경 후
if (multiline) {
```

모든 언어에서 `multiline` 버튼에 `maxWidth`를 설정하도록 변경.

---

## 영향 받는 UI

- `WndChooseSubclass` - 텐구의 가면 세부 직업 선택
- `WndChooseAbility` - 드워프 왕관 갑옷 특성 선택
- 기타 `multiline = true`를 사용하는 모든 `StyledButton`

---

## 수정 파일

| 파일 | 변경 |
|------|------|
| `core/.../ui/StyledButton.java` | Line 73: CJK 제외 조건 제거 |
