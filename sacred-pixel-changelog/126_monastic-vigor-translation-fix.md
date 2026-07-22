# 126. Monastic Vigor 특성 설명 번역 수정

## 배경

Monk 서브클래스의 **Monastic Vigor** 특성 설명에서 "에너지 100%/80%/60%"라는 표현이 **현재 에너지**인지 **최대 에너지 대비 비율**인지 모호했습니다.

실제 코드(`MonkEnergy.java`)를 보면 `energy/energyCap()` 비율을 사용하므로, "최대 에너지의 X%"라는 표현이 정확합니다.

```java
// MonkEnergy.java:237-240
public boolean abilitiesEmpowered( Hero hero ){
    return energy/energyCap() >= 1.2f - 0.2f*hero.pointsInTalent(Talent.MONASTIC_VIGOR);
}
```

## 문제

모든 언어에서 "100% energy"로만 표기되어 있어, 플레이어가 "에너지가 꽉 차야 하는 건지" 또는 "최대치 대비 비율인지" 혼동할 수 있었습니다.

---

## 수정 내용

23개 언어의 `monastic_vigor.desc` 키에 "최대(max/maximum)" 개념을 명시적으로 추가했습니다.

### 수정된 언어 (22개)

| 언어 | 코드 | 수정 전 | 수정 후 |
|------|------|---------|---------|
| English | en | 100% energy | **100% of max energy** |
| 한국어 | ko | 에너지가 100% | **최대 에너지의 100%** |
| 简体中文 | zh | 100%内力 | **最大内力的100%** |
| 繁體中文 | zh-hant | 100%內力 | **最大內力的100%** |
| Deutsch | de | 100% Energie | **100% maximaler Energie** |
| Français | fr | 100% d'énergie | **100% de son énergie maximale** |
| Español | es | 100% de energía | **100% de energía máxima** |
| Português | pt | 100% de energia | **100% da energia máxima** |
| Italiano | it | 100% d'energia | **100% dell'energia massima** |
| Русский | ru | 100% энергии | **100% от максимальной энергии** |
| Українська | uk | 100% енергії | **100% від максимальної енергії** |
| Polski | pl | 100% energii | **100% maksymalnej energii** |
| Nederlands | nl | 100% energie | **100% van de maximale energie** |
| Türkçe | tr | %100 enerjisi | **maksimum enerjisinin %100'ü** |
| Tiếng Việt | vi | 100% năng lượng | **100% năng lượng tối đa** |
| Svenska | sv | 100% energi | **100% av maxenergi** |
| Magyar | hu | 100% energiával | **a maximális energia 100%-ával** |
| Čeština | cs | 100% chi | **100% maximální chi** |
| Ελληνικά | el | 100% ενέργεια | **100% της μέγιστης ενέργειας** |
| Bahasa Indonesia | in | 100% energi | **100% energi maksimum** |
| Esperanto | eo | 100% de energio | **100% de maksimuma energio** |
| Беларуская | be | *(전체 재작성)* | **100% ад максімальнай энергіі** |

### 이미 정확했던 언어 (1개)

| 언어 | 코드 | 기존 표현 |
|------|------|----------|
| 日本語 | ja | _最大値_ (최대값) |

일본어는 이미 `最大値`(최대값)라는 표현을 사용하고 있어 수정이 필요 없었습니다.

---

## 벨라루스어 (be) 특별 수정

벨라루스어 번역은 **완전히 다른 특성을 설명**하고 있었습니다:

```properties
# 수정 전 (잘못된 번역 - HP 회복 설명)
actors.hero.talent.monastic_vigor.desc=_+1:_ Калі Манашка выкарыстоўвае здольнасць, яна аднаўляе _+1 HP._\n\n_+2:_ Калі Манашка выкарыстоўвае здольнасць, яна аднаўляе _+2 HP._\n\n_+3:_ Калі Манашка выкарыстоўвае здольнасць, яна аднаўляе _+3 HP._

# 수정 후 (올바른 번역 - 에너지 임계값 + 능력 강화 효과)
actors.hero.talent.monastic_vigor.desc=_+1:_ Калі ў Манашкі _100% ад максімальнай энергіі_, яе здольнасці ўзмацняюцца.\n\n_+2:_ Калі ў Манашкі _80% ці больш ад максімальнай энергіі_, яе здольнасці ўзмацняюцца.\n\n_+3:_ Калі ў Манашкі _60% ці больш ад максімальнай энергіі_, яе здольнасці ўзмацняюцца.\n\nПры ўзмацненні:\n- Шквал удараў ужывае зачараванне зброі.\n- Канцэнтрацыя адбываецца імгненна.\n- Рывок атрымлівае +4 дыстанцыі.\n- Удар дракона наносіць +50% шкоды, адкідвае і аглушае ўсіх суседніх ворагаў.\n- Медытацыя павольна аднаўляе 20% ад страчанага здароўя і дае 80% супраціўлення шкодзе.
```

---

## 수정된 파일

| 파일 | 경로 |
|------|------|
| actors.properties | `core/src/main/assets/messages/actors/` |
| actors_ko.properties | 〃 |
| actors_zh.properties | 〃 |
| actors_zh-hant.properties | 〃 |
| actors_de.properties | 〃 |
| actors_fr.properties | 〃 |
| actors_es.properties | 〃 |
| actors_pt.properties | 〃 |
| actors_it.properties | 〃 |
| actors_ru.properties | 〃 |
| actors_uk.properties | 〃 |
| actors_pl.properties | 〃 |
| actors_nl.properties | 〃 |
| actors_tr.properties | 〃 |
| actors_vi.properties | 〃 |
| actors_sv.properties | 〃 |
| actors_hu.properties | 〃 |
| actors_cs.properties | 〃 |
| actors_el.properties | 〃 |
| actors_in.properties | 〃 |
| actors_eo.properties | 〃 |
| actors_be.properties | 〃 |

---

## 영향

- 모든 언어에서 Monastic Vigor 특성의 발동 조건이 명확해짐
- 플레이어가 "최대 에너지 대비 비율"임을 즉시 이해 가능
- 벨라루스어 번역 오류 수정으로 해당 언어 사용자에게 올바른 정보 제공

---

## 관련 코드

- `MonkEnergy.java:237-240` - `abilitiesEmpowered()` 메서드
- `Talent.MONASTIC_VIGOR` - 특성 정의
