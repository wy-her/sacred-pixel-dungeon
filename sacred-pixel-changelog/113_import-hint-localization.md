# 113. Import Hint 로컬라이제이션 통일

**날짜**: 2026-06-30

## 개요

DataScene의 URL Import 안내 메시지(`import_hint`)를 한국어 기준으로 전체 언어에서 통일. "Enter 키를 누르세요" 형식 대신 각 언어에 자연스러운 "열기/실행/액세스" 표현으로 변경.

---

## 변경 사항

### Localization (23 languages)

---

### [L-1] 통일 기준 (한국어)

**파일**: `core/src/main/assets/messages/scenes/scenes_ko.properties`

**기준 텍스트**:
```properties
scenes.datascene.import_hint=브라우저 주소창에 URL을 입력 후 실행하세요.
```

**핵심 구조**: "주소창에 URL 입력 + 실행하세요" (Enter 키 언급 없음)

---

### [L-2] 동아시아 언어 (3개)

자연스러운 "액세스/방문" 표현 사용:

| 언어 | 변경 후 |
|------|---------|
| 일본어 (ja) | `ブラウザのアドレスバーにURLを入力してアクセスしてください。` |
| 중국어 간체 (zh) | `在浏览器地址栏输入URL后访问。` |
| 중국어 번체 (zh-hant) | `在瀏覽器地址欄輸入URL後訪問。` |

---

### [L-3] 영어 및 게르만어권 (4개)

자연스러운 "open" 표현 사용:

| 언어 | 변경 후 |
|------|---------|
| English | `Enter the URL in your browser's address bar and open it.` |
| German (de) | `Geben Sie die URL in die Adressleiste Ihres Browsers ein und öffnen Sie sie.` |
| Dutch (nl) | `Plak de gekopieerde URL in de adresbalk van je browser en open deze.` |
| Swedish (sv) | `Klistra in den kopierade URL:en i webbläsarens adressfält och öppna.` |

---

### [L-4] 로망스어권 (4개)

자연스러운 "access/accéder/acceder" 표현 사용:

| 언어 | 변경 후 |
|------|---------|
| French (fr) | `Entrez l'URL dans la barre d'adresse de votre navigateur et accédez-y.` |
| Spanish (es) | `Ingrese la URL en la barra de direcciones de su navegador y acceda.` |
| Italian (it) | `Inserisci l'URL nella barra degli indirizzi del browser e accedi.` |
| Portuguese (pt) | `Digite a URL na barra de endereços do navegador e acesse.` |

---

### [L-5] 슬라브어권 (4개)

자연스러운 "go to/перейти" 표현 사용:

| 언어 | 변경 후 |
|------|---------|
| Russian (ru) | `Введите URL в адресную строку браузера и перейдите по нему.` |
| Ukrainian (uk) | `Введіть URL в адресний рядок браузера і перейдіть за ним.` |
| Polish (pl) | `Wpisz URL w pasek adresu przeglądarki i przejdź do niego.` |
| Czech (cs) | `Zadejte URL do adresního řádku prohlížeče a přejděte na něj.` |

---

### [L-6] 기타 언어 (7개)

| 언어 | 변경 후 |
|------|---------|
| Belarusian (be) | `Устаўце скапіяваную спасылку ў адрасны радок браўзера і перайдзіце па ёй.` |
| Esperanto (eo) | `Algluu la kopiitan URL en la adresan strion de via retumilo kaj malfermu.` |
| Greek (el) | `Εισάγετε τη διεύθυνση URL στη γραμμή διευθύνσεων του προγράμματος περιήγησής σας και μεταβείτε.` |
| Hungarian (hu) | `Írja be az URL-t a böngésző címsorába, majd nyissa meg.` |
| Indonesian (in) | `Masukkan URL di bilah alamat browser dan buka.` |
| Turkish (tr) | `URL'yi tarayıcınızın adres çubuğuna girin ve açın.` |
| Vietnamese (vi) | `Nhập URL vào thanh địa chỉ của trình duyệt và mở nó.` |

---

## 변경 원칙

1. **한국어 기준**: "Enter 키" 언급 없이 자연스러운 "실행하세요" 표현
2. **언어별 자연스러운 표현**:
   - 동아시아: "액세스/방문" (アクセス, 访问, 訪問)
   - 게르만어: "열기" (open, öffnen, avaa)
   - 로망스어: "접근" (accéder, acceder, accedi, acesse)
   - 슬라브어: "이동" (перейти, přejděte, przejdź)
   - 기타: 각 언어에서 가장 자연스러운 표현 선택

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `messages/scenes/scenes.properties` | Enter → open |
| `messages/scenes/scenes_be.properties` | Enter → перайдзіце |
| `messages/scenes/scenes_cs.properties` | Enter → přejděte |
| `messages/scenes/scenes_de.properties` | Enter → öffnen |
| `messages/scenes/scenes_el.properties` | Enter → μεταβείτε |
| `messages/scenes/scenes_eo.properties` | Enter → malfermu |
| `messages/scenes/scenes_es.properties` | Enter → acceda |
| `messages/scenes/scenes_fr.properties` | Enter → accédez-y |
| `messages/scenes/scenes_hu.properties` | Enter → nyissa meg |
| `messages/scenes/scenes_in.properties` | Enter → buka |
| `messages/scenes/scenes_it.properties` | Enter → accedi |
| `messages/scenes/scenes_ja.properties` | Enter → アクセスして |
| `messages/scenes/scenes_ko.properties` | (기준 - 변경 없음) |
| `messages/scenes/scenes_nl.properties` | Enter → open |
| `messages/scenes/scenes_pl.properties` | Enter → przejdź |
| `messages/scenes/scenes_pt.properties` | Enter → acesse |
| `messages/scenes/scenes_ru.properties` | Enter → перейдите |
| `messages/scenes/scenes_sv.properties` | Enter → öppna |
| `messages/scenes/scenes_tr.properties` | Enter → açın |
| `messages/scenes/scenes_uk.properties` | Enter → перейдіть |
| `messages/scenes/scenes_vi.properties` | Enter → mở |
| `messages/scenes/scenes_zh.properties` | Enter → 访问 |
| `messages/scenes/scenes_zh-hant.properties` | Enter → 訪問 |

---
