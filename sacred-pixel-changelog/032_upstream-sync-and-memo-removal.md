# 032. Upstream 3.3.8 동기화 및 메모 기능 제거

**날짜**: 2026-03-31

## 개요

Sacred Pixel Dungeon의 Vault Quest와 Imp Quest를 Shattered Pixel Dungeon 3.3.8 upstream과 완전히 동기화하고, 사용자 메모 입력 기능을 제거합니다. (버전: 3.3.8 완전 동기화)

---

## 변경 사항

### [A] Vault Quest Upstream 동기화

### 교체된 파일 (Upstream 3.3.8 버전으로)

| 파일 | 설명 |
|------|------|
| `levels/VaultLevel.java` | Vault 레벨 로직 |
| `actors/mobs/VaultMob.java` | Vault 몬스터 기본 클래스 |
| `actors/mobs/VaultRat.java` | Vault 쥐 몬스터 |
| `actors/mobs/npcs/VaultSentry.java` | Vault 센트리 NPC |
| `actors/mobs/npcs/VaultLaser.java` | Vault 레이저 NPC |
| `actors/blobs/VaultFlameTraps.java` | Vault 화염 트랩 시스템 |
| `items/quest/EscapeCrystal.java` | 탈출 크리스탈 아이템 |
| `levels/CityLevel.java` | City 레벨 (Vault 진입 로직 포함) |

### 교체된 Vault Room 파일 (12개)

| 파일 |
|------|
| `AlternatingTrapsRoom.java` |
| `VaultCircleRoom.java` |
| `VaultCrossRoom.java` |
| `VaultEnemyCenterRoom.java` |
| `VaultEntranceRoom.java` |
| `VaultFinalRoom.java` |
| `VaultLasersRoom.java` |
| `VaultLongRoom.java` |
| `VaultQuadrantsRoom.java` |
| `VaultRingRoom.java` |
| `VaultRingsRoom.java` |
| `VaultSimpleEnemyTreasureRoom.java` |

### 새로 추가된 Vault Treasure Room 파일 (7개)

| 파일 | 설명 |
|------|------|
| `treasure/VaultTreasureRoom.java` | 추상 기본 클래스 |
| `treasure/VaultBookcaseTreasureRoom.java` | 책장 퍼즐 방 |
| `treasure/VaultFlamePathRoom.java` | 화염 경로 퍼즐 방 |
| `treasure/VaultLaserTreasureRoom.java` | 레이저 센트리 방 |
| `treasure/VaultManyScansRoom.java` | 다중 스캔 센트리 방 |
| `treasure/VaultMultipleEnemyTreasureRoom.java` | 다중 적 보물 방 |
| `treasure/VaultSingleEnemyTreasureRoom.java` | 단일 적 보물 방 |

### 삭제된 Sacred 전용 파일

| 파일 | 설명 |
|------|------|
| `actors/mobs/VaultGuardian.java` | Sacred 전용 Vault 보스 (삭제됨) |

---

## [B] Imp Quest Upstream 동기화

### 변경된 파일

| 파일 | 변경 내용 |
|------|---------|
| `actors/mobs/npcs/Imp.java` | Upstream 3.3.8 버전으로 완전 교체 |

### 제거된 Sacred 전용 기능

- `Imp.Quest.forceSpawn` 필드 (디버그용)
- `Imp.Quest.vaultAttempted()` 메서드
- `Imp.Quest.setVaultAttempted()` 메서드

---

## [C] 디버그 기능 제거

### InterlevelScene.java

- `testVaultLevel` 플래그 제거
- Vault Level 테스트 모드 블록 제거

### TitleScene.java

- "Vault Level" 디버그 버튼 제거

---

## [D] 메모 기능 제거

### WndUseItem.java

- 아이템 설명 창 우측 상단의 메모 버튼 (`ItemJournalButton`) 제거

### WndJournal.java

- NotesTab에서 커스텀 노트 섹션 제거
- `CustomNoteButton` import 제거

### 영향받는 기능

- 아이템에 메모 추가 기능 비활성화
- 저널 탭에서 커스텀 노트 표시/추가 기능 비활성화

---

## [E] TeaVMClassRegistry 업데이트

### 추가된 클래스 등록

```java
VaultBookcaseTreasureRoom
VaultFlamePathRoom
VaultLaserTreasureRoom
VaultManyScansRoom
VaultMultipleEnemyTreasureRoom
VaultSingleEnemyTreasureRoom
```

---

## [F] 빌드 정보

**빌드 명령어:**
```bash
./gradlew --no-daemon teavm:buildRelease
```

**빌드 결과:**
- 총 클래스 수: 5379
- 빌드 시간: 1분 9초
- 출력 경로: `teavm/build/dist/webapp/`

---

## [G] 주요 차이점 (Sacred → Upstream)

### Vault Quest

| 항목 | Sacred (이전) | Upstream 3.3.8 (현재) |
|------|--------------|---------------------|
| 진입 방식 | 직접 진입 + vaultAttempted 체크 | WndOptions 다이얼로그로 확인 후 진입 |
| VaultMob | 커스텀 알람/체인 알러트 시스템 | 단순 감지 원뿔 시스템 |
| VaultRat | HP 28, 알람 레벨 스케일링 | HP 8, 단순 행동 |
| VaultSentry | 알람 발생/체인 알러트 | 쿨다운 기반 스캔 |
| VaultLaser | 데미지 + Cripple 적용 | 감지 전용 (데미지 없음) |
| VaultGuardian | 존재 (Sacred 전용) | 존재하지 않음 |
| Treasure Room | 없음 | 7종 추가 |

### CityLevel activateTransition

| Sacred (이전) | Upstream 3.3.8 (현재) |
|--------------|---------------------|
| `Imp.Quest.vaultAttempted()` 체크 | WndOptions 다이얼로그 표시 |
| `EscapeCrystal` 직접 생성 | 사용자 확인 후 `EscapeCrystal` 생성 |
| `prevHT` 보존 로직 (테스트용) | 표준 HP 업데이트 |

---

## 변경 파일 목록

```
core/src/main/java/com/sacredpixel/sacredpixeldungeon/
├── actors/mobs/npcs/Imp.java                    [교체됨]
├── actors/mobs/VaultMob.java                    [교체됨]
├── actors/mobs/VaultRat.java                    [교체됨]
├── actors/mobs/VaultGuardian.java               [삭제됨]
├── actors/mobs/npcs/VaultSentry.java            [교체됨]
├── actors/mobs/npcs/VaultLaser.java             [교체됨]
├── actors/blobs/VaultFlameTraps.java            [교체됨]
├── items/quest/EscapeCrystal.java               [교체됨]
├── levels/VaultLevel.java                       [교체됨]
├── levels/CityLevel.java                        [교체됨]
├── levels/rooms/quest/vault/*.java              [12개 교체됨]
├── levels/rooms/quest/vault/treasure/*.java     [7개 추가됨]
├── scenes/InterlevelScene.java                  [수정됨]
├── scenes/TitleScene.java                       [수정됨]
├── windows/WndUseItem.java                      [수정됨]
└── windows/WndJournal.java                      [수정됨]

teavm/src/main/java/com/sacredpixel/sacredpixeldungeon/teavm/
└── TeaVMClassRegistry.java                      [수정됨]
```

---

## 테스트 체크리스트

- [ ] CityLevel에서 Vault 진입 다이얼로그 표시 확인
- [ ] Vault 레벨 진입 및 탐험 정상 동작 확인
- [ ] VaultRat, VaultSentry, VaultLaser 정상 동작 확인
- [ ] Treasure Room들 정상 생성 확인
- [ ] EscapeCrystal로 Vault 탈출 확인
- [ ] Imp Quest (Monk/Golem 처치) 정상 동작 확인
- [ ] 아이템 설명 창에서 메모 버튼 제거 확인
- [ ] 저널 탭에서 커스텀 노트 섹션 제거 확인
