# 110. DataScene 가져오기 관련 버그 수정

**날짜**: 2026-06-30

## 개요

DataScene의 데이터 가져오기 관련 버그 수정: 문서(Lore) 개수 카운트 오류, Import 미리보기 줄간격 불일치, 브라우저 리사이즈 후 ESC 키 작동 안됨 버그.

---

## 변경 사항

### Bug Fixes (3)

---

### [B-1] WebDataImporter.java - 문서(Lore) 개수 카운트 범위 수정

**문제**: Export 창에서는 Lore 개수가 30개, Import 창에서는 31개로 표시됨

**원인 분석**:
- Export: `countDocumentPagesInRange(30, 59)` - INTROS Tutorial 제외, HALLS_KING 마지막 페이지 포함
- Import: `countDocumentPagesInRange(29, 58)` - INTROS Tutorial 포함, HALLS_KING 마지막 페이지 제외

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/services/updates/WebDataImporter.java`

**수정**: Import 범위를 Export와 동일하게 30-59로 변경

```java
// 기존 코드 (line 177)
result.loreCount = countDocumentPagesInRange(result.document, 29, 58);

// 수정 코드
result.loreCount = countDocumentPagesInRange(result.document, 30, 59);
```

---

### [B-2] DataScene.java - Import 미리보기 줄간격 통일

**문제**: Export 창과 Import 창의 미리보기 텍스트 줄간격이 다름

**원인 분석**:
- Export: 단일 `RenderedTextBlock`으로 렌더링 (내부 `\n`으로 줄바꿈)
- Import: `split("\n")`으로 분리 후 각 줄마다 `+2pt` 여백 추가

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/scenes/DataScene.java`

**수정**: Import 미리보기도 단일 `RenderedTextBlock`으로 통일

```java
// 기존 코드 (WndImportPreview 내부)
String[] lines = preview.split("\n");
for (String line : lines) {
    RenderedTextBlock txt = PixelScene.renderTextBlock(line, 7);
    txt.maxWidth(width - 10);
    txt.setPos(0, previewY);
    add(txt);
    previewY += 2 + txt.height();
}

// 수정 코드
RenderedTextBlock previewText = PixelScene.renderTextBlock(preview, 7);
previewText.maxWidth(width - 10);
previewText.setPos(0, titlebar.bottom() + 4);
add(previewText);
float btnY = previewText.bottom() + 4;
```

---

### [B-3] DataScene.java - 브라우저 리사이즈 후 ESC 키 버그 수정

**문제**: Import 다이얼로그가 열린 상태에서 브라우저 크기를 조절하면, 창이 사라진 후 ESC 키가 작동하지 않음

**원인 분석**:
1. `importDialogShown` 플래그가 `static`이고 리셋되지 않음
2. `keyListener`가 Window 존재만 확인하고, `active` 상태는 확인하지 않음
3. 브라우저 리사이즈 시 `seamlessResetScene()`이 호출되어 Window가 제거되지만, 비활성 Window 참조가 남아있을 수 있음

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/scenes/DataScene.java`

**수정 1**: `keyListener`에서 Window.active 상태 확인 추가

```java
// 기존 코드 (line 267-270)
// Don't handle keys if a window is open
for (Object v : members) {
    if (v instanceof Window) return false;
}

// 수정 코드
// Don't handle keys if an active window is open
for (Object v : members) {
    if (v instanceof Window && ((Window) v).active) return false;
}
```

**수정 2**: `destroy()` 메서드에서 `importDialogShown` 리셋

```java
// 기존 코드 (line 322-329)
@Override
public void destroy() {
    if (keyListener != null) {
        KeyEvent.removeKeyListener(keyListener);
        keyListener = null;
    }
    super.destroy();
}

// 수정 코드
@Override
public void destroy() {
    if (keyListener != null) {
        KeyEvent.removeKeyListener(keyListener);
        keyListener = null;
    }
    importDialogShown = false;
    super.destroy();
}
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `services/updates/WebDataImporter.java:177` | loreCount 범위 29-58 → 30-59 |
| `scenes/DataScene.java:573-586` | Import 미리보기 단일 RenderedTextBlock으로 변경 |
| `scenes/DataScene.java:267-270` | Window.active 상태 확인 추가 |
| `scenes/DataScene.java:328` | destroy()에서 importDialogShown 리셋 |
| `teavm/.../WebDataServiceImpl.java:92-133` | 스탯 간 구분자 `\n` → `\n\n` 변경 |
| `scenes/DataScene.java:574-584` | WndImportPreview에서 `\n\n`으로 분리 후 GAP 적용 |

---
