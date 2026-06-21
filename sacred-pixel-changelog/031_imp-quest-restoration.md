# 031. Imp 퀘스트 업스트림 복원 및 버전 업데이트

**날짜**: 2026-03-31

## 개요

Imp 퀘스트를 Shattered Pixel Dungeon 3.3.8 업스트림 동작으로 복원하고, 번역 파일을 정리하며, 마이너 변경사항을 적용합니다. (버전: 3.3.8 동기화)

---

## 변경 사항

### [A] Imp 퀘스트 복원

### 변경된 파일

| 파일 | 변경 내용 |
|------|----------|
| `actors/mobs/npcs/Imp.java` | 업스트림 3.3.8 퀘스트 로직으로 복원 |
| `actors/mobs/Monk.java` | `getFloat` → `getInt` 수정 |
| `windows/WndImpReward.java` | 삭제됨 (Sacred 전용 파일) |

### Imp 퀘스트 로직 변경

**이전 (Sacred 커스텀):**
- 금고(Vault) 시스템 기반
- `favor` 포인트 시스템 (토큰 → 호의 변환)
- 다중 보상 선택 (Ring +1, Ring +2, Cash Out)
- `WndImpReward` 창 사용

**복원 후 (업스트림 3.3.8):**
- Monk/Golem 처치 퀘스트
  - 층 17: Monk 처치 (5개 토큰)
  - 층 18: 50/50 랜덤
  - 층 19: Golem 처치 (4개 토큰)
- 몬스터 처치 시 DwarfToken 드롭
- 단일 보상: +2 업그레이드 Ring (저주 걸림)
- `WndImp` 창 사용

### Quest 클래스 변경 사항

```java
// 추가된 필드
private static boolean alternative;  // Monk vs Golem 선택

// 복원된 메서드
public static void process(Mob mob)  // 몬스터 처치 시 토큰 드롭

// 호환성을 위해 유지된 stub 메서드 (Sacred Vault 콘텐츠용)
public static boolean vaultAttempted()
public static void setVaultAttempted()
public static boolean forceSpawn  // 디버그용
```

---

## [B] 메시지 파일 정리

### 제거된 키 (22개 언어 모두)

**actors 파일:**
- `actors.mobs.npcs.imp.vault_intro`
- `actors.mobs.npcs.imp.vault_reminder`
- `actors.mobs.npcs.imp.vault_no_tokens`
- `actors.mobs.npcs.imp.vault_done`
- `actors.mobs.npcs.imp.reward_prompt`
- `actors.mobs.npcs.imp.reward_gold`
- `actors.mobs.npcs.imp.reward_ring1`
- `actors.mobs.npcs.imp.reward_ring2`
- `actors.mobs.npcs.imp.reward_gold_done`

**windows 파일:**
- `windows.wndimpreward.*` (10개 키 전체)

### 추가된 번역

| 키 | 설명 |
|----|------|
| `windows.wndregioncomplete.next_stage` | 18개 언어에 누락 키 추가 |

**번역된 언어:**
be, cs, de, el, eo, es, fr, hu, in, it, ja, nl, pl, pt, ru, sv, tr, uk, vi

---

## [C] 3.3.6 → 3.3.8 변경사항

### 적용된 변경

| 항목 | 설명 | 위험도 |
|------|------|--------|
| Imp 퀘스트 복원 | 업스트림 동작으로 완전 복원 | SAFE |
| 번역 업데이트 | 누락 키 추가, 불필요 키 제거 | SAFE |
| 내부 코드 라이브러리 | 기존 버전 유지 (TeaVM 호환성) | N/A |

### 미적용 변경 (이미 Sacred에 포함)

- Necromancer AI 개선 (Sacred 버전이 더 발전됨)
- UI 개선사항 (대부분 이미 적용됨)

---

## [D] 빌드 정보

**빌드 명령어:**
```bash
./gradlew --no-daemon teavm:buildRelease
```

**빌드 결과:**
- 총 클래스 수: 5370
- 빌드 시간: 1분 42초
- 출력 경로: `teavm/build/dist/webapp/`

---

## [E] 호환성 노트

### 세이브 파일 호환성

⚠️ **주의**: 기존 Sacred 버전에서 Vault 퀘스트를 진행 중이던 세이브 파일은 다음과 같이 동작합니다:

- `vaultAttempted` 플래그가 설정된 경우: 퀘스트 완료 상태로 간주
- `favor` 데이터: 무시됨 (해당 필드 제거됨)
- Vault 레벨에 있는 경우: `EscapeCrystal` 사용 시 정상 탈출 가능

### Sacred 전용 콘텐츠

다음 Sacred 전용 콘텐츠는 유지됩니다 (Imp 퀘스트와 별개):
- `VaultLevel.java` - 금고 레벨
- `EscapeCrystal.java` - 금고 탈출 아이템
- Vault 관련 테스터 기능

---

## 변경 파일 목록

```
core/src/main/java/com/sacredpixel/sacredpixeldungeon/
├── actors/mobs/npcs/Imp.java          [수정됨]
├── actors/mobs/Monk.java              [수정됨]
└── windows/WndImpReward.java          [삭제됨]

core/src/main/assets/messages/
├── actors/actors.properties           [수정됨]
├── actors/actors_*.properties         [수정됨] (22개 언어)
├── windows/windows.properties         [수정됨]
└── windows/windows_*.properties       [수정됨] (22개 언어)
```

---

## 테스트 체크리스트

- [ ] 층 17-19에서 Imp 퀘스트 스폰 확인
- [ ] Monk/Golem 처치 시 DwarfToken 드롭 확인
- [ ] 토큰 4-5개 수집 후 보상 수령 확인
- [ ] 보상 Ring +2 확인
- [ ] 기존 세이브 파일 로드 테스트
- [ ] 모든 언어 메시지 표시 확인
