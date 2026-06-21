import { defineConfig } from '@apps-in-toss/web-framework/config';

export default defineConfig({
  appName: 'sacred-pixel-dungeon',
  brand: {
    displayName: '세이크리드 픽셀 던전',
    icon: 'https://sacredpixeldungeon.pages.dev/assets/icons/icon_gold.png',
    primaryColor: '#FFD700',
  },
  permissions: [],
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
