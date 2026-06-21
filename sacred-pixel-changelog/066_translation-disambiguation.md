# 066. 번역 의미 중복 수정

**날짜**: 2026-05-23

## 개요

여러 언어에서 서로 다른 영어 단어가 동일한 번역어로 되어 있어 사용자 혼란을 야기하는 문제를 수정했습니다. (버전: 3.3.8-HTML5-indev)

---

## 변경 사항

### 문제 설명

게임 내 아이템 액션 버튼 세 가지:
- **DROP** (ac_drop): 아이템을 바닥에 버림 → 인벤토리에서 제거
- **UNEQUIP** (ac_unequip): 장비 해제 → 인벤토리에는 남음
- **DETACH** (ac_detach): 갑옷에서 인장 분리 → 전사 전용

이 세 가지는 완전히 다른 동작인데, 일부 언어에서 같은 단어로 번역되어 있었습니다.

---

## 수정된 언어 및 내용

### 1. 독일어 (DE) - 필수 수정

| 키 | 수정 전 | 수정 후 | 문제 |
|----|---------|---------|------|
| ac_drop | ABLEGEN | **FALLEN LASSEN** | UNEQUIP과 동일 |
| ac_detach | LÖSEN | **SIEGEL LÖSEN** | 대상 불명확 |

- `ABLEGEN` = 벗다/내려놓다 → UNEQUIP에 적합
- `FALLEN LASSEN` = 떨어뜨리다 → DROP에 적합
- `SIEGEL LÖSEN` = 인장을 풀다 → 대상 명시

### 2. 스페인어 (ES) - 필수 수정

| 키 | 수정 전 | 수정 후 | 문제 |
|----|---------|---------|------|
| ac_detach | SOLTAR | **DESPEGAR** | DROP과 동일 |

- `SOLTAR` = 놓다/버리다 → DROP에 사용 중
- `DESPEGAR` = 떼어내다/분리하다 → DETACH에 적합

### 3. 베트남어 (VI) - 필수 수정

| 키 | 수정 전 | 수정 후 | 문제 |
|----|---------|---------|------|
| ac_detach | THÁO | **GỠ** | UNEQUIP과 동일 |

- `THÁO` = 풀다/벗다 → UNEQUIP에 사용 중
- `GỠ` = 떼어내다 → DETACH에 적합

### 4. 그리스어 (EL) - 권장 수정

| 키 | 수정 전 | 수정 후 | 문제 |
|----|---------|---------|------|
| ac_detach | ΛΥΣΗ | **ΞΕΚΟΛΛΑ** | 의미 불명확 |

- `ΛΥΣΗ` = 해결/풀다 → 너무 추상적
- `ΞΕΚΟΛΛΑ` = 떼어내다/분리하다 → 더 구체적

### 5. 인도네시아어 (IN) - 권장 수정

| 키 | 수정 전 | 수정 후 | 문제 |
|----|---------|---------|------|
| ac_detach | LEPAS | **COPOT** | UNEQUIP과 유사 |

- `LEPAS` / `LEPASKAN` = 같은 어근으로 혼동 가능
- `COPOT` = 떼어내다 (부착된 것을 분리) → 더 명확

### 6. 터키어 (TR) - 선택적 개선

| 키 | 수정 전 | 수정 후 | 문제 |
|----|---------|---------|------|
| ac_detach | SÖK | **AYIR** | 의미 개선 |

- `SÖK` = 뜯다 → 다소 공격적
- `AYIR` = 분리하다 → 더 중립적

### 7. 헝가리어 (HU) - 치명적 오류 수정

| 키 | 수정 전 | 수정 후 | 문제 |
|----|---------|---------|------|
| ac_detach | LETŰZÖM | **PECSÉT LE** | 동사가 반대 의미 |

- `LETŰZÖM` = "꽂다/고정하다" → DETACH의 정반대!
- `PECSÉT LE` = 인장을 떼다 → 올바른 의미

---

## 2차 수정: 모든 언어에 "인장(seal)" 개념 추가

모든 언어의 ac_detach에 "인장"이라는 대상을 명시적으로 추가하여 버튼의 목적을 명확히 함.

### 수정된 언어 (18개)

| 언어 | 수정 전 | 수정 후 | 인장 단어 |
|------|---------|---------|----------|
| RU | ОТКРЕПИТЬ | **СНЯТЬ ПЕЧАТЬ** | печать |
| UK | ВІДЧЕПИТИ | **ЗНЯТИ ПЕЧАТКУ** | печатка |
| BE | АДМАЦАВАЦЬ | **ЗНЯЦЬ ПЯЧАТКУ** | пячатка |
| CS | ODEPNOUT | **SEJMOUT PEČEŤ** | pečeť |
| EL | ΞΕΚΟΛΛΑ | **ΒΓΑΛΕ ΣΦΡΑΓΙΔΑ** | σφραγίδα |
| EO | DISIGI | **FORIGI SIGELON** | sigelo |
| TR | AYIR | **MÜHÜR SÖK** | mühür |
| VI | GỠ | **GỠ CON DẤU** | con dấu |
| IN | COPOT | **LEPAS SEGEL** | segel |
| ES | DESPEGAR | **QUITAR SELLO** | sello |
| FR | DÉTACHER | **ÔTER SCEAU** | sceau |
| PT | SOLTAR | **TIRAR SELO** | selo |
| IT | DISTACCA | **TOGLI SIGILLO** | sigillo |
| NL | LOSMAAK | **ZEGEL LOS** | zegel |
| SV | TA LOSS | **TA AV SIGILL** | sigill |
| PL | ODŁĄCZ | **ZDEJMIJ PIECZĘĆ** | pieczęć |
| ZH | 拆卸 | **卸印章** | 印章 |
| ZH-HANT | 拆卸 | **卸印章** | 印章 |

### 이미 인장 개념이 있던 언어 (4개)

| 언어 | 번역 | 인장 단어 |
|------|------|----------|
| DE | SIEGEL LÖSEN | Siegel |
| JA | 印章を外す | 印章 |
| KO | 인장을 떼어낸다 | 인장 |
| HU | PECSÉT LE | pecsét |

---

## 수정된 파일

| 파일 | 수정 내용 |
|------|----------|
| `items_de.properties` | ac_drop, ac_detach 변경 |
| `items_es.properties` | ac_detach 변경 (2회) |
| `items_vi.properties` | ac_detach 변경 (2회) |
| `items_el.properties` | ac_detach 변경 (2회) |
| `items_in.properties` | ac_detach 변경 (2회) |
| `items_tr.properties` | ac_detach 변경 (2회) |
| `items_hu.properties` | ac_detach 변경 |
| `items_ru.properties` | ac_detach 인장 개념 추가 |
| `items_uk.properties` | ac_detach 인장 개념 추가 |
| `items_be.properties` | ac_detach 인장 개념 추가 |
| `items_cs.properties` | ac_detach 인장 개념 추가 |
| `items_eo.properties` | ac_detach 인장 개념 추가 |
| `items_fr.properties` | ac_detach 인장 개념 추가 |
| `items_pt.properties` | ac_detach 인장 개념 추가 |
| `items_it.properties` | ac_detach 인장 개념 추가 |
| `items_nl.properties` | ac_detach 인장 개념 추가 |
| `items_sv.properties` | ac_detach 인장 개념 추가 |
| `items_pl.properties` | ac_detach 인장 개념 추가 |
| `items_zh.properties` | ac_detach 인장 개념 추가 |
| `items_zh-hant.properties` | ac_detach 인장 개념 추가 |
| `translation_guide.md` | 의미 중복 수정 기록 및 인장 개념 테이블 추가 |

---

## 향후 번역 검수 지침

1. **같은 카테고리의 액션 버튼은 반드시 다른 단어 사용**
2. **특히 ac_drop / ac_unequip / ac_detach 는 절대 동일 단어 금지**
3. **DETACH는 "인장"을 분리하는 것이므로 가능하면 대상 명시**
   - 좋은 예: 독일어 `SIEGEL LÖSEN`, 일본어 `印章を外す`, 한국어 `인장을 떼어낸다`

---

## 관련 문서

- `Changelog/translation_guide.md` - 번역 가이드라인 업데이트됨

---
