import react from '@vitejs/plugin-react';
import { fileURLToPath } from 'node:url';
import { defineConfig } from 'vite';

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) },
  },
  server: {
    // Let the dev server answer on GitHub Codespaces forwarded addresses.
    allowedHosts: ['.app.github.dev'],
    // In development the API is same-origin, so session cookies and CSRF work unchanged.
    proxy: { '/api': process.env.VITE_DEV_API ?? 'http://localhost:8080' },
  },
});
