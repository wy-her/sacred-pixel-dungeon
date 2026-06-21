# 025. 저널 UI 정비, 번역 완성, 창 크기 통일

**날짜**: 2026-03-27 ~ 2026-03-28

## 개요

저널 탭 UI 정비, 창 가로길이 통일, 전 언어 번역 완성 및 누락 키 추가.

---

## 변경 사항

### 1. 뱃지 탭 버튼 정비

**파일:** `WndJournal.java` (BadgesTab)

- "이번 도전" / "전체" 버튼에서 `Icons.BADGES` 아이콘 제거
- 버튼 글자크기를 WndSettings 버튼과 동일한 **7**로 변경 (기존 기본값 9)

```java
// Before
btnLocal = new RedButton(Messages.get(this, "this_run")) { ... };
btnLocal.icon(Icons.BADGES.get());

// After
btnLocal = new RedButton(Messages.get(this, "this_run"), 7) { ... };
// icon 제거
```

---

## 2. 저널 탭 제목 글자크기 통일

**파일:** `WndJournal.java`

WndSettings 탭 제목(size 8)과 동일하게 통일:

| 탭 | 변경 전 | 변경 후 |
|----|--------|--------|
| 모험 노트 (NotesTab) | `addHeader(..., 9, true)` | `addHeader(..., 8, true)` |
| 도감 대분류 4개 (CatalogTab) | `addHeader(..., 9, true)` | `addHeader(..., 8, true)` |

GuideTab은 ListTitle(size 8) 사용으로 이미 일치.

---

## 3. 던전 숙련 지침서 탭 제목 스타일 통일

**파일:** `ScrollingListPane.java` (ListTitle)

ListTitle의 레이아웃을 GridHeader(center=true)와 동일하게 변경:
- 검은 선(ColorBlock) 제거
- 높이: `ITEM_HEIGHT(18px)` 고정 → `text.height() + 3` (콘텐츠 기반)
- 위치: 수직 중앙 배치 → `y + 1` (GridHeader와 동일)
- `CENTER_ALIGN` 적용

```java
// Before
line = new ColorBlock(1, 1, 0xFF222222);  // 검은 선
label.setPos((width-label.width())/2f, y + (height() - label.height()) / 2f);

// After (line 제거)
label.align(RenderedTextBlock.CENTER_ALIGN);
label.setPos(x + (width - label.width())/2f, y + 1);
public float height() { return label.height() + 3; }
```

영향 범위: `addTitle()` 사용처 — GuideTab 제목, WndDocument 제목

---

## 4. 연금술/도감 탭 버튼 아이콘 축소

**파일:** `WndJournal.java` (AlchemyTab, CatalogTab)

ItemSprite 아이콘에 `scale.set(0.75f)` 적용 (16px → 12px):
- AlchemyTab: 9개 카테고리 버튼 아이콘
- CatalogTab: 4개 카테고리 버튼 아이콘 (장비/소모품/도감/문서)

---

## 5. 연금술 레시피 배치 통일

**파일:** `WndJournal.java` (AlchemyTab)

### 카테고리 버튼 배치
세로/가로 모두 **1줄 9개** 배치로 통일:
```java
// Before: if (width() >= 180) 분기로 가로 1줄 / 세로 5+4
// After: 항상 1줄
float buttonWidth = width()/pageButtons.length;
```

### 레시피 한 줄 개수
가로모드에서도 세로모드와 동일한 줄당 레시피 수:
```java
// Before: w + toAdd.get(0).width() <= width()
// After:  w + toAdd.get(0).width() <= WIDTH_P
```
가로모드에서는 같은 수의 레시피가 더 넓은 간격으로 배치됨.

---

## 6. 창 가로길이 통일

### WndSettings & WndJournal
| 창 | 세로모드 | 가로모드 |
|----|---------|---------|
| WndSettings | 137 → **147** | 250 (유지) |
| WndJournal | 141 → **147** | 242 → **250** |

### JournalScene (타이틀 화면 저널)
WndJournal과 동일하게 통일:
| | 변경 전 | 변경 후 |
|--|--------|--------|
| 세로 | 126 | **147** |
| 가로 | 216 | **250** |

### WndHero / WndHeroInfo
가로길이 130으로 변경 시도 → 특정 언어에서 레이아웃 깨짐 확인 → **120으로 원복**

---

## 7. WndHeroInfo TalentsPane 높이 최적화

**파일:** `WndHeroInfo.java` (TalentInfoTab)

TalentsPane 높이 하드코딩(120px) 제거 → 실제 콘텐츠 높이 사용:
```java
// Before
talentPane.setRect(0, message.bottom() + 3*MARGIN, width, 120);

// After
talentPane.setRect(0, message.bottom() + 3*MARGIN, width, 0);
talentPane.setSize(width, talentPane.content().height());
```
Tier 1+2 콘텐츠(~80px)에 맞춰 창 높이가 축소되어 불필요한 여백 제거.

---

## 8. WndHero StatsTab 레이아웃 조정

**파일:** `WndHero.java` (StatsTab)

### 2열 비율 변경
| | 변경 전 | 변경 후 |
|--|--------|--------|
| 라벨 영역 | 55% | **65%** |
| 값 영역 | 45% | **35%** |

폰트 자동축소 기준, 값 시작 위치 모두 65%/35%로 통일.

### 소지금 천단위 구분
`DateCompat.formatNumber()` 적용 (WndRanking과 동일 방식):
```java
// Before
statSlot(Messages.get(this, "gold"), Statistics.goldCollected);

// After
statSlot(Messages.get(this, "gold"), DateCompat.formatNumber(Statistics.goldCollected, Messages.locale()));
```
로케일에 따라 자동으로 구분 기호 적용 (한국어: 1,000 / 독일어: 1.000 등).

---

## 9. 전 언어 번역 완성

### 누락 키 추가
- `actors.mobs.npcs.vaultlaser.hit` → **21개 언어** 추가 (ko 제외 전체)
- `levels.vaultlevel$vaultflametrap.name/desc` → **be, el** 추가

### 미번역 값 번역
| 언어 | 번역 수 | 주요 내용 |
|------|---------|----------|
| de | 1 | vaultrat.desc |
| es | 7 | vault 관련 + key_distracted |
| it | 7 | vault 관련 + key_distracted |
| nl | 7 | vault 관련 + key_distracted |
| pl | 9 | vault + skeletonkey + champion_enemies |
| ru | 7 | vault 관련 + key_distracted |
| uk | 7 | vault 관련 + key_distracted |
| sv | 5 | talent + guidebook 제목 |
| el | 20 | UI/아이템/레벨 텍스트 |
| be | 88 | 저널/레벨/뱃지/도전/윈도우 |

**최종 검증:** 22개 언어 전체 missing=0, untranslated=0 확인.

---

## 변경 파일 목록

### Java
- `core/.../windows/WndJournal.java` — 뱃지 버튼, 탭 제목 크기, 아이콘 축소, 레시피 배치, 창 크기
- `core/.../windows/WndSettings.java` — WIDTH_P 변경
- `core/.../windows/WndHero.java` — StatsTab 레이아웃, 소지금 포맷
- `core/.../windows/WndHeroInfo.java` — TalentsPane 높이 최적화
- `core/.../scenes/JournalScene.java` — 타이틀 저널 창 크기
- `core/.../ui/ScrollingListPane.java` — ListTitle 스타일 변경

### Properties (번역)
- `actors/actors_*.properties` (22개 파일)
- `items/items_pl.properties`, `items/items_sv.properties`
- `journal/journal_sv.properties`
- `levels/levels_*.properties` (10개 파일)
- `misc/misc_be.properties`, `misc/misc_el.properties`, `misc/misc_pl.properties`
- `scenes/scenes_be.properties`, `scenes/scenes_el.properties`
- `ui/ui_be.properties`
- `windows/windows_be.properties`, `windows/windows_el.properties`
