# 068. Button Active 상태, Alchemy Z-order, Berserk 번역

**날짜**: 2026-05-24

## 개요

WndRanking의 Challenge 탭 체크박스 클릭 버그 수정, AlchemyScene 감정 메시지 Z-order 문제 해결, 22개 언어의 Berserk angered_desc 번역 형식 통일.

---

## 변경 사항

### 1. Button.java — hotArea.active가 component의 active 플래그 존중

### 문제
WndRanking의 Challenge 탭에서 체크박스를 클릭하면 원래는 아무 반응이 없어야 하는데 (readOnly 상태), 창이 새로 열리는 버그 발생.

### 원인 분석
- `CheckBox`는 `Button`을 상속
- `Button.update()`에서 `hotArea.active = visible;`로만 설정
- `Button.active = false`로 설정해도 `hotArea`는 여전히 클릭을 처리함
- WndRanking에서 Challenge 탭의 체크박스는 `active = false`로 설정되어 있지만, hotArea가 이를 무시하고 클릭 이벤트를 처리

### 해결
`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/Button.java` (line 169):

```java
// 변경 전
hotArea.active = visible;

// 변경 후
hotArea.active = visible && active;
```

### 영향
- `CheckBox.active = false`가 정상 작동
- `IconButton.active = false`가 정상 작동
- 모든 Button 기반 컴포넌트에서 `active` 플래그가 hotArea에도 적용됨

---

## 2. AlchemyScene.java — Energize 감정 메시지 Z-order 수정

### 문제
가로모드에서 연금화 시 감정되는 아이템 안내 메시지(BG, oldName, newName)가 WndBag 아이템 선택창 뒤에 표시되어 보이지 않음.

### 원인 분석
- 감정 메시지 요소들이 `add()`로 추가됨
- WndBag는 `addToFront()`로 추가되어 더 앞에 위치
- 감정 메시지가 WndBag보다 뒤에 렌더링됨

### 해결
`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/scenes/AlchemyScene.java` (lines 987-989):

```java
// 변경 전
add(BG);
add(oldName);
add(newName);

// 변경 후
addToFront(BG);
addToFront(oldName);
addToFront(newName);
```

### 영향
- 연금화 시 감정 메시지가 항상 최상단에 표시됨
- 가로/세로 모드 모두 정상 작동

---

## 3. Berserk angered_desc — 22개 언어 번역 형식 통일

### 변경 내용
모든 언어 파일에서 Berserk angered_desc 마지막 부분의 형식을 통일:

1. **피해 증가 형식 변경**: `_+%2$.0f%%_ damage` → `Damage bonus: _+%2$.0f%%_`
2. **줄바꿈 축소**: `\n\n` → `\n` (광포화 방어막 앞)
3. **방어막 설명 간소화**: `Berserk shield at current armor and HP: _%3$d_` → `Berserk Shield: _%3$d_`

### 수정된 파일 (22개)

| 언어 | 파일 | 변경 후 |
|------|------|---------|
| English | actors.properties | `Damage bonus: _+%2$.0f%%_\nBerserk Shield: _%3$d_` |
| Korean | actors_ko.properties | `피해 증가: _+%2$.0f%%_\n광포화 방어막: _%3$d_` |
| German | actors_de.properties | `Schadensbonus: _+%2$.0f%%_\nRage-Abschirmung: _%3$d_` |
| Japanese | actors_ja.properties | `ダメージボーナス: _+%2$.0f%%_\n凶暴化シールド: _%3$d_` |
| Chinese (Simplified) | actors_zh.properties | `伤害加成：_+%2$.0f%%_\n狂暴护盾：_%3$d_` |
| Chinese (Traditional) | actors_zh-hant.properties | `傷害加成：_+%2$.0f%%_\n狂暴護盾：_%3$d_` |
| French | actors_fr.properties | `Dégâts bonus : _+%2$.0f%%_\nProtection de rage : _%3$d_` |
| Spanish | actors_es.properties | `Daño extra: _+%2$.0f%%_\nBlindaje de furia: _%3$d_` |
| Russian | actors_ru.properties | `Бонус урона: _+%2$.0f%%_\nЩит Берсерка: _%3$d_` |
| Polish | actors_pl.properties | `Bonus obrażeń: _+%2$.0f%%_\nOsłona Berserka: _%3$d_` |
| Portuguese | actors_pt.properties | `Dano bônus: _+%2$.0f%%_\nBlindagem Berserk: _%3$d_` |
| Italian | actors_it.properties | `Danno bonus: _+%2$.0f%%_\nScudo della furia: _%3$d_` |
| Belarusian | actors_be.properties | `Бонус шкоды: _+%2$.0f%%_\nШчыт берсерка: _%3$d_` |
| Czech | actors_cs.properties | `Bonus poškození: _+%2$.0f%%_\nOchrana berserku: _%3$d_` |
| Greek | actors_el.properties | `Επιπλέον ζημιά: _+%2$.0f%%_\nΠροστασία εξαγρίωσης: _%3$d_` |
| Esperanto | actors_eo.properties | `Bonuso al vundpovo: _+%2$.0f%%_\nŜirmo de Berserko: _%3$d_` |
| Hungarian | actors_hu.properties | `Bónusz sebzés: _+%2$.0f%%_\nBerserk védőburok: _%3$d_` |
| Indonesian | actors_in.properties | `Bonus damage: _+%2$.0f%%_\nPerisai Mengamuk: _%3$d_` |
| Dutch | actors_nl.properties | `Bonusschade: _+%2$.0f%%_\nBerserkschild: _%3$d_` |
| Swedish | actors_sv.properties | `Bonusskada: _+%2$.0f%%_\nBärsärkarsköld: _%3$d_` |
| Turkish | actors_tr.properties | `Ek Hasar: _+%%%2$.0f_\nÖfke Kalkanı: _%3$d_` |
| Ukrainian | actors_uk.properties | `Бонус шкоди: _+%2$.0f%%_\nЗахист берсерка: _%3$d_` |
| Vietnamese | actors_vi.properties | `Sát thương cộng thêm: _+%2$.0f%%_\nKhiên Cuồng Nộ: _%3$d_` |

### 효과
- 모든 언어에서 일관된 형식 제공
- 불필요한 정보(현재 갑옷/HP) 제거로 간결해짐
- 가독성 향상

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `ui/Button.java` | `hotArea.active = visible && active` |
| `scenes/AlchemyScene.java` | `add()` → `addToFront()` |
| `messages/actors/actors*.properties` (22개) | angered_desc 형식 통일 |

---

## 테스트 완료

### Button Active 상태
- [x] WndRanking Challenge 탭에서 체크박스 클릭 → 반응 없음 (정상)
- [x] CheckBox.active = true 상태에서 클릭 → 정상 작동
- [x] IconButton.active = false 상태에서 클릭 → 반응 없음

### Alchemy Z-order
- [x] 가로모드에서 energize로 아이템 감정 → 메시지가 WndBag 앞에 표시
- [x] 세로모드에서도 정상 작동

### Berserk 번역
- [x] 영어 및 한국어에서 새 형식 확인
- [x] 22개 언어 파일 모두 수정 완료

---
