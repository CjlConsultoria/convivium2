import { vi } from 'vitest'

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
  length: 0,
  key: vi.fn(),
}
Object.defineProperty(globalThis, 'localStorage', { value: localStorageMock })

// Mock window.alert
vi.stubGlobal('alert', vi.fn())
