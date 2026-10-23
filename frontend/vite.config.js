import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 不配置 proxy。页面在 5173，接口在 8080，由 Spring CorsConfig 放行。
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    strictPort: true
  },
  preview: {
    port: 4173,
    strictPort: true
  }
})
