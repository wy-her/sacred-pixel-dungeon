# 134. 레벨 전환 후 이동 불가 버그 수정 (모든 직업)

## 배경

트롤 대장장이 퀘스트(별도 마이닝 레벨)를 완료하고 cave 레벨로 돌아오거나, Beacon of Returning으로 다른 층으로 이동한 후 모든 영웅 직업에서 이동이 불가능해지는 버그가 있었습니다. 이 버그는 프리러너뿐만 아니라 숲지기, 수도사 등 모든 직업에서 발생했습니다.

---

## 원인 분석

### Hero.ready 상태 미설정

`Dungeon.switchLevel()` 메서드에서 레벨 전환이 완료된 후 `hero.ready` 상태를 true로 설정하지 않았습니다.

```java
// Dungeon.switchLevel() - 기존 코드
hero.curAction = hero.lastAction = null;
// hero.ready가 설정되지 않음!

observe();
```

### GameScene 초기화 문제

`GameScene.create()`에서 `hero.ready`가 false이면 `ready()`를 호출하지 않아 이동 입력이 차단되었습니다.

```java
// GameScene.create() 라인 795-799
if (Dungeon.hero.ready) {
    ready();  // hero.ready가 false면 호출되지 않음
}
```

### 영향 범위

- 트롤 대장장이 퀘스트 완료 후 cave 복귀
- Beacon of Returning으로 다른 층 이동
- 기타 모든 레벨 전환 (사다리, 함정 등)

---

## 변경 사항

### Dungeon.switchLevel()에서 hero.ready 설정

```diff
  hero.curAction = hero.lastAction = null;
+ hero.ready = true;

  observe();
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `Dungeon.java:505` | `hero.ready = true;` 추가 |

---

## 영향

- 모든 영웅 직업에서 레벨 전환 후 즉시 이동 가능
- 트롤 대장장이 퀘스트 완료 후 이동 가능
- Beacon of Returning 사용 후 이동 가능
- 기존 레벨 전환 로직에 영향 없음

---

## 관련 코드

- `Dungeon.switchLevel()` - 레벨 전환 처리
- `GameScene.create()` - 게임 씬 초기화 및 ready() 호출
- `Hero.ready` - 영웅 입력 수신 가능 상태
- 관련 changelog: #133 (프리러너 Momentum 타이밍 버그 수정)
