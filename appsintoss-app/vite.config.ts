import { defineConfig } from 'vite';

export default defineConfig({
    root: '.',
    publicDir: 'public',
    build: {
        outDir: 'dist',
        assetsDir: 'assets',
        // 게임 파일을 그대로 복사
        copyPublicDir: true,
    },
    server: {
        port: 5173,
        host: '127.0.0.1',
    },
});
