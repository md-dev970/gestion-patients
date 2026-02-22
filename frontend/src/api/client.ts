import axios, { type AxiosError } from "axios";

// Use backend gateway URL so requests go to the API, not the Vite dev server.
// Set VITE_API_URL in .env to override (e.g. for production).
const baseURL =
  (import.meta as unknown as { env: { VITE_API_URL?: string } }).env
    .VITE_API_URL || "http://localhost:8080";

export const apiClient = axios.create({
  baseURL,
  headers: { "Content-Type": "application/json" },
});

export function setAuthToken(token: string | null) {
  if (token) {
    apiClient.defaults.headers.common["Authorization"] = `Bearer ${token}`;
  } else {
    delete apiClient.defaults.headers.common["Authorization"];
  }
}

type OnUnauthorized = () => void;

let onUnauthorized: OnUnauthorized = () => {};

export function setOnUnauthorized(handler: OnUnauthorized) {
  onUnauthorized = handler;
}

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      onUnauthorized();
    }
    return Promise.reject(error);
  }
);
