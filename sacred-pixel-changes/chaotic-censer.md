# 혼돈의 향로 밸런스 조정

**날짜**: 2026-04-13

## 개요

혼돈의 향로 장신구의 자해 위험을 줄이기 위해 조정했습니다. 가스 생성 최소 거리를 2타일에서 3타일로 늘리고, 모든 가스량을 50% 줄였습니다.

---

## 변경 사항

### 1. 가스 생성 거리

| 항목 | 변경 전 | 변경 후 |
|-----|--------|--------|
| 최소 거리 | 2타일 | **3타일** |
| 최대 거리 | 6타일 | 6타일 (변경 없음) |

#### 설계 의도
- 가스가 영웅에게 더 가까이 생성되는 것을 방지
- 영웅이 가스에 즉각적으로 노출될 위험 감소

---

### 2. 가스량 변경

#### 일반 (COMMON) 가스

| 가스 유형 | 변경 전 | 변경 후 |
|----------|--------|--------|
| ToxicGas (독성 가스) | 300 | **150** |
| ConfusionGas (혼란 가스) | 300 | **150** |
| Regrowth (재성장) | 200 | **100** |

#### 희귀 (UNCOMMON) 가스

| 가스 유형 | 변경 전 | 변경 후 |
|----------|--------|--------|
| StormCloud (폭풍 구름) | 300 | **150** |
| SmokeScreen (연막) | 300 | **150** |
| StenchGas (악취 가스) | 200 | **100** |

#### 레어 (RARE) 가스

| 가스 유형 | 변경 전 | 변경 후 |
|----------|--------|--------|
| Inferno (지옥불) | 300 | **150** |
| Blizzard (눈보라) | 300 | **150** |
| CorrosiveGas (부식 가스) | 200 | **100** |

#### 설계 의도
- 가스 지속 시간 및 범위 축소
- 영웅이 가스에 노출되더라도 피해량 감소
- 적에게도 효과가 약해지지만, 안전성 향상이 더 중요

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|---------|
| `core/src/main/java/com/sacredpixel/sacredpixeldungeon/items/trinkets/ChaoticCenser.java` | 최소 생성 거리 증가, 가스량 50% 감소 |

---

## 코드 변경 상세

### ChaoticCenser.java

#### 1. 가스 분출 최소 거리 변경 (라인 186-191)

```java
// 변경 전
//spawn gas in a random visible cell 2-6 tiles away
if (PathFinder.distance[i] >= 2 && PathFinder.distance[i] <= 6) {

// 변경 후
//spawn gas in a random visible cell 3-6 tiles away
if (PathFinder.distance[i] >= 3 && PathFinder.distance[i] <= 6) {
```

#### 2. COMMON 가스량 변경 (라인 314-319)

```java
private static final HashMap<Class<? extends Blob>, Float> COMMON_GASSES = new HashMap<>();
static {
    COMMON_GASSES.put(ToxicGas.class, 150f);
    COMMON_GASSES.put(ConfusionGas.class, 150f);
    COMMON_GASSES.put(Regrowth.class, 100f);
}
```

#### 3. UNCOMMON 가스량 변경 (라인 321-326)

```java
private static final HashMap<Class<? extends Blob>, Float> UNCOMMON_GASSES = new HashMap<>();
static {
    UNCOMMON_GASSES.put(StormCloud.class, 150f);
    UNCOMMON_GASSES.put(SmokeScreen.class, 150f);
    UNCOMMON_GASSES.put(StenchGas.class, 100f);
}
```

#### 4. RARE 가스량 변경 (라인 328-333)

```java
private static final HashMap<Class<? extends Blob>, Float> RARE_GASSES = new HashMap<>();
static {
    RARE_GASSES.put(Inferno.class, 150f);
    RARE_GASSES.put(Blizzard.class, 150f);
    RARE_GASSES.put(CorrosiveGas.class, 100f);
}
```

---

## 메시지 문자열

메시지 파일에는 가스량이나 거리에 대한 구체적인 수치가 포함되어 있지 않아 **수정이 필요하지 않습니다**.

---

## 영향 범위

### 영향받는 시스템
- 혼돈의 향로 가스 분출 위치
- 분출되는 가스의 양 및 지속 시간

### 영향받지 않는 시스템
- 가스 분출 주기 (trinket 레벨에 따른 턴 수 - 변경 없음)
- 가스 종류별 출현 확률 (COMMON/UNCOMMON/RARE 비율 - 변경 없음)
- 가스 자체의 효과 (독성, 혼란 등의 기본 메커니즘 - 변경 없음)
- 다른 장신구 및 게임 시스템 (영향 없음)

---

## 호환성 참고

### 세이브 파일
- 이미 생성된 가스 구름에는 영향 없음
- 새로 분출되는 가스부터 변경 사항 적용
- 게임 플레이에 실질적인 문제 없음

---
