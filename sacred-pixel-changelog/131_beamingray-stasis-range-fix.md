# 131. 재배치 광선(Beaming Ray) 정지 아군 유효거리 버그 수정

## 배경

재배치 광선(Beaming Ray) 스펠이 정지(Stasis) 상태의 아군을 꺼낼 때, 유효거리 체크가 아군의 동결된 위치(ally.pos)를 기준으로 하여 의도대로 작동하지 않았습니다.

정지 상태의 아군은 맵에서 제거된 상태이므로, 유효거리는 영웅 위치에서 측정되어야 합니다.

---

## 변경 사항

### 1. BeamingRay.java - 정지 아군 유효거리 계산 수정

**수정 전:**
```java
int range = 4*hero.pointsInTalent(Talent.BEAMING_RAY);
if (Char.hasProp(ally, Char.Property.IMMOVABLE)){
    range /= 2;
}
if (Dungeon.level.distance(ally.pos, telePos) > range){
    GLog.w(Messages.get(this, "out_of_range"));
    return;
}
```

**수정 후:**
```java
int range = 4*hero.pointsInTalent(Talent.BEAMING_RAY);
if (Char.hasProp(ally, Char.Property.IMMOVABLE)){
    range /= 2;
}
// Stasis ally is not on the map, so use hero position for distance check
int distanceFrom = (ally == Stasis.getStasisAlly()) ? hero.pos : ally.pos;
if (Dungeon.level.distance(distanceFrom, telePos) > range){
    GLog.w(Messages.get(this, "out_of_range"));
    return;
}
```

### 2. 설명 텍스트 업데이트 (23개 언어)

유효거리 측정 기준이 설명에 명시되지 않아 혼란을 줄 수 있으므로, **스펠 설명과 특성 설명** 모두에 다음 문장을 추가했습니다:

| 언어 | 추가된 문장 |
|------|------------|
| English | "The range is measured from the ally's current position, or from the Cleric if the ally is in stasis." |
| 한국어 | "유효거리는 아군의 현재 위치를 기준으로 측정되며, 정지 상태의 아군은 성직자 위치를 기준으로 합니다." |
| 日本語 | "射程は味方の現在位置から測定されるが、味方が停止状態の場合は聖職者の位置から測定される。" |
| 简体中文 | "范围从盟友当前位置开始测量，若盟友处于静止状态，则从牧师位置开始测量。" |
| 繁體中文 | "範圍從盟友當前位置開始測量，若盟友處於靜止狀態，則從牧師位置開始測量。" |
| Deutsch | "Die Reichweite wird von der aktuellen Position des Verbündeten gemessen, oder vom Kleriker, wenn sich der Verbündete in Stasis befindet." |
| Français | "La portée est mesurée depuis la position actuelle de l'allié, ou depuis le Clerc si l'allié est en stase." |
| Español | "El alcance se mide desde la posición actual del aliado, o desde el Clérigo si el aliado está en estasis." |
| Italiano | "La portata viene misurata dalla posizione attuale dell'alleato, oppure dal Chierico se l'alleato è in stasi." |
| Português | "O alcance é medido a partir da posição atual do aliado, ou do Clérigo se o aliado estiver em estase." |
| Русский | "Дальность измеряется от текущей позиции союзника или от Жреца, если союзник находится в стазисе." |
| Polski | "Zasięg mierzony jest od aktualnej pozycji sojusznika lub od Kapłana, jeśli sojusznik jest w stazisie." |
| Українська | "Дальність вимірюється від поточної позиції союзника або від Жерця, якщо союзник перебуває в стазисі." |
| Türkçe | "Menzil, müttefikin mevcut konumundan ölçülür veya müttefik durgunluk halindeyse Papazdan ölçülür." |
| Nederlands | "Het bereik wordt gemeten vanaf de huidige positie van de bondgenoot, of vanaf de Geestelijke als de bondgenoot in stasis is." |
| Svenska | "Räckvidden mäts från allierades nuvarande position, eller från Klerken om den allierade är i stasis." |
| Čeština | "Dosah se měří od aktuální pozice spojence, nebo od Kněze, pokud je spojenec v klidovém stavu." |
| Magyar | "A hatótáv a szövetséges jelenlegi pozíciójától méretik, vagy a Klerikus pozíciójától, ha a szövetséges stasisban van." |
| Ελληνικά | "Η εμβέλεια μετράται από την τρέχουσα θέση του συμμάχου ή από τον Κληρικό αν ο σύμμαχος βρίσκεται σε στάση." |
| Tiếng Việt | "Phạm vi được đo từ vị trí hiện tại của đồng minh, hoặc từ Giáo sĩ nếu đồng minh đang ở trạng thái đình trệ." |
| Bahasa Indonesia | "Jangkauan diukur dari posisi sekutu saat ini, atau dari Pendeta jika sekutu dalam keadaan stasis." |
| Беларуская | "Далёкасць вымяраецца ад бягучай пазіцыі саюзніка або ад Жраца, калі саюзнік знаходзіцца ў стазісе." |
| Esperanto | "La distanco estas mezurita de la nuna pozicio de la amiko, aŭ de la Pastro se la amiko estas en stazo." |

---

## 원인 분석

### 정지(Stasis) 아군의 특수성

| 상태 | 아군 위치 | 유효거리 기준 |
|------|----------|--------------|
| 빛의 아군 (맵에 있음) | 실제 맵 좌표 | 아군 위치 (정상) |
| 정지 아군 (맵에 없음) | 동결된 좌표 (무효) | ~~아군 위치~~ → **영웅 위치** |

정지 상태의 아군은 `GameScene.add()`가 호출되기 전까지 맵에 존재하지 않습니다. 따라서 `ally.pos`는 정지 스펠 사용 시점의 동결된 좌표이며, 현재 맵 상태와 무관합니다.

### 기존 코드 로직 분석

```java
Char ally = PowerOfMany.getPoweredAlly();

if (ally == null){
    //temporary, for distance checks
    ally = Dungeon.hero;  // ← 이 주석의 의도가 실제로 구현되지 않음
}
// ... (중간 로직)

if (ally == Dungeon.hero){
    ally = Stasis.getStasisAlly();  // ← 여기서 ally가 교체됨
}

// 거리 체크는 ally 교체 후에 실행됨
if (Dungeon.level.distance(ally.pos, telePos) > range){  // ← 버그
```

주석에는 "temporary, for distance checks"라고 되어 있지만, 실제 거리 체크는 `ally`가 Stasis 아군으로 교체된 후에 수행되어 의도대로 작동하지 않았습니다.

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `actors/hero/spells/BeamingRay.java:124` | `distanceFrom` 조건 분기 추가 |

### 스펠 설명 (`beamingray.desc`, line 629)

| 언어 파일 | 변경 내용 |
|----------|----------|
| `messages/actors/actors.properties:629` | 유효거리 기준 설명 추가 |
| `messages/actors/actors_ko.properties:629` | 〃 |
| `messages/actors/actors_ja.properties:629` | 〃 |
| `messages/actors/actors_zh.properties:629` | 〃 |
| `messages/actors/actors_zh-hant.properties:629` | 〃 |
| `messages/actors/actors_de.properties:629` | 〃 |
| `messages/actors/actors_fr.properties:629` | 〃 |
| `messages/actors/actors_es.properties:629` | 〃 |
| `messages/actors/actors_it.properties:629` | 〃 |
| `messages/actors/actors_pt.properties:629` | 〃 |
| `messages/actors/actors_ru.properties:629` | 〃 |
| `messages/actors/actors_pl.properties:629` | 〃 |
| `messages/actors/actors_uk.properties:629` | 〃 |
| `messages/actors/actors_tr.properties:629` | 〃 |
| `messages/actors/actors_nl.properties:629` | 〃 |
| `messages/actors/actors_sv.properties:629` | 〃 |
| `messages/actors/actors_cs.properties:629` | 〃 |
| `messages/actors/actors_hu.properties:629` | 〃 |
| `messages/actors/actors_el.properties:629` | 〃 |
| `messages/actors/actors_vi.properties:629` | 〃 |
| `messages/actors/actors_in.properties:629` | 〃 |
| `messages/actors/actors_be.properties:629` | 〃 |
| `messages/actors/actors_eo.properties:629` | 〃 |

### 특성 설명 (`beaming_ray.desc`, line 1290)

| 언어 파일 | 변경 내용 |
|----------|----------|
| `messages/actors/actors.properties:1290` | 유효거리 기준 설명 추가 |
| `messages/actors/actors_ko.properties:1290` | 〃 |
| `messages/actors/actors_ja.properties:1290` | 〃 |
| `messages/actors/actors_zh.properties:1290` | 〃 |
| `messages/actors/actors_zh-hant.properties:1290` | 〃 |
| `messages/actors/actors_de.properties:1290` | 〃 |
| `messages/actors/actors_fr.properties:1290` | 〃 |
| `messages/actors/actors_es.properties:1290` | 〃 |
| `messages/actors/actors_it.properties:1290` | 〃 |
| `messages/actors/actors_pt.properties:1290` | 〃 |
| `messages/actors/actors_ru.properties:1290` | 〃 |
| `messages/actors/actors_pl.properties:1290` | 〃 |
| `messages/actors/actors_uk.properties:1290` | 〃 |
| `messages/actors/actors_tr.properties:1290` | 〃 |
| `messages/actors/actors_nl.properties:1290` | 〃 |
| `messages/actors/actors_sv.properties:1292` | 〃 |
| `messages/actors/actors_cs.properties:1290` | 〃 |
| `messages/actors/actors_hu.properties:1290` | 〃 |
| `messages/actors/actors_el.properties:1290` | 〃 |
| `messages/actors/actors_vi.properties:1290` | 〃 |
| `messages/actors/actors_in.properties:1290` | 〃 |
| `messages/actors/actors_be.properties:1290` | 〃 |
| `messages/actors/actors_eo.properties:1290` | 〃 |

---

## 영향

- 정지 상태의 아군을 재배치 광선으로 꺼낼 때 유효거리가 영웅 위치 기준으로 정상 계산됨
- 설명 텍스트에 유효거리 기준이 명시되어 플레이어 혼란 방지
- 빛의 아군(맵에 있는 아군)은 기존과 동일하게 아군 위치 기준으로 계산

---

## 검증

에이전트 3명이 독립적으로 수정안을 분석 후 교차검증:

| 검증 항목 | 결과 |
|----------|------|
| Stasis 아군 케이스 수정 여부 | O |
| Light 아군 기존 동작 유지 | O |
| 참조 동등성 비교 안전성 | O (싱글톤 패턴) |
| 부작용 가능성 | 없음 |

---

## 비고

- 이 버그는 원본 Shattered PD 3.3.8에도 존재하는 것으로 확인됨
- GitHub 이슈 검색 결과, Evan(원작자)은 해당 버그를 인지하지 못한 것으로 보임

---

## 관련 코드

- `Stasis.getStasisAlly()` - 정지 상태 아군 반환
- `PowerOfMany.getPoweredAlly()` - 빛의 아군 반환
- `Dungeon.level.distance()` - 타일 거리 계산

