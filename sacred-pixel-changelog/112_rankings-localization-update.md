# 112. 랭킹 텍스트 로컬라이제이션 업데이트

**날짜**: 2026-06-30

## 개요

DataScene의 랭킹 정보 표시 형식을 전체 언어에서 통일. 괄호 형식을 하이픈 형식으로 변경하고, 한국어/일본어/중국어의 "최고" 표현을 개선.

---

## 변경 사항

### Localization (23 languages)

---

### [L-1] 랭킹 메시지 형식 및 텍스트 변경

**변경 내용**: `(Best: X)` 형식을 `- Best record: X` 형식으로 통일

**변경 전**:
```
Rankings: 5 records
(Best: 12,345)
```

**변경 후**:
```
Rankings: 5 records
- Best record: 12,345
```

---

### [L-2] 한국어 (ko) 개선

**파일**: `core/src/main/assets/messages/scenes/scenes_ko.properties`

**변경 내용**:
1. "순위" → "랭킹" (일관성)
2. "최고점수" → "최고 기록" (띄어쓰기 + 표현 변경)

```properties
# 변경 전
scenes.datascene.rankings=순위: %d개\n(최고점수: %,d점)

# 변경 후
scenes.datascene.rankings=랭킹: %d개\n- 최고 기록: %,d점
```

---

### [L-3] 일본어 (ja) 개선

**파일**: `core/src/main/assets/messages/scenes/scenes_ja.properties`

**변경 내용**: "最高" → "最高記録" (명확성 + 표현 변경)

```properties
# 변경 전
scenes.datascene.rankings=ランキング: %d件\n(最高: %,d)

# 변경 후
scenes.datascene.rankings=ランキング: %d件\n- 最高記録: %,d
```

---

### [L-4] 중국어 간체 (zh) 개선

**파일**: `core/src/main/assets/messages/scenes/scenes_zh.properties`

**변경 내용**: "最高" → "最高记录" (명확성 + 표현 변경)

```properties
# 변경 전
scenes.datascene.rankings=排行榜: %d 条记录\n(最高: %,d)

# 변경 후
scenes.datascene.rankings=排行榜: %d 条记录\n- 最高记录: %,d
```

---

### [L-5] 중국어 번체 (zh-hant) 개선

**파일**: `core/src/main/assets/messages/scenes/scenes_zh-hant.properties`

**변경 내용**: "最高" → "最高紀錄" (명확성 + 표현 변경)

```properties
# 변경 전
scenes.datascene.rankings=排行榜: %d 條記錄\n(最高: %,d)

# 변경 후
scenes.datascene.rankings=排行榜: %d 條記錄\n- 最高紀錄: %,d
```

---

### [L-6] 베트남어 (vi) 개선

**파일**: `core/src/main/assets/messages/scenes/scenes_vi.properties`

**변경 내용**: "Cao nhất" → "Kỷ lục tốt nhất" (Best record로 표현 통일)

```properties
# 변경 전
scenes.datascene.rankings=Xếp hạng: %d bản ghi\n(Cao nhất: %,d)

# 변경 후
scenes.datascene.rankings=Xếp hạng: %d bản ghi\n- Kỷ lục tốt nhất: %,d
```

---

### [L-7] 기타 언어들 (17개)

다음 언어들은 괄호 형식을 하이픈 형식으로만 변경:

| 언어 | 파일 |
|------|------|
| English | scenes.properties |
| Belarusian | scenes_be.properties |
| Czech | scenes_cs.properties |
| German | scenes_de.properties |
| Greek | scenes_el.properties |
| Esperanto | scenes_eo.properties |
| Spanish | scenes_es.properties |
| French | scenes_fr.properties |
| Hungarian | scenes_hu.properties |
| Indonesian | scenes_in.properties |
| Italian | scenes_it.properties |
| Dutch | scenes_nl.properties |
| Polish | scenes_pl.properties |
| Portuguese | scenes_pt.properties |
| Russian | scenes_ru.properties |
| Swedish | scenes_sv.properties |
| Turkish | scenes_tr.properties |
| Ukrainian | scenes_uk.properties |

**공통 변경 패턴**:
```properties
# 변경 전
scenes.datascene.rankings=... (Best: %,d)

# 변경 후
scenes.datascene.rankings=... - Best record: %,d
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `messages/scenes/scenes.properties` | 괄호 → 하이픈 |
| `messages/scenes/scenes_be.properties` | 괄호 → 하이픈 |
| `messages/scenes/scenes_cs.properties` | 괄호 → 하이픈 |
| `messages/scenes/scenes_de.properties` | 괄호 → 하이픈 |
| `messages/scenes/scenes_el.properties` | 괄호 → 하이픈 |
| `messages/scenes/scenes_eo.properties` | 괄호 → 하이픈 |
| `messages/scenes/scenes_es.properties` | 괄호 → 하이픈 |
| `messages/scenes/scenes_fr.properties` | 괄호 → 하이픈 |
| `messages/scenes/scenes_hu.properties` | 괄호 → 하이픈 |
| `messages/scenes/scenes_in.properties` | 괄호 → 하이픈 |
| `messages/scenes/scenes_it.properties` | 괄호 → 하이픈 |
| `messages/scenes/scenes_ja.properties` | 괄호 → 하이픈, 最高 → 最高記録 |
| `messages/scenes/scenes_ko.properties` | 괄호 → 하이픈, 순위 → 랭킹, 최고점수 → 최고 기록 |
| `messages/scenes/scenes_nl.properties` | 괄호 → 하이픈 |
| `messages/scenes/scenes_pl.properties` | 괄호 → 하이픈 |
| `messages/scenes/scenes_pt.properties` | 괄호 → 하이픈 |
| `messages/scenes/scenes_ru.properties` | 괄호 → 하이픈 |
| `messages/scenes/scenes_sv.properties` | 괄호 → 하이픈 |
| `messages/scenes/scenes_tr.properties` | 괄호 → 하이픈 |
| `messages/scenes/scenes_uk.properties` | 괄호 → 하이픈 |
| `messages/scenes/scenes_vi.properties` | 괄호 → 하이픈, Cao nhất → Kỷ lục tốt nhất |
| `messages/scenes/scenes_zh.properties` | 괄호 → 하이픈, 最高 → 最高记录 |
| `messages/scenes/scenes_zh-hant.properties` | 괄호 → 하이픈, 最高 → 最高紀錄 |

---
