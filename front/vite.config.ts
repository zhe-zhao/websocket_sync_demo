import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      "/ws": {
        // proxy to rust backend
        // target: "http://127.0.0.1:3333",
        // proxy to kotlin backend
        target: "http://127.0.0.1:8080",
        ws: true,
      },
    },
  },
});
