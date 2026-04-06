// vite.config.ts
import { defineConfig } from "vite";
import solidPlugin from "vite-plugin-solid";

export default defineConfig({
  plugins: [solidPlugin()],

  // Tauri dev server must listen on a fixed port
  server: {
    port: 5173,
    strictPort: true,
    // Only listen on localhost for security
    host: "localhost",
  },

  // Tauri uses file:// in production; tell Vite to use relative paths
  base: "./",

  build: {
    target: "esnext",
    minify: "esbuild",
    outDir: "dist",
    sourcemap: false,
    // Keep chunk sizes reasonable for Tauri bundler
    chunkSizeWarningLimit: 1000,
  },

  // Make sure Tauri's IPC global (__TAURI__) is not tree-shaken
  define: {
    "__TAURI_IPC__": JSON.stringify(true),
  },
});
