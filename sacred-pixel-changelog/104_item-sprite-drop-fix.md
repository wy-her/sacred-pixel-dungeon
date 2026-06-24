# 104. 아이템 줍기 실패 시 스프라이트 무한 상승 버그 수정

**날짜**: 2026-06-24

## 개요

인벤토리가 가득 찬 상태에서 바닥의 아이템(Heap)을 반복적으로 줍기 시도할 때, 아이템 스프라이트가 끝없이 위로 올라가는 버그 수정.

---

## 문제 분석

### 증상

- 인벤토리가 가득 찬 상태에서 아이템 줍기 시도 시 아이템이 위로 튕기는 애니메이션 발생
- 애니메이션이 끝나기 전에 다시 줍기를 시도하면 스프라이트가 현재 위치에서 다시 위로 튕김
- 반복 클릭 시 아이템 스프라이트가 끝없이 하늘로 올라감

### 원인

`ItemSprite.drop()` 메서드에서 `heap.size() == 1`인 경우에만 `place(heap.pos)`로 위치를 리셋하고, `heap.size() > 1`인 경우에는 위치 리셋 없이 현재 위치에서 애니메이션을 시작함.

**기존 코드의 주석에 의도적인 버그임이 명시되어 있었음:**
> "in order to preserve an amusing visual bug/feature that used to trigger for heaps with size > 1 where as long as the player continually taps, the heap sails up into the air."

---

## 변경 사항

### Bug Fix

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/sprites/ItemSprite.java`

**수정**: 모든 경우에 `place(heap.pos)`를 호출하여 애니메이션 시작 전 위치를 바닥으로 리셋

```java
// 기존 코드
public void drop() {
    if (heap.isEmpty()) {
        return;
    } else if (heap.size() == 1){
        // normally this would happen for any heap, however this is not applied to heaps greater than 1 in size
        // in order to preserve an amusing visual bug/feature that used to trigger for heaps with size > 1
        // where as long as the player continually taps, the heap sails up into the air.
        place(heap.pos);
    }

    dropInterval = DROP_INTERVAL;
    // ...
}

// 수정 코드
public void drop() {
    if (heap.isEmpty()) {
        return;
    }

    place(heap.pos);
    dropInterval = DROP_INTERVAL;
    // ...
}
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `sprites/ItemSprite.java:166-173` | `heap.size()` 조건 제거, 모든 경우에 `place(heap.pos)` 호출 |

---

## 영향 범위

- 아이템 줍기 실패 시 애니메이션: 항상 바닥에서 시작
- 일반 아이템 드랍 애니메이션: 변경 없음 (정상 동작)
- `drop(int from)` 메서드: 내부에서 `place(from)`을 재호출하므로 정상 동작

---
