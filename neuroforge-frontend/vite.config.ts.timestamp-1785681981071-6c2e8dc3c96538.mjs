// vite.config.ts
import path from "path";
import react from "file:///D:/Downloads/NeuroForge-Fixed_(4)_1784807830345/neuroforge_fixed/NeuroForge-Frontend/node_modules/@vitejs/plugin-react/dist/index.js";
import tailwindcss from "file:///D:/Downloads/NeuroForge-Fixed_(4)_1784807830345/neuroforge_fixed/NeuroForge-Frontend/node_modules/@tailwindcss/vite/dist/index.mjs";
import { defineConfig } from "file:///D:/Downloads/NeuroForge-Fixed_(4)_1784807830345/neuroforge_fixed/NeuroForge-Frontend/node_modules/vite/dist/node/index.js";
var __vite_injected_original_dirname = "D:\\Downloads\\NeuroForge-Fixed_(4)_1784807830345\\neuroforge_fixed\\NeuroForge-Frontend";
var vite_config_default = defineConfig({
  plugins: [react(), tailwindcss()],
  define: {
    global: "globalThis"
  },
  resolve: {
    alias: {
      "@": path.resolve(__vite_injected_original_dirname, "src")
    }
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: (id) => {
          if (id.includes("react") || id.includes("react-dom")) {
            return "react-vendor";
          }
          if (id.includes("@radix-ui")) {
            return "ui-vendor";
          }
          if (id.includes("lucide-react")) {
            return "ui-vendor";
          }
          if (id.includes("@tanstack/react-query")) {
            return "query-vendor";
          }
          if (id.includes("recharts")) {
            return "charts-vendor";
          }
          if (id.includes("react-hook-form") || id.includes("@hookform") || id.includes("zod")) {
            return "form-vendor";
          }
          if (id.includes("@dnd-kit")) {
            return "dnd-vendor";
          }
          if (id.includes("date-fns") || id.includes("react-day-picker")) {
            return "date-vendor";
          }
          if (id.includes("axios") || id.includes("wouter") || id.includes("sonner") || id.includes("framer-motion")) {
            return "other-vendor";
          }
        }
      }
    },
    chunkSizeWarningLimit: 1e3
  },
  server: {
    host: "0.0.0.0",
    port: 5e3,
    allowedHosts: true,
    strictPort: true,
    proxy: {
      "/auth": {
        target: "http://localhost:8081",
        changeOrigin: true
      },
      "/api": {
        target: "http://localhost:8081",
        changeOrigin: true
      }
    }
  }
});
export {
  vite_config_default as default
};
//# sourceMappingURL=data:application/json;base64,ewogICJ2ZXJzaW9uIjogMywKICAic291cmNlcyI6IFsidml0ZS5jb25maWcudHMiXSwKICAic291cmNlc0NvbnRlbnQiOiBbImNvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9kaXJuYW1lID0gXCJEOlxcXFxEb3dubG9hZHNcXFxcTmV1cm9Gb3JnZS1GaXhlZF8oNClfMTc4NDgwNzgzMDM0NVxcXFxuZXVyb2ZvcmdlX2ZpeGVkXFxcXE5ldXJvRm9yZ2UtRnJvbnRlbmRcIjtjb25zdCBfX3ZpdGVfaW5qZWN0ZWRfb3JpZ2luYWxfZmlsZW5hbWUgPSBcIkQ6XFxcXERvd25sb2Fkc1xcXFxOZXVyb0ZvcmdlLUZpeGVkXyg0KV8xNzg0ODA3ODMwMzQ1XFxcXG5ldXJvZm9yZ2VfZml4ZWRcXFxcTmV1cm9Gb3JnZS1Gcm9udGVuZFxcXFx2aXRlLmNvbmZpZy50c1wiO2NvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9pbXBvcnRfbWV0YV91cmwgPSBcImZpbGU6Ly8vRDovRG93bmxvYWRzL05ldXJvRm9yZ2UtRml4ZWRfKDQpXzE3ODQ4MDc4MzAzNDUvbmV1cm9mb3JnZV9maXhlZC9OZXVyb0ZvcmdlLUZyb250ZW5kL3ZpdGUuY29uZmlnLnRzXCI7aW1wb3J0IHBhdGggZnJvbSAncGF0aCc7XG5pbXBvcnQgcmVhY3QgZnJvbSAnQHZpdGVqcy9wbHVnaW4tcmVhY3QnO1xuaW1wb3J0IHRhaWx3aW5kY3NzIGZyb20gJ0B0YWlsd2luZGNzcy92aXRlJztcbmltcG9ydCB7IGRlZmluZUNvbmZpZyB9IGZyb20gJ3ZpdGUnO1xuXG5leHBvcnQgZGVmYXVsdCBkZWZpbmVDb25maWcoe1xuICBwbHVnaW5zOiBbcmVhY3QoKSwgdGFpbHdpbmRjc3MoKV0sXG4gIGRlZmluZToge1xuICAgIGdsb2JhbDogJ2dsb2JhbFRoaXMnLFxuICB9LFxuICByZXNvbHZlOiB7XG4gICAgYWxpYXM6IHtcbiAgICAgICdAJzogcGF0aC5yZXNvbHZlKF9fZGlybmFtZSwgJ3NyYycpLFxuICAgIH0sXG4gIH0sXG4gIGJ1aWxkOiB7XG4gICAgcm9sbHVwT3B0aW9uczoge1xuICAgICAgb3V0cHV0OiB7XG4gICAgICAgIG1hbnVhbENodW5rczogKGlkKSA9PiB7XG4gICAgICAgICAgLy8gUmVhY3QgY29yZVxuICAgICAgICAgIGlmIChpZC5pbmNsdWRlcygncmVhY3QnKSB8fCBpZC5pbmNsdWRlcygncmVhY3QtZG9tJykpIHtcbiAgICAgICAgICAgIHJldHVybiAncmVhY3QtdmVuZG9yJztcbiAgICAgICAgICB9XG4gICAgICAgICAgLy8gUmFkaXggVUkgY29tcG9uZW50c1xuICAgICAgICAgIGlmIChpZC5pbmNsdWRlcygnQHJhZGl4LXVpJykpIHtcbiAgICAgICAgICAgIHJldHVybiAndWktdmVuZG9yJztcbiAgICAgICAgICB9XG4gICAgICAgICAgLy8gSWNvbnNcbiAgICAgICAgICBpZiAoaWQuaW5jbHVkZXMoJ2x1Y2lkZS1yZWFjdCcpKSB7XG4gICAgICAgICAgICByZXR1cm4gJ3VpLXZlbmRvcic7XG4gICAgICAgICAgfVxuICAgICAgICAgIC8vIFJlYWN0IFF1ZXJ5XG4gICAgICAgICAgaWYgKGlkLmluY2x1ZGVzKCdAdGFuc3RhY2svcmVhY3QtcXVlcnknKSkge1xuICAgICAgICAgICAgcmV0dXJuICdxdWVyeS12ZW5kb3InO1xuICAgICAgICAgIH1cbiAgICAgICAgICAvLyBDaGFydHNcbiAgICAgICAgICBpZiAoaWQuaW5jbHVkZXMoJ3JlY2hhcnRzJykpIHtcbiAgICAgICAgICAgIHJldHVybiAnY2hhcnRzLXZlbmRvcic7XG4gICAgICAgICAgfVxuICAgICAgICAgIC8vIEZvcm1zXG4gICAgICAgICAgaWYgKGlkLmluY2x1ZGVzKCdyZWFjdC1ob29rLWZvcm0nKSB8fCBpZC5pbmNsdWRlcygnQGhvb2tmb3JtJykgfHwgaWQuaW5jbHVkZXMoJ3pvZCcpKSB7XG4gICAgICAgICAgICByZXR1cm4gJ2Zvcm0tdmVuZG9yJztcbiAgICAgICAgICB9XG4gICAgICAgICAgLy8gRHJhZyBhbmQgZHJvcFxuICAgICAgICAgIGlmIChpZC5pbmNsdWRlcygnQGRuZC1raXQnKSkge1xuICAgICAgICAgICAgcmV0dXJuICdkbmQtdmVuZG9yJztcbiAgICAgICAgICB9XG4gICAgICAgICAgLy8gRGF0ZSBoYW5kbGluZ1xuICAgICAgICAgIGlmIChpZC5pbmNsdWRlcygnZGF0ZS1mbnMnKSB8fCBpZC5pbmNsdWRlcygncmVhY3QtZGF5LXBpY2tlcicpKSB7XG4gICAgICAgICAgICByZXR1cm4gJ2RhdGUtdmVuZG9yJztcbiAgICAgICAgICB9XG4gICAgICAgICAgLy8gT3RoZXIgdmVuZG9yc1xuICAgICAgICAgIGlmIChpZC5pbmNsdWRlcygnYXhpb3MnKSB8fCBpZC5pbmNsdWRlcygnd291dGVyJykgfHwgaWQuaW5jbHVkZXMoJ3Nvbm5lcicpIHx8IGlkLmluY2x1ZGVzKCdmcmFtZXItbW90aW9uJykpIHtcbiAgICAgICAgICAgIHJldHVybiAnb3RoZXItdmVuZG9yJztcbiAgICAgICAgICB9XG4gICAgICAgIH0sXG4gICAgICB9LFxuICAgIH0sXG4gICAgY2h1bmtTaXplV2FybmluZ0xpbWl0OiAxMDAwLFxuICB9LFxuICBzZXJ2ZXI6IHtcbiAgICBob3N0OiAnMC4wLjAuMCcsXG4gICAgcG9ydDogNTAwMCxcbiAgICBhbGxvd2VkSG9zdHM6IHRydWUsXG4gICAgc3RyaWN0UG9ydDogdHJ1ZSxcbiAgICBwcm94eToge1xuICAgICAgJy9hdXRoJzoge1xuICAgICAgICB0YXJnZXQ6ICdodHRwOi8vbG9jYWxob3N0OjgwODEnLFxuICAgICAgICBjaGFuZ2VPcmlnaW46IHRydWUsXG4gICAgICB9LFxuICAgICAgJy9hcGknOiB7XG4gICAgICAgIHRhcmdldDogJ2h0dHA6Ly9sb2NhbGhvc3Q6ODA4MScsXG4gICAgICAgIGNoYW5nZU9yaWdpbjogdHJ1ZSxcbiAgICAgIH0sXG4gICAgfSxcbiAgfSxcbn0pOyJdLAogICJtYXBwaW5ncyI6ICI7QUFBd2IsT0FBTyxVQUFVO0FBQ3pjLE9BQU8sV0FBVztBQUNsQixPQUFPLGlCQUFpQjtBQUN4QixTQUFTLG9CQUFvQjtBQUg3QixJQUFNLG1DQUFtQztBQUt6QyxJQUFPLHNCQUFRLGFBQWE7QUFBQSxFQUMxQixTQUFTLENBQUMsTUFBTSxHQUFHLFlBQVksQ0FBQztBQUFBLEVBQ2hDLFFBQVE7QUFBQSxJQUNOLFFBQVE7QUFBQSxFQUNWO0FBQUEsRUFDQSxTQUFTO0FBQUEsSUFDUCxPQUFPO0FBQUEsTUFDTCxLQUFLLEtBQUssUUFBUSxrQ0FBVyxLQUFLO0FBQUEsSUFDcEM7QUFBQSxFQUNGO0FBQUEsRUFDQSxPQUFPO0FBQUEsSUFDTCxlQUFlO0FBQUEsTUFDYixRQUFRO0FBQUEsUUFDTixjQUFjLENBQUMsT0FBTztBQUVwQixjQUFJLEdBQUcsU0FBUyxPQUFPLEtBQUssR0FBRyxTQUFTLFdBQVcsR0FBRztBQUNwRCxtQkFBTztBQUFBLFVBQ1Q7QUFFQSxjQUFJLEdBQUcsU0FBUyxXQUFXLEdBQUc7QUFDNUIsbUJBQU87QUFBQSxVQUNUO0FBRUEsY0FBSSxHQUFHLFNBQVMsY0FBYyxHQUFHO0FBQy9CLG1CQUFPO0FBQUEsVUFDVDtBQUVBLGNBQUksR0FBRyxTQUFTLHVCQUF1QixHQUFHO0FBQ3hDLG1CQUFPO0FBQUEsVUFDVDtBQUVBLGNBQUksR0FBRyxTQUFTLFVBQVUsR0FBRztBQUMzQixtQkFBTztBQUFBLFVBQ1Q7QUFFQSxjQUFJLEdBQUcsU0FBUyxpQkFBaUIsS0FBSyxHQUFHLFNBQVMsV0FBVyxLQUFLLEdBQUcsU0FBUyxLQUFLLEdBQUc7QUFDcEYsbUJBQU87QUFBQSxVQUNUO0FBRUEsY0FBSSxHQUFHLFNBQVMsVUFBVSxHQUFHO0FBQzNCLG1CQUFPO0FBQUEsVUFDVDtBQUVBLGNBQUksR0FBRyxTQUFTLFVBQVUsS0FBSyxHQUFHLFNBQVMsa0JBQWtCLEdBQUc7QUFDOUQsbUJBQU87QUFBQSxVQUNUO0FBRUEsY0FBSSxHQUFHLFNBQVMsT0FBTyxLQUFLLEdBQUcsU0FBUyxRQUFRLEtBQUssR0FBRyxTQUFTLFFBQVEsS0FBSyxHQUFHLFNBQVMsZUFBZSxHQUFHO0FBQzFHLG1CQUFPO0FBQUEsVUFDVDtBQUFBLFFBQ0Y7QUFBQSxNQUNGO0FBQUEsSUFDRjtBQUFBLElBQ0EsdUJBQXVCO0FBQUEsRUFDekI7QUFBQSxFQUNBLFFBQVE7QUFBQSxJQUNOLE1BQU07QUFBQSxJQUNOLE1BQU07QUFBQSxJQUNOLGNBQWM7QUFBQSxJQUNkLFlBQVk7QUFBQSxJQUNaLE9BQU87QUFBQSxNQUNMLFNBQVM7QUFBQSxRQUNQLFFBQVE7QUFBQSxRQUNSLGNBQWM7QUFBQSxNQUNoQjtBQUFBLE1BQ0EsUUFBUTtBQUFBLFFBQ04sUUFBUTtBQUFBLFFBQ1IsY0FBYztBQUFBLE1BQ2hCO0FBQUEsSUFDRjtBQUFBLEVBQ0Y7QUFDRixDQUFDOyIsCiAgIm5hbWVzIjogW10KfQo=
