# 081. 금고 퀘스트 (Vault Quest) 비활성화

**날짜**: 2026-06-02

## 개요

AmbitiousImpRoom의 EXIT 타일을 밟아도 VaultLevel로 진입할 수 없도록 비활성화했습니다. 개발자 메시지 본문은 모든 언어에서 빈 문자열로 변경되었습니다.

---

## 변경 사항

### 변경 사유

금고 퀘스트(Vault Quest)는 아직 개발 중인 기능으로, 현재 릴리스에서는 접근을 차단합니다.

---

### 수정 내용

### 1. CityLevel.java - BRANCH_EXIT 전환 비활성화

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/levels/CityLevel.java`

```java
// 수정 전
@Override
public boolean activateTransition(Hero hero, LevelTransition transition) {
    if (transition.type == LevelTransition.Type.BRANCH_EXIT) {
        // 대화창 표시 및 VaultLevel 진입 로직
        Game.runOnRenderThread(new Callback() {
            @Override
            public void call() {
                GameScene.show(new WndOptions(...) {
                    // 진입 확인 대화창
                });
            }
        });
        return false;
    } else {
        return super.activateTransition(hero, transition);
    }
}

// 수정 후
@Override
public boolean activateTransition(Hero hero, LevelTransition transition) {
    if (transition.type == LevelTransition.Type.BRANCH_EXIT) {
        // Vault quest is disabled for now
        return false;
    } else {
        return super.activateTransition(hero, transition);
    }
}
```

**효과**: EXIT 타일을 밟아도 아무 일도 일어나지 않음

---

### 2. 개발자 메시지 본문 제거 (23개 언어)

**키**: `levels.citylevel.upcoming_quest_intro_body`

**수정 내용**: 모든 언어 파일에서 메시지 본문을 빈 문자열로 변경

| 언어 | 파일 |
|------|------|
| English | `levels.properties` |
| 한국어 | `levels_ko.properties` |
| Deutsch | `levels_de.properties` |
| Español | `levels_es.properties` |
| Français | `levels_fr.properties` |
| Italiano | `levels_it.properties` |
| Português | `levels_pt.properties` |
| Русский | `levels_ru.properties` |
| 日本語 | `levels_ja.properties` |
| 简体中文 | `levels_zh.properties` |
| 繁體中文 | `levels_zh-hant.properties` |
| Polski | `levels_pl.properties` |
| Türkçe | `levels_tr.properties` |
| Nederlands | `levels_nl.properties` |
| Čeština | `levels_cs.properties` |
| Magyar | `levels_hu.properties` |
| Svenska | `levels_sv.properties` |
| Ελληνικά | `levels_el.properties` |
| Українська | `levels_uk.properties` |
| Беларуская | `levels_be.properties` |
| Tiếng Việt | `levels_vi.properties` |
| Bahasa Indonesia | `levels_in.properties` |
| Esperanto | `levels_eo.properties` |

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `core/.../levels/CityLevel.java` | BRANCH_EXIT 전환 시 즉시 false 반환 |
| `core/.../messages/levels/levels.properties` | 메시지 본문 제거 |
| `core/.../messages/levels/levels_ko.properties` | 메시지 본문 제거 |
| `core/.../messages/levels/levels_de.properties` | 메시지 본문 제거 |
| `core/.../messages/levels/levels_es.properties` | 메시지 본문 제거 |
| `core/.../messages/levels/levels_fr.properties` | 메시지 본문 제거 |
| `core/.../messages/levels/levels_it.properties` | 메시지 본문 제거 |
| `core/.../messages/levels/levels_pt.properties` | 메시지 본문 제거 |
| `core/.../messages/levels/levels_ru.properties` | 메시지 본문 제거 |
| `core/.../messages/levels/levels_ja.properties` | 메시지 본문 제거 |
| `core/.../messages/levels/levels_zh.properties` | 메시지 본문 제거 |
| `core/.../messages/levels/levels_zh-hant.properties` | 메시지 본문 제거 |
| `core/.../messages/levels/levels_pl.properties` | 메시지 본문 제거 |
| `core/.../messages/levels/levels_tr.properties` | 메시지 본문 제거 |
| `core/.../messages/levels/levels_nl.properties` | 메시지 본문 제거 |
| `core/.../messages/levels/levels_cs.properties` | 메시지 본문 제거 |
| `core/.../messages/levels/levels_hu.properties` | 메시지 본문 제거 |
| `core/.../messages/levels/levels_sv.properties` | 메시지 본문 제거 |
| `core/.../messages/levels/levels_el.properties` | 메시지 본문 제거 |
| `core/.../messages/levels/levels_uk.properties` | 메시지 본문 제거 |
| `core/.../messages/levels/levels_be.properties` | 메시지 본문 제거 |
| `core/.../messages/levels/levels_vi.properties` | 메시지 본문 제거 |
| `core/.../messages/levels/levels_in.properties` | 메시지 본문 제거 |
| `core/.../messages/levels/levels_eo.properties` | 메시지 본문 제거 |

---

## 참고: 금고 퀘스트 구조

### AmbitiousImpRoom
- 임프가 위치한 퀘스트 방 (9x9 고정 크기)
- 중앙에 EXIT 타일 → VaultLevel로의 BRANCH_EXIT 전환
- QuestEntrance (5x5) + EntranceBarrier (3x3) 시각 효과

### VaultLevel
- 분기 레벨 (branch: 1, depth: 16-19)
- 드워프 금고 습격 퀘스트
- 아이템 없이 진입, EscapeCrystal로 탈출

### 비활성화 전 흐름
```
EXIT 타일 밟음 → 개발자 메시지 대화창 → "금고로 내려간다" 선택 → VaultLevel 진입
```

### 비활성화 후 흐름
```
EXIT 타일 밟음 → 아무 일도 일어나지 않음
```

---
