import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  test: {
    globals: true,
    environment: 'jsdom',
    env: { VITE_GOOGLE_CLIENT_ID: '' },
    include: ['src/**/*.{test,spec}.{ts,tsx,vue}'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html', 'lcov'],
      include: ['src/**/*.{ts,tsx,vue}'],
      exclude: [
        'src/main.ts',
        'src/App.vue',
        'src/**/*.d.ts',
        'src/**/*.spec.{ts,tsx,vue}',
        'src/**/*.test.{ts,tsx,vue}',
        'src/router/**',
        'src/types/**',
        'src/api/client.ts',
        'src/views/**',
        'src/layouts/**',
        'src/components/layout/**',
        'src/test/**',
      ],
      thresholds: {
        lines: 80,
        functions: 74,
        branches: 85,
        statements: 80,
      },
    },
    setupFiles: ['./src/test/setup.ts'],
  },
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
})
