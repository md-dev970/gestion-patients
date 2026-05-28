# Hospital Platform Frontend

React + TypeScript + Vite frontend for the hospital patient microservices platform.

The UI communicates with the backend through `gateway-service`; it should not call individual microservices directly.

## Stack

- React 18
- TypeScript
- Vite
- Chakra UI
- Axios
- React Router

## Requirements

- Node.js 18 or newer
- npm
- Backend gateway running on `http://localhost:8080` or another configured URL

## Configuration

Create a local environment file when you need to override the API URL:

```bash
cp .env.example .env
```

Default:

```text
VITE_API_URL=http://localhost:8080
```

If the backend is running with HTTPS locally, use:

```text
VITE_API_URL=https://localhost:8080
```

For self-signed development certificates, the browser must trust or explicitly accept the certificate before API requests will succeed.

## Development

Install dependencies:

```bash
npm install
```

Start the dev server:

```bash
npm run dev
```

The app is usually available at:

```text
http://localhost:5173
```

If port `5173` is already in use, Vite will choose the next available port.

## Backend Startup

Start the backend before using the UI:

```bash
docker-compose up -d --build
```

Then verify the gateway:

```bash
curl http://localhost:8080/actuator/health
```

## Build

Create a production build:

```bash
npm run build
```

Preview the production build locally:

```bash
npm run preview
```

The generated files are written to `dist/`.

## API Access Pattern

All API calls should go through the gateway URL:

```text
${VITE_API_URL}/api/...
```

Examples:

- `${VITE_API_URL}/api/auth/login`
- `${VITE_API_URL}/api/patients`
- `${VITE_API_URL}/api/appointments`
- `${VITE_API_URL}/api/medical-records`
- `${VITE_API_URL}/api/consultations`

Authenticated requests should send:

```http
Authorization: Bearer <accessToken>
```

## Common Issues

| Symptom | Cause | Fix |
|---------|-------|-----|
| Network errors in browser | Gateway is not running | Start backend and check `/actuator/health` |
| `401 Unauthorized` | Missing or expired token | Login again |
| `403 Forbidden` | Role is not allowed by gateway RBAC | Use an account with the required role |
| CORS failure | Gateway CORS config or wrong API URL | Verify `VITE_API_URL` and gateway config |
| HTTPS certificate warning | Local self-signed certificate | Accept/trust the dev certificate |
