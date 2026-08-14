# 155. 자동 타겟팅 로직 분석

**날짜**: 2026-08-13

## 배경

지팡이 등 원거리 아이템 사용 시 자동으로 크로스헤어가 표시되는 타겟 선택 로직을 분석했다.
가까운 적보다 먼 적이 자동 선택되는 현상이 발견되어 원인을 조사했다.

---

## 분석 결과

### 현상

1. 뱀이 먼저 시야에 들어옴 (2칸 거리)
2. 영웅이 이동하여 쥐가 시야에 들어옴 (1칸 거리)
3. 지팡이 사용 시 크로스헤어가 더 먼 뱀에 표시됨

### 원인: `lastTarget` 유지 로직

`Hero.checkVisibleMobs()`에서 `lastTarget` 교체 조건:

```java
// Hero.java:1790-1800
boolean shouldTarget = false;
if (target != null) {
    if (lastTarget == null) {
        shouldTarget = true;
    } else {
        if (!lastTarget.isAlive() || !lastTarget.isActive() ||
                    lastTarget.alignment == Alignment.ALLY ||
                    !fieldOfView[lastTarget.pos]) {
            shouldTarget = true;
        }
        // ★ "더 가까운 적이 있는지" 체크 없음
    }
}
```

교체 조건:
- `lastTarget`이 null
- `lastTarget`이 죽음
- `lastTarget`이 비활성화됨
- `lastTarget`이 아군으로 변경됨
- `lastTarget`이 시야 밖으로 벗어남

**"더 가까운 적이 있는지"는 체크하지 않음** → 시야에 먼저 들어온 적이 유지됨

---

## 원본 코드 비교

### Shattered Pixel Dungeon (원본)

```java
Char lastTarget = QuickSlotButton.lastTarget;
if (target != null && (lastTarget == null ||
                    !lastTarget.isAlive() || !lastTarget.isActive() ||
                    lastTarget.alignment == Alignment.ALLY ||
                    !fieldOfView[lastTarget.pos])){
    QuickSlotButton.target(target);
}
```

### Sacred Pixel Dungeon (현재)

```java
Char lastTarget = QuickSlotButton.lastTarget;
try {
    boolean shouldTarget = false;
    if (target != null) {
        if (lastTarget == null) {
            shouldTarget = true;
        } else {
            if (!lastTarget.isAlive() || !lastTarget.isActive() ||
                        lastTarget.alignment == Alignment.ALLY ||
                        !fieldOfView[lastTarget.pos]) {
                shouldTarget = true;
            }
        }
    }
    if (shouldTarget) {
        // TeaVM 안전 코드...
        QuickSlotButton.target(target);
    }
} catch (Exception e) { ... }
```

### 결론

**로직 동일**. Sacred에서 TeaVM 호환성을 위한 try-catch와 shouldTarget 플래그만 추가됨.
이 동작은 **원본 Shattered Pixel Dungeon의 의도된 설계**임.

---

## 관련 코드

| 파일 | 위치 | 설명 |
|------|------|------|
| `Hero.java` | 1751-1819 | `checkVisibleMobs()` - 타겟 선택 로직 |
| `QuickSlotButton.java` | 354-380 | `autoAim()` - 공격 가능 여부 판정 |
| `QuickSlotButton.java` | 402-424 | `target()` - lastTarget 설정 |
| `CellSelector.java` | 353-369 | `initKeyboardCursor()` - 크로스헤어 초기 위치 |

---

## 설계 의도 추정

- **전투 중 타겟 유지**: 이전 타겟이 유효하면 유지하여 전투 중 타겟이 갑자기 바뀌는 것을 방지
- **트레이드오프**: 전투 연속성 vs 최적 타겟 자동 선택

---

## 잠재적 개선안 (미적용)

필요 시 다음 조건 추가 가능:

```diff
  if (!lastTarget.isAlive() || !lastTarget.isActive() ||
              lastTarget.alignment == Alignment.ALLY ||
-             !fieldOfView[lastTarget.pos]) {
+             !fieldOfView[lastTarget.pos] ||
+             QuickSlotButton.autoAim(lastTarget) == -1) {  // 공격 불가능 시
      shouldTarget = true;
  }
```

또는 거리 비교 추가:

```diff
+             distance(lastTarget) > distance(target)) {  // 더 가까운 적 존재 시
```

**현재는 원본과 동일하게 유지.**

---

## 영향

- 없음 (분석 기록만, 코드 변경 없음)

---

## 관련 문서

- 원본 저장소: `00-Evan/shattered-pixel-dungeon`
