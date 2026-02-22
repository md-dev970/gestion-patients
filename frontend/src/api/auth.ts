import { apiClient } from "./client";

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  username: string;
  roles: string[];
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  staffId?: number;
  role?: string;
}

export function login(body: LoginRequest) {
  return apiClient.post<AuthResponse>("/api/auth/login", body);
}

export function register(body: RegisterRequest) {
  return apiClient.post<AuthResponse>("/api/auth/register", body);
}

export function refresh(refreshToken: string) {
  return apiClient.post<AuthResponse>("/api/auth/refresh", null, {
    headers: { "Refresh-Token": refreshToken },
  });
}
