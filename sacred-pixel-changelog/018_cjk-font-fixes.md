# 018. CJK 폰트 렌더링 수정

**날짜**: 2026-03-25

## 개요

간체/번체 중국어 글자가 깨지는 문제 수정. OTF→woff2 변환 및 폰트 폴백 순서 최적화.

---

## 변경 사항

### 1. SC/TC OTF 포맷 Canvas 호환성 문제

**증상:** 간체(简体中文)/번체(繁體中文)로 언어 설정 시 한자가 깨져서 표시됨.

**원인:** Noto Sans SC/TC가 noto-cjk 레포에서 다운로드한 **CID-keyed CFF OpenType (.otf, 16MB)** 형식. 이 형식은 CSS `@font-face`로 로드는 되지만, HTML5 Canvas 2D의 `ctx.fillText()`에서 글리프 렌더링 호환성 문제 발생.

반면 KR/JP는 google/fonts 레포에서 TTF→**woff2** 변환본을 사용하여 정상 동작.

**해결:** fonttools + brotli로 SC/TC OTF를 woff2로 변환.

| Font | Before | After |
|------|--------|-------|
| Noto Sans SC | 16MB OTF (CFF) | **10.9MB woff2** (TrueType) |
| Noto Sans TC | 16MB OTF (CFF) | **10.9MB woff2** (TrueType) |

### 2. CJK 폰트 폴백 순서 최적화

**증상:** 일본어/한국어 설정에서 일부 한자가 다른 형태로 표시됨. 특히 간체자(语, 设 등)가 KR/JP 폰트에서 먼저 검색되어 잘못된 글리프로 렌더링되거나 tofu 표시.

**원인:** CSS font-family 스택에서 KR/JP가 SC보다 앞에 위치. KR/JP 폰트(google/fonts 버전)는 각 언어 전용 서브셋이라 간체자를 포함하지 않음.

**해결:** `buildFontFamily()` 순서를 `[해당 언어 폰트] → SC(최대 한자 커버리지) → 나머지`로 변경.

```
일본어: NotoSansJP → NotoSansSC → NotoSansTC → NotoSansKR
한국어: NotoSansKR → NotoSansSC → NotoSansTC → NotoSansJP
번체:   NotoSansTC → NotoSansSC → NotoSansJP → NotoSansKR
간체:   NotoSansSC → NotoSansTC → NotoSansJP → NotoSansKR
기본:   NotoSansSC → NotoSansKR → NotoSansJP → NotoSansTC
```

**원칙:** Noto Sans SC(간체 중국어)가 Noto Sans CJK 시리즈 중 가장 넓은 한자 커버리지를 가지므로, 항상 2번째 이내에 폴백으로 배치.

---

## 수정된 파일

| File | Change |
|------|--------|
| `core/src/main/assets/fonts/noto-sans-sc.woff2` | OTF→woff2 변환 (신규) |
| `core/src/main/assets/fonts/noto-sans-tc.woff2` | OTF→woff2 변환 (신규) |
| `teavm/webapp/fonts/noto-sans-sc.woff2` | OTF→woff2 변환 (신규) |
| `teavm/webapp/fonts/noto-sans-tc.woff2` | OTF→woff2 변환 (신규) |
| `teavm/webapp/styles.css` | `format('opentype')` → `format('woff2')` |
| `teavm/webapp/index.html` | FontFace API URL `.otf` → `.woff2` |
| `teavm/src/.../TeaVMPlatformSupport.java` | `buildFontFamily()` 폴백 순서 수정 |

## Font Format Comparison

| Format | KR | JP | SC | TC |
|--------|----|----|----|----|
| **이전** | woff2 ✓ | woff2 ✓ | OTF ✗ | OTF ✗ |
| **이후** | woff2 ✓ | woff2 ✓ | woff2 ✓ | woff2 ✓ |

## Lesson Learned

- **Canvas 2D에서는 woff2(TrueType 기반)가 CFF OTF보다 안정적**
- noto-cjk 레포의 OTF는 데스크톱용 CID-keyed CFF — 웹 Canvas에서는 호환성 문제 가능
- google/fonts 레포의 TTF는 웹 최적화 — fonttools로 woff2 변환 시 안정적
- CJK 폰트 폴백에서 **SC를 항상 상위에 배치** — 가장 넓은 한자 커버리지
