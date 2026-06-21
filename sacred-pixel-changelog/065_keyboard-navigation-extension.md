# 065. 키보드 네비게이션 확장

**날짜**: 2026-05-23

## 개요

퀘스트 창, 상점 창, 강화 창 등 게임 내 주요 Window들에 키보드 네비게이션 지원을 추가했습니다. 이를 통해 마우스 없이도 Tab/Shift+Tab 및 Enter 키로 버튼 간 이동 및 선택이 가능합니다. (버전: 3.3.8-HTML5-indev)

---

## 변경 사항

### 수정된 파일

### 1. WndSadGhost.java (슬픈 유령 퀘스트)

**추가된 내용:**
- 무기/방어구 선택 버튼(`btnWeapon`, `btnArmor`)에 `addFocusable()` 추가
- 확인/취소 버튼에 `addFocusableButton()` 추가

**위치:** `WndSadGhost.java:107-109, 154, 164`

### 2. WndWandmaker.java (지팡이 제작자 퀘스트)

**추가된 내용:**
- 지팡이 선택 버튼(`btnWand1`, `btnWand2`)에 `addFocusable()` 추가
- 확인/취소 버튼에 `addFocusableButton()` 추가

**위치:** `WndWandmaker.java:93, 108, 153, 163`

### 3. WndTradeItem.java (상점 거래)

**추가된 내용:**
- 판매 버튼(`btnSell`, `btnSell1`, `btnSellAll`)에 `addFocusableButton()` 추가
- 구매 버튼(`btnBuy`)에 `addFocusableButton()` 추가
- 훔치기 버튼(`btnSteal`)에 `addFocusableButton()` 추가

**위치:** `WndTradeItem.java:105, 125, 139, 172, 228`

### 4. WndResurrect.java (부활의 발목)

**추가된 내용:**
- 아이템 선택 버튼(`btnItem1`, `btnItem2`)에 `addFocusable()` 추가
- 확인 버튼(`btnContinue`)에 `addFocusableButton()` 추가

**위치:** `WndResurrect.java:89, 101, 126`

### 5. WndRegionComplete.java (구역 완료)

**추가된 내용:**
- 다음 단계 버튼(`btnNextStage`)에 `addFocusableButton()` 추가

**위치:** `WndRegionComplete.java:95`

### 6. WndUpgrade.java (강화 창)

**추가된 내용:**
- 강화 버튼(`btnUpgrade`)에 `addFocusableButton()` 추가
- 취소 버튼(`btnCancel`)에 `addFocusableButton()` 추가

**위치:** `WndUpgrade.java:469, 486`

### 7. WndEnergizeItem.java (연금술 에너지화)

**추가된 내용:**
- 에너지화 버튼들(`btnEnergize`, `btnEnergize1`, `btnEnergizeAll`)에 `addFocusableButton()` 추가

**위치:** `WndEnergizeItem.java:90, 106, 116`

### 8. WndInfoTalent.java (재능 정보)

**추가된 내용:**
- 재능 버튼에 `addFocusableButton()` 추가

**위치:** `WndInfoTalent.java:89`

### 9. HeroSelectScene.java (WndRandomStart)

**추가된 내용:**
- 랜덤 시작 창의 체크박스 및 버튼들에 `addFocusable()` 추가

**위치:** `HeroSelectScene.java:1081-1085`

---

## 키보드 네비게이션 사용법

| 키 | 동작 |
|----|------|
| Tab | 다음 포커스 가능한 요소로 이동 |
| Shift+Tab | 이전 포커스 가능한 요소로 이동 |
| Enter/Space | 현재 포커스된 버튼 클릭 |

---

## 기술적 세부사항

### addFocusable() vs addFocusableButton()

- `addFocusable(Component)`: 일반 컴포넌트(ItemButton 등)를 포커스 목록에 추가
- `addFocusableButton(RedButton)`: RedButton을 포커스 목록에 추가하고 강조 효과 자동 적용

### 포커스 순서

포커스 순서는 `addFocusable()`/`addFocusableButton()` 호출 순서에 따라 결정됩니다. 일반적으로 화면 위에서 아래, 왼쪽에서 오른쪽 순서로 추가합니다.

---

## 테스트 체크리스트

- [x] WndSadGhost: Tab으로 무기/방어구 버튼 간 이동, Enter로 선택
- [x] WndWandmaker: Tab으로 지팡이 버튼 간 이동, Enter로 선택
- [x] WndTradeItem: Tab으로 판매/구매/훔치기 버튼 이동
- [x] WndResurrect: Tab으로 아이템 버튼 및 확인 버튼 이동
- [x] WndRegionComplete: Enter로 다음 단계 진행
- [x] WndUpgrade: Tab으로 강화/취소 버튼 이동
- [x] WndEnergizeItem: Tab으로 에너지화 버튼들 이동
- [x] WndInfoTalent: 재능 버튼 키보드 접근 가능
- [x] HeroSelectScene WndRandomStart: 체크박스 및 버튼 키보드 접근 가능

---

## 영향 받는 게임플레이 상황

| 상황 | 키보드 네비게이션 |
|------|------------------|
| 1층 슬픈 유령 퀘스트 완료 | ✓ |
| 2층 지팡이 제작자 퀘스트 완료 | ✓ |
| 상점에서 아이템 거래 | ✓ |
| 부활의 발목 사용 | ✓ |
| 보스 처치 후 구역 완료 화면 | ✓ |
| 업그레이드 주문서/마법 주입 사용 | ✓ |
| 연금술 가마에서 에너지화 | ✓ |
| 재능 정보 확인 및 선택 | ✓ |
| 랜덤 시작 설정 | ✓ |

---

## 수정된 파일

| File | Changes |
|------|---------|
| `WndSadGhost.java` | 무기/방어구 버튼에 addFocusable() 추가 |
| `WndWandmaker.java` | 지팡이 버튼에 addFocusable() 추가 |
| `WndTradeItem.java` | 판매/구매/훔치기 버튼에 addFocusableButton() 추가 |
| `WndResurrect.java` | 아이템 버튼에 addFocusable() 추가 |
| `WndRegionComplete.java` | 다음 단계 버튼에 addFocusableButton() 추가 |
| `WndUpgrade.java` | 강화/취소 버튼에 addFocusableButton() 추가 |
| `WndEnergizeItem.java` | 에너지화 버튼에 addFocusableButton() 추가 |
| `WndInfoTalent.java` | 재능 버튼에 addFocusableButton() 추가 |
| `HeroSelectScene.java` | WndRandomStart 체크박스/버튼에 addFocusable() 추가 |

---

## 관련 Changelog

- Changelog #71: 키보드 접근성 수정
- Changelog #72: 키보드 접근성 향상
- Changelog #73: 키보드 접근성 개선
- Changelog #81: 키보드 네비게이션 스타일 통일

---
