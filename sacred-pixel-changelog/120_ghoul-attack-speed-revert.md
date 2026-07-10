# 120. 구울 공격 속도 원복

**날짜**: 2026-07-10

## 개요

드워프 구울(Ghoul)의 공격 속도를 원본 Shattered Pixel Dungeon과 동일하게 조정. 2회 공격(0.5턴)에서 1회 공격(1.0턴)으로 변경.

---

## 기존 상태

Sacred Pixel Dungeon의 Ghoul에 `attackDelay()` 메서드가 추가되어 있었음:

```java
// 기존 코드 (Sacred)
@Override
public float attackDelay() {
    //ghouls attack rapidly, getting 2 attacks per hero turn
    return super.attackDelay()*0.5f;
}
```

원본 Shattered Pixel Dungeon에는 이 메서드가 **없음**.

---

## 밸런스 영향

| 항목 | 원본 (Shattered) | 기존 (Sacred) |
|------|-----------------|---------------|
| attackDelay | 1.0 (기본값) | 0.5 |
| 턴당 공격 횟수 | 1회 | 2회 |
| DPS | 16-22 | 32-44 (2배) |
| 난이도 | 보통 | 높음 |

Ghoul은 드워프 도시에서 **쌍으로 등장**하므로, 2회 공격 시 실제로는 4회 공격을 받는 셈.

---

## 변경 사항

### Ghoul.java - attackDelay() 메서드 제거

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/actors/mobs/Ghoul.java`

**삭제된 코드**:
```java
@Override
public float attackDelay() {
    //ghouls attack rapidly, getting 2 attacks per hero turn
    return super.attackDelay()*0.5f;
}
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `actors/mobs/Ghoul.java` | `attackDelay()` 메서드 삭제 |
| `levels/TestLevel.java` | 주석 "2x attack" 제거 |

---

## 수정 효과

| 항목 | Before | After |
|------|--------|-------|
| 구울 공격 속도 | 0.5 (2회/턴) | 1.0 (1회/턴) |
| 원본과 일치 | ❌ | ✅ |
| 밸런스 | 과도하게 강함 | 정상 |

---

## 참고: 정상적인 0.5턴 공격 적

다음 적들은 **원본에서도** 0.5턴 공격을 사용:
- Monk (수도승)
- RipperDemon (리퍼 데몬)
- Thief (도둑)

Ghoul은 원본에서 0.5턴 공격을 사용하지 **않음**.

---
