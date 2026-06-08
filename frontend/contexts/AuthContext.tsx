import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
} from 'react';
import { authApi, userApi } from '../lib/api/endpoints';
import { storage } from '../lib/storage';
import { setLogoutCallback } from '../lib/api/client';
import type { UserResponse } from '../types/api';

interface AuthState {
  user: UserResponse | null;
  isLoading: boolean;
  isAuthenticated: boolean;
}

interface AuthContextValue extends AuthState {
  login: (email: string, password: string) => Promise<{ error?: string }>;
  register: (email: string, password: string) => Promise<{ error?: string; message?: string }>;
  logout: () => Promise<void>;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [state, setState] = useState<AuthState>({
    user: null,
    isLoading: true,
    isAuthenticated: false,
  });

  // Stable ref so the API client can trigger logout without a circular dependency.
  const logoutRef = useRef<() => Promise<void>>(async () => {});

  const logout = useCallback(async () => {
    const rToken = await storage.getRefreshToken();
    if (rToken) {
      // Best-effort server-side revocation — don't block on failure.
      authApi.logout(rToken).catch(() => {});
    }
    await storage.clearAll();
    setState({ user: null, isLoading: false, isAuthenticated: false });
  }, []);

  useEffect(() => {
    logoutRef.current = logout;
  }, [logout]);

  // Wire the API client's 401 handler to call logout.
  useEffect(() => {
    setLogoutCallback(() => void logoutRef.current());
  }, []);

  const refreshUser = useCallback(async () => {
    const result = await userApi.me();
    if (result.ok) {
      setState((s) => ({ ...s, user: result.data }));
    }
  }, []);

  // Restore session from persisted tokens on app start.
  useEffect(() => {
    async function restore() {
      const [accessToken, refreshToken] = await Promise.all([
        storage.getAccessToken(),
        storage.getRefreshToken(),
      ]);

      if (!accessToken || !refreshToken) {
        setState((s) => ({ ...s, isLoading: false }));
        return;
      }

      // Optimistically mark as authenticated so protected routes can render.
      setState((s) => ({ ...s, isAuthenticated: true }));

      const result = await userApi.me();
      if (result.ok) {
        setState({ user: result.data, isLoading: false, isAuthenticated: true });
      } else {
        // Token rejected — clear and force re-login.
        await storage.clearAll();
        setState({ user: null, isLoading: false, isAuthenticated: false });
      }
    }
    restore();
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    const result = await authApi.login({ email, password });
    if (!result.ok) {
      return { error: result.error.message };
    }

    const { accessToken, refreshToken } = result.data;
    await Promise.all([
      storage.setAccessToken(accessToken),
      storage.setRefreshToken(refreshToken),
    ]);

    setState((s) => ({ ...s, isAuthenticated: true }));

    // Fetch user profile after setting tokens so the API client can attach the header.
    const userResult = await userApi.me();
    if (userResult.ok) {
      setState((s) => ({ ...s, user: userResult.data, isLoading: false }));
    }

    return {};
  }, []);

  const register = useCallback(async (email: string, password: string) => {
    const result = await authApi.register({ email, password });
    if (!result.ok) {
      return { error: result.error.message };
    }
    return { message: result.data.message };
  }, []);

  return (
    <AuthContext.Provider value={{ ...state, login, logout, register, refreshUser }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within <AuthProvider>');
  return ctx;
}
