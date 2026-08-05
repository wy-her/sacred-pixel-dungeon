# 151. 22개 언어 번역 품질 검수 및 개선

**날짜**: 2026-08-03

## 배경

한국어 번역에서 '영혼의 매' 갑옷 능력의 '매의 눈' 특성 설명에서 일부 항목에만 "기존"이라는 단어가 포함되어 일관성이 없는 문제가 발견되었습니다. 이를 계기로 전체 22개 언어(영어 제외)에 대한 종합적인 번역 품질 검수를 진행했습니다.

---

## 변경 사항

### 1. 언어별 주요 개선

다수의 언어에서 오타, 번역 표현, 플레이스홀더 형식 등을 개선했습니다.

#### BE (벨라루스어)
- boss 번호 개선: "другога боса" → "чацвёртага боса" (2번째 → 4번째)
- 방향 표현 개선: "вышэй" → "ніжэй" (위로 → 아래로)

#### CS (체코어)
- 철자 패턴 개선: "Zvyva" → "Zbývá"
- 클래스명 철자: "Kerik" → "Klerik"
- 기타 철자: "magickyým", "scchopnosti", "pooškození" 등

#### DE (독일어)
- `wandoflightning.eleblast_desc`: 기절 지속시간 "für 5 Züge" 추가

#### EL (그리스어)
- champion_1 배지: "Αργυρός" → "Χάλκινος" (Silver → Bronze)

#### EO (에스페란토)
- double space 개선, ordinal suffix "-a" 추가
- 철자: "apaŭ" → "apenaŭ"

#### FR (프랑스어)
- 핵심 철자: "Tous restants" → "Tours restants" (모든 → 턴)

#### IN (인도네시아어)
- 의미 개선: "udara" → "air" (공기 → 물, 불 끄는 맥락)
- 철자: "Perkenakan" → "Perkenalan", "Membaut" → "Membuat"

#### KO (한국어)
- 띄어쓰기: "방어막을얻습니다" → "방어막을 얻습니다"
- 동사 표현: "입이며" → "입으며"
- 중복 제거: "4타일 내 내" → "4타일 내"
- 철자: "글씌" → "글씨"
- 번역 표현: "느림" → "낮음" (Visual Grid 설정)

#### RU (러시아어)
- 플레이스홀더 공백: "%2$dурона" → "%2$d урона" 등
- 단어 중복 제거: "зависит зависит" → "зависит", "но но" → "но"
- 철자: "изучается" → "излучается", "совершет" → "совершает"

#### SV (스웨덴어)
- 배지 번역 개선: "Nybörjarforskare" → "Skicklig forskare"
- 철자: "exlirer" → "elixir"

#### TR (터키어)
- 플레이스홀더 형식: "-%%%3$s" → "%3$s%%"
- 철자: "Metini" → "Metni"

#### UK (우크라이나어)
- 의미 개선: "Боягуз" → "Пацифіст" (Coward → Pacifist)
- 의미 개선: "Вбитий чарами" → "Задихнувся" (Killed by spells → Suffocated)
- 챔피언 번역: "чемпіон-ніцшеанець" → "зростаючий чемпіон"
- 철자: "енегрія" → "енергія", "маютьі" → "мають"

#### VI (베트남어)
- 발음부호 개선: "tam trí" → "tâm trí", "mặt tời" → "mặt trời"
- 문법 개선: "có thể kĩ năng" → "có thể thực hiện"
- 중복 제거: "của bạn của bạn" → "của bạn"

#### ZH (중국어 간체)
- WndScoreBreakdown: "通关倍率" → "通关奖励"
- 이중 구두점 제거: "余烬.。" → "余烬。"

#### ZH-HANT (중국어 번체)
- 숫자 개선: "100個敵人" → "500個敵人"
- WndScoreBreakdown: "通關倍率" → "通關獎勵"
- 철자: "Yender" → "Yendor", "治療虊水" → "治療藥水"

### 2. 용어 통일 (5개 언어)

`scenes.datascene.delete_warn`에서 사용하는 용어를 다른 키와 일치시켰습니다.

| 언어 | 기존 | 개선 | 기준 키 |
|------|------|------|---------|
| HU | Rangsorok | Eredmények | titlescene.rankings |
| IT | Classifiche | Risultati | titlescene.rankings |
| RU | Рейтинги | Рекорды | titlescene.rankings |
| SV | Märken | Emblem | wndranking.badges |
| VI | Huy hiệu | Các huy chương | wndranking.badges |

---

## 수정된 파일

### scenes 카테고리
| 파일 | 변경 내용 |
|------|----------|
| `scenes/scenes_be.properties` | exit_desc 방향 개선 |
| `scenes/scenes_cs.properties` | daily_repeat 철자 |
| `scenes/scenes_eo.properties` | descend/resurrect 개선 |
| `scenes/scenes_hu.properties` | delete_warn 용어 통일 |
| `scenes/scenes_it.properties` | delete_warn 용어 통일 |
| `scenes/scenes_ko.properties` | Visual Grid "느림" → "낮음" |
| `scenes/scenes_nl.properties` | changesbutton 철자 |
| `scenes/scenes_ru.properties` | delete_warn 용어 통일 |
| `scenes/scenes_sv.properties` | delete_warn 용어 통일 |
| `scenes/scenes_uk.properties` | save_warning 철자 |
| `scenes/scenes_vi.properties` | delete_warn 용어 통일, 철자 |

### actors 카테고리
| 파일 | 변경 내용 |
|------|----------|
| `actors/actors_cs.properties` | trinity/shadowclone/holyweapon 철자 |
| `actors/actors_fr.properties` | "Tous restants" → "Tours restants" |
| `actors/actors_in.properties` | burning.desc 의미 개선, 철자 |
| `actors/actors_ko.properties` | 띄어쓰기, 동사 표현, 중복 제거 |
| `actors/actors_nl.properties` | adrenaline.desc 공백 |
| `actors/actors_ru.properties` | 플레이스홀더 공백, 철자, 단어 중복 |
| `actors/actors_tr.properties` | weakness.desc 포맷 |
| `actors/actors_uk.properties` | championenemy, ascensionchallenge, daze 철자 |

### 기타 카테고리
| 파일 | 변경 내용 |
|------|----------|
| `items/items_de.properties` | wandoflightning "5 Züge" 추가 |
| `items/items_fr.properties` | blandfruit 철자 |
| `items/items_ko.properties` | regionlorepage "글씌" → "글씨" |
| `items/items_ru.properties` | smokebomb, elixirofmight 플레이스홀더/중복 |
| `items/items_tr.properties` | chaliceofblood 플레이스홀더 |
| `items/items_vi.properties` | affection, roguearmor, capeofthorns |
| `items/items_zh.properties` | embers 이중 구두점 |
| `journal/journal_in.properties` | intro, stones 철자 |
| `journal/journal_it.properties` | halls_king 문법 |
| `journal/journal_sv.properties` | brews_elixirs 철자 |
| `journal/journal_zh-hant.properties` | Yender → Yendor |
| `levels/levels_be.properties` | exit_desc 방향 |
| `levels/levels_cs.properties` | citylevel 철자 |
| `levels/levels_eo.properties` | vaultlevel 철자 |
| `levels/levels_pt.properties` | caveslevel 철자 |
| `levels/levels_uk.properties` | cavesbosslevel 철자 |
| `misc/misc_cs.properties` | rodney, victory, researcher 철자/발음부호 |
| `misc/misc_el.properties` | champion_1 Bronze/Silver |
| `misc/misc_eo.properties` | champion_enemies 이중 콤마 |
| `misc/misc_sv.properties` | researcher_2 번역 개선 |
| `misc/misc_uk.properties` | no_monsters_slain, death_from_gas 의미 개선 |
| `misc/misc_zh-hant.properties` | monsters_slain_5 숫자, no_healing 철자 |
| `plants/plants_cs.properties` | earthroot 철자 |
| `plants/plants_pt.properties` | rotberry 철자 |
| `plants/plants_uk.properties` | starflower 마크다운 공백 |
| `plants/plants_vi.properties` | sungrass 철자 |
| `ui/ui_be.properties` | unlock_tier4 boss 번호 |
| `ui/ui_nl.properties` | updatenotification, customnotebutton 철자 |
| `ui/ui_tr.properties` | customnotebutton 철자 |
| `ui/ui_uk.properties` | talentspane 공백 |
| `windows/windows_zh.properties` | wndscorebreakdown 倍率 → 奖励 |
| `windows/windows_zh-hant.properties` | wndscorebreakdown, wndreforge 철자 |

---

## 영향

- 22개 언어에서 다수의 번역 품질 개선
- 플레이스홀더 형식 개선으로 표시 문제 해결
- 의미 및 표현 개선으로 사용자 경험 향상
- 용어 일관성 향상으로 UX 개선

---

## 관련 문서

- #066 - 번역 명확화 작업
- #037 - 로컬라이제이션 정리
