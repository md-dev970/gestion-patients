# KIT COMMUN Frontend

React + TypeScript + Vite + Chakra UI v3 frontend for the KIT COMMUN hospital gateway.

## Setup

1. Copy `.env.example` to `.env` and set the API gateway URL if needed:
   ```bash
   cp .env.example .env
   ```
   Default: `VITE_API_URL=http://localhost:8080` (edit `.env` to match your gateway).

2. Install dependencies and run the dev server:
   ```bash
   npm install
   npm run dev
   ```
   The app will be at `http://localhost:5173` (or the next free port).

## Running with the backend

- Start the backend (gateway and services) first, e.g. via Docker Compose or Maven from the repo root.
- Ensure the gateway is at `http://localhost:8080` (or set `VITE_API_URL` in `.env` to your gateway URL).
- CORS is configured in the gateway to allow the frontend origin (e.g. `http://localhost:5173`).

## Build

```bash
npm run build
```
Output is in `dist/`. Serve with any static file server or use `npm run preview` to try the production build locally.
