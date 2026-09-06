import { defineConfig } from '@apps-in-toss/web-framework/config';

export default defineConfig({
  appName: 'sacred-pixel-dungeon',
  brand: {
    displayName: '세이크리드 픽셀 던전',
    icon: 'https://sacredpixeldungeon.pages.dev/assets/icons/icon_gold.png',
    primaryColor: '#FFD700',
  },
  permissions: [],
  navigationBar: {
    theme: 'dark',
    // 내비게이션 바를 투명하게 만들어 WebView가 화면 최상단부터 시작하도록 한다.
    // 상태바 회피는 main.ts에서 SafeAreaInsets.top으로 처리한다.
    // (더보기/X 버튼이 배너 위에 겹치는 것은 의도된 동작)
    transparentBackground: true,
  },
  web: {
    host: 'localhost',
    port: 5173,
    commands: {
      dev: 'npm run dev',
      build: 'npm run build:vite',
    },
  },
  webViewProps: {
    type: 'game',
    allowsInlineMediaPlayback: true,
    mediaPlaybackRequiresUserAction: false,
    bounces: false,
    overScrollMode: 'never',
  },
  outdir: 'dist',
});
