# 076. WndJournal 클릭 수정 및 UI 정리

**날짜**: 2026-05-29

## 개요

WndJournal 탭 콘텐츠의 클릭 문제 해결, 증강 선택창 레이아웃 통일, 데이터 내보내기 주의 메시지 추가, Android 빌드 설정 수정.

---

## 변경 사항

### 1. WndJournal 탭 콘텐츠 클릭 문제 해결

### 문제
- WndJournal에서 '모험 노트', '던전 숙련 지침서', '확인된 물건', '연금술' 탭의 콘텐츠 아이콘 클릭이 안 됨
- JournalScene에서는 동일한 아이콘들이 정상 작동
- '뱃지' 탭은 정상 작동

### 원인
`ScrollingGridPane.java`와 `ScrollingListPane.java`의 `isWindowOpenInScene()` 메서드가 씬에 Window가 있으면 무조건 클릭을 차단함.

- WndJournal 자체가 Window이므로 → 내부 컴포넌트들이 클릭을 받지 못함
- JournalScene은 Window가 아닌 PixelScene이므로 → 정상 작동
- 뱃지 탭(BadgesGrid/BadgesList)은 이 체크를 사용하지 않으므로 → 정상 작동

### 해결
`isWindowOpenInScene()` 대신 `shouldBlockClicks()` 메서드로 변경:
- 부모 Window가 있으면 → `parentWindow.isTopmost()` 확인
- 부모 Window가 없으면 (JournalScene) → 기존 `isWindowOpenInScene()` 확인

### 수정 파일
| 파일 | 변경 내용 |
|------|----------|
| `ui/ScrollingGridPane.java` | `onClick()`에서 `shouldBlockClicks()` 사용 |
| `ui/ScrollingListPane.java` | `onClick()`에서 `shouldBlockClicks()` 사용 |

### 동작 비교
| 상황 | 이전 | 이후 |
|------|------|------|
| WndJournal 탭 콘텐츠 클릭 | 차단 | 허용 |
| WndJournal + 하위 창 열림 | 차단 | 차단 (정상) |
| JournalScene 콘텐츠 클릭 | 허용 | 허용 (변경 없음) |
| JournalScene + 하위 창 열림 | 차단 | 차단 (변경 없음) |

---

## 2. 증강 선택창 레이아웃 통일

### 문제
증강의 돌(Stone of Augmentation) 선택창에서 본문↔버튼 간격이 2px로, WndUseItem의 4px와 불일치.

### 해결
`StoneOfAugmentation.java`의 `WndAugment`에서 본문↔버튼 간격을 4px로 변경.

```java
// 변경 전
float pos = tfMesage.top() + tfMesage.height();

// 변경 후
float pos = tfMesage.top() + tfMesage.height() + MARGIN;
```

### 수정 파일
- `items/stones/StoneOfAugmentation.java`

---

## 3. 데이터 내보내기 주의 메시지 추가

### 변경 내용
데이터 내보내기 성공 창의 `import_hint` 메시지에 주의 문구 추가:

> 주의:
> 진행 중 게임은 내보내는 데이터에 포함되지 않습니다.

### 수정 파일 (23개 언어)
모든 `messages/scenes/scenes_*.properties` 파일의 `import_hint` 키 수정.

| 언어 | 추가된 문구 |
|------|------------|
| en | Note:\nGames in progress are not included in exported data. |
| ko | 주의:\n진행 중 게임은 내보내는 데이터에 포함되지 않습니다. |
| de | Hinweis:\nLaufende Spiele sind nicht in den exportierten Daten enthalten. |
| es | Nota:\nLas partidas en progreso no se incluyen en los datos exportados. |
| fr | Note :\nLes parties en cours ne sont pas incluses dans les données exportées. |
| it | Nota:\nLe partite in corso non sono incluse nei dati esportati. |
| pt | Nota:\nJogos em andamento não são incluídos nos dados exportados. |
| ru | Примечание:\nТекущие игры не включаются в экспортируемые данные. |
| zh | 注意：\n进行中的游戏不包含在导出的数据中。 |
| zh-hant | 注意：\n進行中的遊戲不包含在匯出的資料中。 |
| ja | 注意:\n進行中のゲームはエクスポートデータに含まれません。 |
| pl | Uwaga:\nGry w toku nie są uwzględniane w eksportowanych danych. |
| tr | Not:\nDevam eden oyunlar dışa aktarılan verilere dahil değildir. |
| uk | Примітка:\nПоточні ігри не включаються в експортовані дані. |
| be | Нататка:\nБягучыя гульні не ўключаюцца ў экспартаваныя даныя. |
| cs | Poznamka:\nRozehrane hry nejsou zahrnuty v exportovanych datech. |
| el | Σημείωση:\nΤα παιχνίδια σε εξέλιξη δεν περιλαμβάνονται στα εξαγόμενα δεδομένα. |
| eo | Noto:\nLudoj en progreso ne estas inkluzivitaj en eksportitaj datumoj. |
| hu | Megjegyzés:\nA folyamatban lévő játékok nem szerepelnek az exportált adatokban. |
| in | Catatan:\nGame yang sedang berlangsung tidak termasuk dalam data yang diekspor. |
| nl | Opmerking:\nLopende spellen worden niet opgenomen in de geëxporteerde gegevens. |
| sv | Obs:\nPagaende spel ingar inte i exporterade data. |
| vi | Lưu ý:\nCác trò chơi đang tiến hành không được bao gồm trong dữ liệu xuất. |

---

## 4. Android 빌드 설정 수정

### 변경 내용
Android 앱 빌드 시 화면 방향을 **세로 모드 고정**으로 변경.

### 수정 파일
| 파일 | 변경 전 | 변경 후 |
|------|---------|---------|
| `capacitor-app/android/app/src/main/AndroidManifest.xml` | `userLandscape` | `userPortrait` |
| `Changelog/build_capacitor.md` | 가로 모드 고정 | 세로 모드 고정 |

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `ui/ScrollingGridPane.java` | `shouldBlockClicks()` 메서드 추가 |
| `ui/ScrollingListPane.java` | `shouldBlockClicks()` 메서드 추가 |
| `items/stones/StoneOfAugmentation.java` | 본문↔버튼 간격 4px로 변경 |
| `messages/scenes/scenes*.properties` (23개) | import_hint에 주의 메시지 추가 |
| `capacitor-app/.../AndroidManifest.xml` | userPortrait로 변경 |
| `Changelog/build_capacitor.md` | 세로 모드 고정으로 문서 업데이트 |

---

---

*관련 NEVER-CHANGE 항목: #24 (ScrollingGridPane/ScrollingListPane shouldBlockClicks)*
