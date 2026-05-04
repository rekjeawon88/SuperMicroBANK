import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
      // App.tsx에서 '../lib/utils' 로 import하는 것을 루트 기준으로 해소
      '../lib': path.resolve(__dirname, './lib'),
    },
  },
  server: {
    port: 5173,
    strictPort: true,
    host: true,
  },
})
