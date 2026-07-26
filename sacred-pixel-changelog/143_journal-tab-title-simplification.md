# 143. 저널 탭 제목 키 통합 및 간소화

**날짜**: 2026-07-26

## 배경

저널의 각 탭에서 제목이 중복으로 정의되어 있거나, 불필요하게 장황한 문서 제목이 사용되고 있었음. 이로 인해:
- 번역 파일에 동일한 값을 가진 키가 중복 존재
- "Tome of Dungeon Mastery" / "던전 숙련 지침서" 같은 장황한 제목 사용
- 유지보수 부담 증가

---

## 변경 사항

### 1. BadgesTab - `title_main_menu` 키 제거

`title`과 `title_main_menu` 키가 동일한 값을 가지므로 통합.

```diff
# WndJournal.java:1649
- title = PixelScene.renderTextBlock(Messages.get(this, "title_main_menu"), 8);
+ title = PixelScene.renderTextBlock(Messages.get(this, "title"), 8);
```

```diff
# windows*.properties (23개 파일)
  windows.wndjournal$badgestab.title=Badges
- windows.wndjournal$badgestab.title_main_menu=Badges
```

### 2. GuideTab - 문서 제목 대신 탭 제목 사용

장황한 문서 제목("Tome of Dungeon Mastery") 대신 간결한 탭 제목("Dungeon Guide") 사용.

```diff
# WndJournal.java:699
- list.addTitle(Document.ADVENTURERS_GUIDE.title());
+ list.addTitle(Messages.get(this, "title"));
```

```diff
# journal*.properties (23개 파일)
- journal.document.adventurers_guide.title=Tome of Dungeon Mastery
  journal.document.adventurers_guide.intro.title=Introduction
- journal.document.adventurers_guide.intro.body=...you are reading the Tome of Dungeon Mastery!...
+ journal.document.adventurers_guide.intro.body=...welcome to this guidebook!...
```

### 3. AlchemyTab - 문서 제목 대신 탭 제목 사용

GuideTab과 동일한 방식으로 개선.

```diff
# AlchemyScene.java:530
- return Messages.titleCase(Document.ALCHEMY_GUIDE.title());
+ return Messages.get(WndJournal.AlchemyTab.class, "title");
```

```diff
# journal*.properties (23개 파일)
- journal.document.alchemy_guide.title=Alchemy Guide
  journal.document.alchemy_guide.potions.title=Intro and Potions
```

---

## 수정된 파일

### Java 코드
| 파일 | 변경 내용 |
|------|----------|
| `windows/WndJournal.java` | `title_main_menu` → `title` 키 사용 (line 1649) |
| `〃` | `Document.ADVENTURERS_GUIDE.title()` → `Messages.get(this, "title")` (line 699) |
| `scenes/AlchemyScene.java` | `Document.ALCHEMY_GUIDE.title()` → `Messages.get(WndJournal.AlchemyTab.class, "title")` (line 530) |

### 번역 파일 (23개 언어)
| 파일 패턴 | 변경 내용 |
|----------|----------|
| `windows/windows*.properties` | `title_main_menu` 키 삭제 (23개 파일) |
| `journal/journal*.properties` | `adventurers_guide.title` 키 삭제, `intro.body` 텍스트 간소화 (23개 파일) |
| `〃` | `alchemy_guide.title` 키 삭제 (23개 파일) |

---

## 제거된 번역 키

| 키 | 변경 이유 |
|----|----------|
| `windows.wndjournal$badgestab.title_main_menu` | `title` 키와 동일하여 통합 |
| `journal.document.adventurers_guide.title` | 탭 제목 키(`guidetab.title`) 재사용 |
| `journal.document.alchemy_guide.title` | 탭 제목 키(`alchemytab.title`) 재사용 |

---

## 영향

- 번역 파일에서 중복 키 제거로 유지보수 간소화
- 저널 내 제목 표시 일관성 향상
- 장황한 제목("Tome of Dungeon Mastery" 등)이 간결한 제목("Dungeon Guide")으로 통일
- 23개 언어 × 3개 키 = 69개 불필요한 키 제거

---

## 관련 문서

- `WndJournal.java` - 저널 윈도우 구현
- `AlchemyScene.java:530` - 연금술 가이드 버튼 hover 텍스트
- `Document.java:225` - 문서 제목 조회 메서드

