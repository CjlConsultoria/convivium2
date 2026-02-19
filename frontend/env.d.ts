/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

// RouteMeta ampliado em src/router/index.ts ou onde meta é definido (evita shadowing do vue-router no build)
// Se precisar de meta.layout etc. tipados, use: as { layout?: 'default' | 'auth' | 'admin'; ... }

interface ImportMetaEnv {
  readonly VITE_API_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
