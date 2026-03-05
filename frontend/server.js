import express from 'express'
import { fileURLToPath } from 'url'
import { dirname, join } from 'path'

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

const app = express()
const distPath = join(__dirname, 'dist')

// Serve static files from the dist directory with cache headers
app.use(
  express.static(distPath, {
    maxAge: '1y',
    immutable: true,
    index: false, // Don't auto-serve index.html for directories — let the fallback handle it
  }),
)

// SPA fallback: serve index.html for all non-file requests
// This allows Vue Router to handle client-side routes like /c/1/financial?payment=cancel
app.get('*', (_req, res) => {
  res.sendFile(join(distPath, 'index.html'))
})

const port = process.env.PORT || 4173
app.listen(port, '0.0.0.0', () => {
  console.log(`Convivium SPA server running on port ${port}`)
})
