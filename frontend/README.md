# NeuroForge

A React + Vite + TypeScript web application.

## Getting Started

```bash
npm install
npm run dev
```

Open [http://localhost:5173](http://localhost:5173) in your browser.

## Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start the development server |
| `npm run build` | Build for production |
| `npm run preview` | Preview the production build |
| `npm run typecheck` | Run TypeScript type check |

## Backend

The frontend proxies two backend services:

- `/api/*` → `http://localhost:8080` (main API server)
- `/auth/*` → `http://localhost:8081` (auth service)

Start your backend servers separately before running the frontend.

## Tech Stack

- **React 19** + **Vite 7** + **TypeScript 5**
- **Tailwind CSS v4** + shadcn/ui components
- **Wouter** — lightweight client-side routing
- **TanStack Query** — async state / data fetching
- **React Hook Form** + **Zod** — form validation
- **Framer Motion** — animations
- **Recharts** — charts and data visualization
- **Axios** — HTTP client
