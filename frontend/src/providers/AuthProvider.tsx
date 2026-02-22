import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { useNavigate } from "react-router-dom";
import { setAuthToken, setOnUnauthorized } from "../api/client";
import type { AuthResponse } from "../api/auth";

const STORAGE_KEY = "kit_commun_auth";

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  username: string | null;
  roles: string[];
}

function loadStored(): AuthState {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return { accessToken: null, refreshToken: null, username: null, roles: [] };
    const data = JSON.parse(raw) as AuthResponse;
    return {
      accessToken: data.accessToken ?? null,
      refreshToken: data.refreshToken ?? null,
      username: data.username ?? null,
      roles: Array.isArray(data.roles) ? data.roles : [],
    };
  } catch {
    return { accessToken: null, refreshToken: null, username: null, roles: [] };
  }
}

function saveToStorage(data: AuthResponse | null) {
  if (data) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
  } else {
    localStorage.removeItem(STORAGE_KEY);
  }
}

interface AuthContextValue extends AuthState {
  isAuthenticated: boolean;
  login: (data: AuthResponse) => void;
  logout: () => void;
  getToken: () => string | null;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const navigate = useNavigate();
  const [state, setState] = useState<AuthState>(loadStored);

  useEffect(() => {
    setAuthToken(state.accessToken);
  }, [state.accessToken]);

  useEffect(() => {
    setOnUnauthorized(() => {
      setState({ accessToken: null, refreshToken: null, username: null, roles: [] });
      saveToStorage(null);
      setAuthToken(null);
      navigate("/login", { replace: true });
    });
  }, [navigate]);

  const login = useCallback((data: AuthResponse) => {
    const next = {
      accessToken: data.accessToken ?? null,
      refreshToken: data.refreshToken ?? null,
      username: data.username ?? null,
      roles: Array.isArray(data.roles) ? data.roles : [],
    };
    setState(next);
    saveToStorage(data);
    setAuthToken(next.accessToken);
  }, []);

  const logout = useCallback(() => {
    setState({ accessToken: null, refreshToken: null, username: null, roles: [] });
    saveToStorage(null);
    setAuthToken(null);
    navigate("/login", { replace: true });
  }, [navigate]);

  const getToken = useCallback(() => state.accessToken, [state.accessToken]);

  const value = useMemo<AuthContextValue>(
    () => ({
      ...state,
      isAuthenticated: !!state.accessToken,
      login,
      logout,
      getToken,
    }),
    [state, login, logout, getToken]
  );

  return (
    <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
