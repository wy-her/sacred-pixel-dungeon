# 033. 문서 폰트 참조 업데이트, OFL 라이센스 추가

**날짜**: 2026-04-02

## 개요

README.md 및 문서의 폰트 참조 업데이트, Inter 및 Noto Sans용 OFL 라이센스 파일 추가.

---

## 변경 사항

### 1. README.md 폰트 참조 업데이트

### 변경
- **기술 하이라이트 섹션** (Line 51):
  - 변경 전: `NeoDunggeunmoPro pixel font + system font toggle`
  - 변경 후: `Inter + Noto Sans (CJK support) via CSS font-family stack`

- **크레딧 섹션** (Line 62):
  - 변경 전: `[NeoDungGeunMoPro](https://neodgm.dalgona.dev/) by Dalgona`
  - 변경 후: `[Inter](https://rsms.me/inter/) by Rasmus Andersson, [Noto Sans](https://fonts.google.com/noto/specimen/Noto+Sans) by Google`

**파일:** `README.md`

---

## 2. README-HTML5.md 폰트 렌더링 설명 업데이트

### 변경
- **Font Rendering 섹션** (Line 40):
  - 변경 전: `FreeTypeFontGenerator constructor selects CSS fontFamily based on filename`
  - 변경 후: `FreeTypeFontGenerator constructor sets CSS fontFamily (Inter, Noto Sans CJK variants)`

**파일:** `README-HTML5.md`

---

## 3. docs/recommended-changes.md 폰트 시스템 가이드 업데이트

### 변경
- **Font System 설명** (Lines 19-21):
  - 변경 전: `NeoDunggeunmoPro (pixel-style Korean font)` + `system-ui, sans-serif`
  - 변경 후: `Inter (Latin) + Noto Sans (CJK: KR, JP, SC, TC)` + `7-font CSS font-family stack`

- **폰트 변경 가이드** (Lines 23-28):
  - 변경 전: 4단계 (woff2 추가 → @font-face 업데이트 → fontFamily 업데이트 → preload 업데이트)
  - 변경 후: 5단계 (양쪽 폴더에 woff2 추가 → styles.css 업데이트 → CSS_FONT_FAMILY 업데이트 → preload 업데이트 → **OFL.txt 추가**)

**파일:** `docs/recommended-changes.md`

---

## 4. OFL.txt 폰트 라이센스 파일 추가

### 추가
Inter와 Noto Sans 모두 SIL Open Font License 1.1 적용.

**위치:**
- `core/src/main/assets/fonts/OFL.txt`
- `teavm/webapp/fonts/OFL.txt`

**내용:**
```
Copyright 2020 The Inter Project Authors (https://github.com/rsms/inter)
Copyright 2014-2021 Google Inc. (Noto Sans)

This Font Software is licensed under the SIL Open Font License, Version 1.1.
...
```

---

## 수정 파일 목록

| 파일 | 변경 |
|------|------|
| `README.md` | 폰트 크레딧 및 기술 설명 업데이트 |
| `README-HTML5.md` | FreeTypeFontGenerator 설명 업데이트 |
| `docs/recommended-changes.md` | 폰트 시스템 설명 및 변경 가이드 업데이트 |
| `core/src/main/assets/fonts/OFL.txt` | 신규 추가 (SIL OFL 1.1) |
| `teavm/webapp/fonts/OFL.txt` | 신규 추가 (SIL OFL 1.1) |
