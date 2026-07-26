# 142. 랭킹 정렬 2차 기준 추가 (duration)

**날짜**: 2026-07-26

## 배경

랭킹에서 동일 점수의 기록이 여러 개 있을 때, 기존에는 `gameID.hashCode()`로 정렬하여 사실상 무작위 순서였다. 같은 점수일 경우 더 적은 턴으로 달성한 기록이 상위에 오도록 2차 정렬 기준을 추가한다.

---

## 변경 사항

### 1. scoreComparator 2차 기준 추가

`Rankings.scoreComparator`에 duration 기준 추가:

```diff
  int result = (int)Math.signum( rhs.score - lhs.score );
  if (result == 0) {
-     return (int)Math.signum( rhs.gameID.hashCode() - lhs.gameID.hashCode());
-  } else {
-     return result;
+     // 2nd criteria: shorter duration (fewer turns) ranks higher
+     result = (int)Math.signum( lhs.duration - rhs.duration );
   }
+  if (result == 0) {
+     // 3rd criteria: fallback to gameID hash for stable sorting
+     return (int)Math.signum( rhs.gameID.hashCode() - lhs.gameID.hashCode());
+  }
+  return result;
```

### 2. 정렬 우선순위

| 순서 | 기준 | 정렬 방향 | 상태 |
|------|------|----------|------|
| 1차 | Custom Seed 여부 | 일반 런 > 커스텀 시드 | 기존 |
| 2차 | 점수 (score) | 높을수록 상위 | 기존 |
| 3차 | 턴 수 (duration) | 짧을수록 상위 | **추가** |
| 4차 | gameID 해시 | 안정 정렬용 | 기존 (폴백) |

### 3. CloudSave 정렬 기준 통일

CloudSave에서도 동일한 `scoreComparator` 사용하도록 변경:

```diff
- // Sort by score descending and keep top TABLE_SIZE records
- Collections.sort(Rankings.INSTANCE.records, (a, b) -> b.score - a.score);
+ // Sort by scoreComparator (customSeed > score > duration > gameID)
+ Collections.sort(Rankings.INSTANCE.records, Rankings.scoreComparator);
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `core/src/main/java/.../Rankings.java` | scoreComparator에 duration 기준 추가 |
| `core/src/main/java/.../CloudSave.java` | scoreComparator 사용으로 통일 |

---

## 영향

- 동일 점수 기록 간 정렬이 의미 있는 기준(턴 수)으로 변경
- 더 효율적인 플레이(적은 턴)가 상위 랭크
- 기존 duration이 0인 오래된 기록은 gameID 폴백으로 안정 정렬
- 데이터 호환성 문제 없음 (정렬 순서만 변경, 저장 구조 동일)

---

## 적용 범위

`scoreComparator`가 사용되는 모든 곳에 일괄 적용:

| 위치 | 용도 |
|------|------|
| `Rankings.java:151` | 새 기록 추가 시 정렬 |
| `WelcomeScene.java:275` | 환영 화면 정렬 |
| `WebDataMerger.java:170` | 데이터 병합 시 정렬 |
| `CloudSave.java:597` | 클라우드 저장 병합 시 정렬 |

---

## 관련 코드

- `Rankings.java:746-763` - scoreComparator 정의
- `Rankings.Record.duration` - 게임 턴 수 필드
