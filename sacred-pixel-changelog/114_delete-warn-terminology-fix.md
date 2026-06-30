# 114. 삭제 경고창 용어 통일

**날짜**: 2026-06-30

## 개요

DataScene의 데이터 삭제 경고 메시지(`delete_warn`)에서 "high scores" 표현을 `best_record` 키와 일치하도록 수정. 전체 23개 언어에서 용어 통일.

---

## 변경 사항

### Localization (23 languages)

---

### [L-1] 문제점

`delete_warn` 메시지에서 사용하는 표현이 `best_record` 키의 표현과 불일치:

| 키 | 영어 | 한국어 |
|---|---|---|
| `best_record` | Best record | 최고 기록 |
| `delete_warn` (기존) | high scores | 최고 점수 |

---

### [L-2] 영어 (en)

**파일**: `core/src/main/assets/messages/scenes/scenes.properties`

```properties
# 변경 전
- Rankings & high scores

# 변경 후
- Rankings & best record
```

---

### [L-3] 한국어 (ko)

**파일**: `core/src/main/assets/messages/scenes/scenes_ko.properties`

```properties
# 변경 전
- 랭킹 및 최고 점수

# 변경 후
- 랭킹 및 최고 기록
```

---

### [L-4] 동아시아 언어 (2개)

| 언어 | 변경 전 | 변경 후 |
|------|---------|---------|
| 일본어 (ja) | ハイスコア | 最高記録 |
| 중국어 간체 (zh) | 最高分 | 最高记录 |
| 중국어 번체 (zh-hant) | 最高分 | 最高紀錄 |

---

### [L-5] 게르만어권 (4개)

| 언어 | 변경 전 | 변경 후 |
|------|---------|---------|
| German (de) | Highscores | bester Rekord |
| Dutch (nl) | hoogste scores | beste record |
| Swedish (sv) | högsta poäng | bästa rekord |

---

### [L-6] 로망스어권 (4개)

| 언어 | 변경 전 | 변경 후 |
|------|---------|---------|
| French (fr) | meilleurs scores | meilleur record |
| Spanish (es) | puntuaciones altas | mejor récord |
| Italian (it) | punteggi migliori | miglior record |
| Portuguese (pt) | pontuações mais altas | melhor recorde |

---

### [L-7] 슬라브어권 (5개)

| 언어 | 변경 전 | 변경 후 |
|------|---------|---------|
| Russian (ru) | рекорды | лучший рекорд |
| Ukrainian (uk) | найкращі результати | найкращий рекорд |
| Belarusian (be) | найлепшыя вынікі | найлепшы рэкорд |
| Polish (pl) | najlepsze wyniki | najlepszy rekord |
| Czech (cs) | nejvyšší skóre | nejlepší rekord |

---

### [L-8] 기타 언어 (6개)

| 언어 | 변경 전 | 변경 후 |
|------|---------|---------|
| Greek (el) | υψηλότερες βαθμολογίες | καλύτερο ρεκόρ |
| Hungarian (hu) | legjobb eredmények | legjobb rekord |
| Turkish (tr) | en yüksek puanlar | en iyi rekor |
| Indonesian (in) | skor tertinggi | rekor terbaik |
| Vietnamese (vi) | điểm cao | kỷ lục tốt nhất |
| Esperanto (eo) | plej altaj poentoj | plej bona rekordo |

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `messages/scenes/scenes.properties` | high scores → best record |
| `messages/scenes/scenes_be.properties` | найлепшыя вынікі → найлепшы рэкорд |
| `messages/scenes/scenes_cs.properties` | nejvyšší skóre → nejlepší rekord |
| `messages/scenes/scenes_de.properties` | Highscores → bester Rekord |
| `messages/scenes/scenes_el.properties` | υψηλότερες βαθμολογίες → καλύτερο ρεκόρ |
| `messages/scenes/scenes_eo.properties` | plej altaj poentoj → plej bona rekordo |
| `messages/scenes/scenes_es.properties` | puntuaciones altas → mejor récord |
| `messages/scenes/scenes_fr.properties` | meilleurs scores → meilleur record |
| `messages/scenes/scenes_hu.properties` | legjobb eredmények → legjobb rekord |
| `messages/scenes/scenes_in.properties` | skor tertinggi → rekor terbaik |
| `messages/scenes/scenes_it.properties` | punteggi migliori → miglior record |
| `messages/scenes/scenes_ja.properties` | ハイスコア → 最高記録 |
| `messages/scenes/scenes_ko.properties` | 최고 점수 → 최고 기록 |
| `messages/scenes/scenes_nl.properties` | hoogste scores → beste record |
| `messages/scenes/scenes_pl.properties` | najlepsze wyniki → najlepszy rekord |
| `messages/scenes/scenes_pt.properties` | pontuações mais altas → melhor recorde |
| `messages/scenes/scenes_ru.properties` | рекорды → лучший рекорд |
| `messages/scenes/scenes_sv.properties` | högsta poäng → bästa rekord |
| `messages/scenes/scenes_tr.properties` | en yüksek puanlar → en iyi rekor |
| `messages/scenes/scenes_uk.properties` | найкращі результати → найкращий рекорд |
| `messages/scenes/scenes_vi.properties` | điểm cao → kỷ lục tốt nhất |
| `messages/scenes/scenes_zh.properties` | 最高分 → 最高记录 |
| `messages/scenes/scenes_zh-hant.properties` | 最高分 → 最高紀錄 |

---
