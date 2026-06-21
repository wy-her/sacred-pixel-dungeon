# 028. 텍스트 렌더링 수정, 번역 정비, UI 마무리

**날짜**: 2026-03-29

## 개요

하이라이트 텍스트 뒤 불필요한 띄어쓰기 수정, CJK 문자 간격 조정, 긴 버튼 텍스트 단축, 번역 전수 검사.

---

## 변경 사항

### 1. Highlight 텍스트 뒤 불필요한 띄어쓰기 수정

**파일:** `ui/RenderedTextBlock.java`

### 문제
`_highlighted text_` 뒤에 바로 이어지는 조사/구두점과의 사이에 불필요한 시각적 공백 발생.
- 한국어: `_인장_을` → "인장 을" (조사 앞 공백)
- 모든 언어: `_소검_,` → "소검 ," (쉼표 앞 공백)

### 원인
각 `RenderedText` 단어의 `width()`에 border padding이 포함됨. Highlight 토글(`_`) 경계에서 분리된 인접 단어 사이에 SPACE 토큰이 없어 패딩 보상이 적용되지 않음.

### 해결
`HIGHLIGHT_BRIDGE` 센티널 토큰 방식 도입:

1. **build():** highlight 종료 직후 텍스트 토큰 앞에 `HIGHLIGHT_BRIDGE` 마커 삽입
2. **layout():** `HIGHLIGHT_BRIDGE`를 만나면 `x -= (2*borderExcess - charGap)` 적용
3. **CJK 예외:** 일본어/중국어에서는 이전 문자의 CJK 보상이 이미 적용되므로 추가 보상 없음
4. **grouping 루프:** `HIGHLIGHT_BRIDGE`를 건너뛰도록 수정

```java
// build() - highlight 종료 후 다음 텍스트 앞에 마커 삽입
if (justEndedHighlight) {
    words.add(HIGHLIGHT_BRIDGE);
}

// layout() - 언어별 보상량
if (word == HIGHLIGHT_BRIDGE) {
    if (JA/ZH) { /* no subtraction - CJK compensation handles it */ }
    else       { x -= (2*borderExcess - charGap); }
}
```

---

## 2. CJK 문자 간격 조정

**파일:** `ui/RenderedTextBlock.java`

### 중국어/일본어 문자 간격 축소
- `borderExcess * 0.7f` → **`0.90f`** (더 촘촘한 문자 간격)

### 일본어/중국어 SPACE 간격
- 기존: 모든 언어 동일 `scaledSpaceW` (음수값 → CJK에서 간격 소멸)
- 수정: 일본어/중국어에서 SPACE 토큰은 `charGap` 사용 (작지만 보이는 양수 간격)

```java
if (JA/ZH/ZH-TW) {
    x += charGap;  // small positive gap
} else {
    x += scaledSpaceW;  // negative compensation for border padding
}
```

---

## 3. 일본어 전각 스페이스 교체

**파일:** 5개 일본어 properties 파일

전각 스페이스(U+3000) → 반각 스페이스(U+0020) 일괄 교체: **28개**

| 파일 | 교체 수 |
|------|---------|
| windows/windows_ja.properties | 6 |
| actors/actors_ja.properties | 8 |
| items/items_ja.properties | 1 |
| journal/journal_ja.properties | 12 |
| scenes/scenes_ja.properties | 1 |

---

## 4. 긴 버튼 텍스트 단축 (14개 언어, 100+ 변경)

### 독일어 (DE) — 아이템 액션
| 이전 | 변경 | 영어 |
|------|------|------|
| FALLEN LASSEN | **ABLEGEN** | DROP |
| AUSRÜSTEN | **ANLEGEN** | EQUIP |
| ENTFERNEN | **LÖSEN** | DETACH |
| IDENTIFIZIEREN | **ERKENNEN** | IDENTIFY |
| AKTIVIEREN | **AKTIV.** | ACTIVATE |
| EINGRAVIEREN | **GRAVIEREN** | INSCRIBE |
| ZERSCHMETTERN | **BRECHEN** | SHATTER |
| VORHERSEHUNG | **SEHEN** | SCRY |
| ANZÜNDEN & WERFEN | **ZÜND. & WERF.** | LIGHT & THROW |

### 독일어 (DE) — UI 버튼/제목
| 이전 | 변경 |
|------|------|
| Einstellungen (WndGame/TitleScene) | **Optionen** |
| Gruppiert (toolbar) | **Gruppe** |
| Werkzeugleiste-Einst. (toolbar settings) | 유지 |
| Voreinstellung Tastaturbelegung | **Standard-Belegung** |
| Anzeige-Einstellungen | **Anzeige-Einst.** |
| Interface-Einstellungen | **Interface-Einst.** |
| Controller-Einstellungen | **Controller-Einst.** |
| Audio-Einstellungen | **Audio-Einst.** |
| Verbrauchsgegenstände | **Verbrauchbares** |

### 러시아어 (RU)
| 이전 | 변경 |
|------|------|
| ИСПОЛЬЗОВАТЬ (ac_zap x3) | **РАЗРЯД** |
| ПОДЖЕЧЬ И БРОСИТЬ | **ЗАЖЕЧЬ И БРОС.** |
| Подтвердить (confirm) | **Принять** |

### 벨라루스어 (BE)
| 이전 | 변경 |
|------|------|
| ВЫКАРЫСТАЦЬ (ac_zap x3) | **РАЗРАД** |
| ПРАЧЫТАЦЬ (ac_read x2) | **ЧЫТАЦЬ** |
| ПАДПАЛІЦЬ І КІНУЦЬ | **ЗАПАЛ. І КІН.** |
| Падзяліць/Згрупіраваць/Цэнтраваць (toolbar) | **Дзял./Груп./Цэнтр** |
| Пацвярдзіць (confirm) | **Прыняць** |

### 우크라이나어 (UK)
| 이전 | 변경 |
|------|------|
| ВИСТРІЛИТИ (ac_zap x2) | **РОЗРЯД** |
| ПРОЧИТАТИ (ac_read x2) | **ЧИТАТИ** |
| Підтвердити (confirm) | **Прийняти** |

### 스페인어 (ES)
QUITAR SELLO→**SOLTAR**, DESEQUIPAR→**QUITAR**, ENCENDER Y LANZAR→**ENCEND. Y LANZ.**

### 프랑스어 (FR)
DÉSÉQUIPER→**RETIRER**, CHOISISSEZ UNE CAPACITÉ→**CAPACITÉ**, enable_news/amuletscene.exit 단축, ALLUMER & JETER→**ALLUM. & JETER**

### 포르투갈어 (PT)
DESEQUIPAR→**REMOVER**, ACENDER & JOGAR→**ACEND. & JOGAR**

### 네덜란드어 (NL)
NEEM TERUG→**UITTREK**, IDENTIFICEREN→**HERKEN**, MAAK LOS→**LOSMAAK**, STEEK AAN & GOOI→**AANSTK. & GOOI**, toolbar: Verdelen→**Deel**, Groeperen→**Groep**, Centreren→**Centr.**

### 스웨덴어 (SV)
ANVÄND INTE→**AVLÄGG**, toolbar_settings→**Verktygsfältsinst.**

### 그리스어 (EL)
ΧΡΗΣΙΜΟΠΟΙΗΣΕ→**ΧΡΗΣΗ** (x4), ΕΝΕΡΓΟΠΟΙΗΣΕ→**ΕΝΕΡΓ.**, ΑΠΟΚΟΛΛΗΣΗ→**ΛΥΣΗ**, Επιβεβαίωση→**Αποδοχή**

### 헝가리어 (HU)
MEGGYÚJTOM ÉS ELDOBOM→**GYÚJTS & DOBJ**, Csoportos→**Csop.**, Jóváhagyás→**Jóváhagy**

### 폴란드어 (PL)
RZUĆ ZAKLĘCIE→**RZUĆ** (x3)

### 에스페란토 (EO)
toolbar: Disigita→**Divid.**, Kunigita→**Kunig.**, Centrigita→**Centro**

### 인도네시아어 (IN)
NYALAKAN & LEMPAR→**BAKAR & LEMPAR**, toolbar: Terpisah→**Pisah**

### 이탈리아어 (IT)
toolbar: Centrata→**Centro**, ACCENDI e LANCIA→**ACCENDI & LANC.**

### 터키어 (TR)
toolbar: Paylaştır→**Böl**

### 체코어 (CS)
ZAPÁLIT A HODIT→**ZAPAL. A HODIT**

### Edit Title (15개 언어)
DE→Titel bearb., FR→Mod. titre, RU→Ред. название, IT→Mod. Titolo, NL→Titel wijzig, UK→Ред. назву, VI→Sửa tiêu đề, TR→Başlık Düz., SV→Red. titel, EO→Red. titolon, CS→Upr. nadpis, JA→題名編集, ES→Ed. Título, PT→Ed. Título, BE→Зм. Назву

### Energize All/1 (10개 언어)
FR, DE, IT, SV, HU, UK, IN, BE, RU, ES — 모두 단축

---

## 5. 번역 전수 검사

- **140개 이슈** 발견 및 수정 (3회 반복 검증)
- 21개 언어 × 6개 missing keys (test_zone, replay 관련) 추가
- 14개 untranslated 값 번역 (goo.defeated, alarm_status, annoying.msg 등)
- 최종 검증: **0개 남음**

---

## 6. RedButton 텍스트 초과 판정 완화

**파일:** `ui/RedButton.java`

- `availW = width - componentWidth - bg.marginHor() - 2` → **`+ 2`**
- border padding 감안하여 시각적 초과와 논리적 초과 차이 보정

---

## 7. RightClickMenu 수정

**파일:** `ui/RightClickMenu.java`

- 제목 + 모든 버튼 텍스트: 노란색 → **흰색** (`textColor(Window.WHITE)`)
- 검은색 가로 구분선: `separator.visible = false`
- `multiline = true` 설정 → auto-shrink 비활성화 (동적 크기 버튼)

---

## 8. 기타 UI 변경

### WndUseItem 동적 크기 버튼
- `multiline = true` 설정 → auto-shrink 비활성화
- WndGameInProgress 도전 버튼 동일 처리

### 한국어 텍스트
- "게임 진행 중" → **"진행 중 게임"** (StartScene 제목)
- "도전" → **"도전 항목"** (WndGame 인게임 메뉴)

### StartScene 슬롯 버튼
- SLOT_WIDTH: 120 → **135** (+12.5%)

---

## 수정 파일 목록

### Java
| 파일 | 변경 |
|------|------|
| `ui/RenderedTextBlock.java` | HIGHLIGHT_BRIDGE, CJK 간격, JA/ZH SPACE |
| `ui/RedButton.java` | availW 판정 완화 |
| `ui/RightClickMenu.java` | 흰색 폰트, 구분선 숨김, multiline |
| `windows/WndUseItem.java` | multiline=true |
| `windows/WndGameInProgress.java` | 도전 버튼 multiline=true |
| `scenes/StartScene.java` | SLOT_WIDTH 135 |

### Properties (번역/텍스트)
- 14개 언어 items 파일 (아이템 액션 단축)
- 9개 언어 windows 파일 (toolbar/confirm/energize 단축)
- 15개 언어 ui 파일 (Edit Title 단축)
- 10개 언어 windows 파일 (Energize All/1 단축)
- 5개 일본어 파일 (전각 스페이스 교체)
- 21개 언어 scenes/windows 파일 (missing keys 추가)
- `scenes/scenes_ko.properties` (진행 중 게임)
- `scenes/scenes_de.properties` (Optionen)
- `windows/windows_de.properties` (Optionen, Einst., Verbrauchbares)
- `scenes/scenes_fr.properties` (enable_news, amuletscene.exit)
