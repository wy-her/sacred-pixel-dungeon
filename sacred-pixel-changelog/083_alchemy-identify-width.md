# 083. 연금술 감정 창 너비 수정

**날짜**: 2026-06-03

## 개요

한국어 버전에서 물약 감정 창 텍스트 줄바꿈 문제 수정 및 테스트 레벨 아이템 미감정 상태 변경.

---

## 변경 사항

### 연금술 감정 창 폭 수정

#### 문제
- 한국어 버전에서 "암갈색 물약", "하늘색 물약" 등 일부 물약의 감정 창에서 텍스트가 한 줄에 충분히 들어갈 수 있음에도 줄바꿈이 발생
- 원인: 창 크기 측정값과 실제 렌더링 시 필요한 폭의 미세한 차이
  - `(int)` 캐스팅으로 인한 소수점 절삭
  - 한국어 공백 처리 시 음수 scaledSpaceW 적용
  - 측정 방식(`reqWidth()`)과 레이아웃 방식(`layout()`)의 차이

#### 수정 내용

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/scenes/AlchemyScene.java`

```java
// 변경 전 (line 1087)
contentWidth = (int)Math.max(oldWidth, newWidth) + 4;

// 변경 후
contentWidth = (int)Math.ceil(Math.max(oldWidth, newWidth)) + 8;
```

- `(int)` → `(int)Math.ceil()`: 소수점 올림 처리
- `+4` → `+8`: 여유분 증가 (측정 오차 커버)

### 관련 조사 (코드 변경 없음)

#### 연금술 감정 창 구조 분석
- `WndAlchemyIdentify` 클래스 (AlchemyScene.java 내부)
- 4단계 애니메이션: 옛 이름 표시(0.5s) → 전환(0.5s) → 새 이름 유지(1.5s) → 페이드아웃(0.5s)
- 총 표시 시간: 약 3초

#### 임프 퀘스트 & 드워프 금고 조사
- 임프 퀘스트: 17-19층에서 수도승/골렘 처치 후 토큰 수집 → +2강 반지 보상
- 드워프 금고: 25개 방으로 구성된 branch 레벨, 토큰 수집 후 반지/골드 교환

#### 소환 함정 조사
- 1-3마리 몹 소환 (층별 몹 풀에서 랜덤)
- 2초 딜레이 후 텔레포트 이펙트와 함께 등장

---

## 수정된 파일

| File | Changes |
|------|---------|
| `scenes/AlchemyScene.java` | 감정 창 contentWidth 계산 개선 |

---
