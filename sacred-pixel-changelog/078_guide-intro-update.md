# 078. 가이드 소개 텍스트 업데이트

**날짜**: 2026-05-30

## 개요

던전 가이드 소개 문서의 오래된 텍스트 제거 및 changelog 파일 인덱싱 통일.

---

## 변경 사항

### 1. 던전 가이드 소개 문서 수정

### 문제
- 가이드 문서가 표시될 때 이제는 저널 버튼이 깜빡이지 않고 직접 표시됨
- 기존 소개 문서에 "저널 버튼이 깜빡이면 선택하세요" 라는 오래된 안내가 남아있었음

### 해결
모든 언어 파일(23개)에서 저널 버튼 깜빡임 관련 문구 제거, 유실된 페이지 안내만 유지.

### 수정된 파일

| 파일 | 언어 |
|------|------|
| journal.properties | English |
| journal_ko.properties | 한국어 |
| journal_de.properties | Deutsch |
| journal_es.properties | Español |
| journal_fr.properties | Français |
| journal_it.properties | Italiano |
| journal_ja.properties | 日本語 |
| journal_zh.properties | 简体中文 |
| journal_zh-hant.properties | 繁體中文 |
| journal_pt.properties | Português |
| journal_ru.properties | Русский |
| journal_pl.properties | Polski |
| journal_nl.properties | Nederlands |
| journal_tr.properties | Türkçe |
| journal_sv.properties | Svenska |
| journal_hu.properties | Magyar |
| journal_in.properties | Bahasa Indonesia |
| journal_eo.properties | Esperanto |
| journal_vi.properties | Tiếng Việt |
| journal_uk.properties | Українська |
| journal_be.properties | Беларуская |
| journal_el.properties | Ελληνικά |
| journal_cs.properties | Čeština |

### 변경 예시 (한국어)

**변경 전:**
```
(일지 버튼이 빛난다면, 버튼을 선택해 가이드북이 당신에게 해주고 싶은 말을 읽으세요! 몇몇 페이지는 유실된 것 같은데, 던전에서 찾을 수 있지 않을까요?)
```

**변경 후:**
```
(몇몇 페이지는 유실된 것 같은데, 던전에서 찾을 수 있지 않을까요?)
```

### 2. Changelog 파일 인덱싱 변경

모든 changelog 파일의 번호 형식을 3자리로 통일:
- `0x_` → `00x_`
- `xx_` → `0xx_`
- `1xx_` → 그대로

---

## 수정된 파일

| File | Changes |
|------|---------|
| `messages/journal/journal_*.properties` (23개) | 저널 버튼 깜빡임 관련 문구 제거 |

---
