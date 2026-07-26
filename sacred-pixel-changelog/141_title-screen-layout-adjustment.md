# 141. 타이틀 화면 레이아웃 수직 중앙 정렬

**날짜**: 2026-07-26

## 배경

타이틀 화면에서 로고와 버튼들이 화면 비율에 따라 상하 여백이 불균형하게 표시되는 문제가 있었다. 기존에는 화면 높이의 고정 비율(38%)을 기준점으로 사용했으나, 이 방식은 다양한 화면 비율에서 일관된 레이아웃을 보장하지 못했다.

---

## 변경 사항

### 1. 수직 중앙 정렬 방식으로 변경

콘텐츠(로고 + 버튼들) 전체 높이를 계산한 후, 상하 여백을 균등하게 배분.

```diff
- // Keep original topRegion for button placement
- float topRegion = Math.max(scaledH - 6, h*0.38f);
-
- // Position logo closer to buttons (just above button area)
- title.x = insets.left + (w - scaledW) / 2f;
- title.y = insets.top + topRegion - scaledH - 5;

+ // Calculate content height for vertical centering
+ final int BTN_HEIGHT = 20;
+ final int buttonRows = 5;
+ final int minGap = 2;
+ final float logoButtonGap = 5;
+ final int gapCount = buttonRows - 1;  // 4 gaps between 5 rows
+
+ float minContentHeight = scaledH + logoButtonGap
+         + (buttonRows * BTN_HEIGHT)
+         + (gapCount * minGap);
+
+ // Distribute extra space: 1/25 to GAP, 24/25 to margins (equal top/bottom)
+ float extraSpace = Math.max(h - minContentHeight, 0);
+ float gapExtra = extraSpace / 25f;
+ float marginSpace = extraSpace - gapExtra;
+ float margin = marginSpace / 2f;
+ int GAP = minGap + (int)(gapExtra / gapCount);
+
+ // Position logo with equal top margin
+ title.x = insets.left + (w - scaledW) / 2f;
+ title.y = insets.top + margin;
```

### 2. 버튼 시작 위치를 로고 기준으로 변경

```diff
- btnPlay.setRect(btnAreaLeft, insets.top + topRegion+GAP, buttonAreaWidth, BTN_HEIGHT);

+ float buttonsStartY = title.y + scaledH + logoButtonGap;
+ btnPlay.setRect(btnAreaLeft, buttonsStartY, buttonAreaWidth, BTN_HEIGHT);
```

### 3. 여분 공간 분배 비율

| 항목 | 비율 | 설명 |
|------|------|------|
| GAP (버튼 간격) | 1/25 (4%) | 기존 비율 유지 |
| 상단 여백 | 12/25 (48%) | 균등 분배 |
| 하단 여백 | 12/25 (48%) | 균등 분배 |

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `scenes/TitleScene.java:160-180` | 콘텐츠 높이 계산 및 수직 중앙 정렬 |
| `scenes/TitleScene.java:317-319` | 버튼 시작 위치를 로고 기준으로 변경 |

---

## 영향

- 모든 화면 비율에서 상하 여백이 균등하게 유지
- 로고, Fireball, titleGlow는 기존처럼 연동되어 자동으로 따라감
- 버튼 간 간격(GAP)은 기존과 동일한 비율(1/25) 유지
- 디버그 모드의 TestZone 버튼은 기존처럼 btnAbout 아래에 배치 (하단 여백 약간 감소)

---

## 레이아웃 비교

```
[기존 방식]                    [변경 후]
┌─────────────────┐           ┌─────────────────┐
│  (상단 여백)     │  불균형    │  (상단 여백)     │  균등
│    [로고]        │           │    [로고]        │
│    [버튼1]       │           │    [버튼1]       │
│    [버튼2]       │           │    [버튼2]       │
│      ...        │           │      ...        │
│    [버튼5]       │           │    [버튼5]       │
│                 │           │  (하단 여백)     │  균등
│  (하단 여백)     │  남는공간   └─────────────────┘
└─────────────────┘
```

---

## 관련 코드

- `TitleScene.java:160-180` - 중앙 정렬 계산
- `TitleScene.java:317-319` - 버튼 배치
- `TitleScene.java:204` - Fireball Y 위치 (로고 연동)
