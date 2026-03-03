import { vi } from 'vitest'

// Mock localStorage (persiste valores entre setItem/getItem para testes)
const storage: Record<string, string> = {}
const localStorageMock = {
  getItem: vi.fn((key: string) => storage[key] ?? null),
  setItem: vi.fn((key: string, value: string) => { storage[key] = value }),
  removeItem: vi.fn((key: string) => { delete storage[key] }),
  clear: vi.fn(() => { Object.keys(storage).forEach((k) => delete storage[k]) }),
  get length() { return Object.keys(storage).length },
  key: vi.fn((i: number) => Object.keys(storage)[i] ?? null),
}
Object.defineProperty(globalThis, 'localStorage', { value: localStorageMock })

// Mock window.alert
vi.stubGlobal('alert', vi.fn())

// Polyfill DataTransfer para testes de file upload (Node.js/jsdom não tem)
if (typeof DataTransfer === 'undefined') {
  class DataTransferMock {
    _files: File[] = []
    get files(): FileList {
      const list = Object.assign([...this._files], {
        item: (i: number) => this._files[i] ?? null,
      }) as unknown as FileList
      return list
    }
    get items() {
      const self = this
      return {
        add: (file: File) => { self._files.push(file) },
        get length() { return self._files.length },
      } as DataTransferItemList
    }
  }
  ;(globalThis as any).DataTransfer = DataTransferMock
}
