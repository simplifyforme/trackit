import { storage } from '../storage';
import type { ApiError, Result } from '../../types/api';

const BASE_URL = process.env.EXPO_PUBLIC_API_URL ?? 'http://localhost:8080';

// Singleton promise so concurrent 401s share one refresh attempt instead of
// hammering the refresh endpoint in parallel.
let refreshPromise: Promise<string | null> | null = null;

async function doRefresh(): Promise<string | null> {
  const refreshToken = await storage.getRefreshToken();
  if (!refreshToken) return null;
  try {
    const res = await fetch(`${BASE_URL}/api/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken }),
    });
    if (!res.ok) return null;
    const data = await res.json();
    await storage.setAccessToken(data.accessToken);
    return data.accessToken as string;
  } catch {
    return null;
  }
}

type LogoutCallback = () => void;
let onSessionExpired: LogoutCallback | null = null;

export function setLogoutCallback(cb: LogoutCallback): void {
  onSessionExpired = cb;
}

async function request<T>(
  path: string,
  options: RequestInit,
  isRetry = false,
): Promise<Result<T>> {
  const accessToken = await storage.getAccessToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string> | undefined),
  };
  if (accessToken) {
    headers['Authorization'] = `Bearer ${accessToken}`;
  }

  let res: Response;
  try {
    res = await fetch(`${BASE_URL}${path}`, { ...options, headers });
  } catch {
    return {
      ok: false,
      error: {
        timestamp: new Date().toISOString(),
        status: 0,
        error: 'Network Error',
        message: 'Could not reach the server. Check your connection.',
        path,
      },
    };
  }

  // On 401, try one token refresh then retry the original request.
  if (res.status === 401 && !isRetry) {
    if (!refreshPromise) {
      refreshPromise = doRefresh().finally(() => {
        refreshPromise = null;
      });
    }
    const newToken = await refreshPromise;
    if (newToken) {
      return request<T>(path, options, true);
    }
    // Refresh failed — session is unrecoverable.
    await storage.clearAll();
    onSessionExpired?.();
    return {
      ok: false,
      error: {
        timestamp: new Date().toISOString(),
        status: 401,
        error: 'Unauthorized',
        message: 'Session expired. Please log in again.',
        path,
      },
    };
  }

  // 204 No Content or empty body
  const contentLength = res.headers.get('content-length');
  if (res.status === 204 || contentLength === '0') {
    return { ok: true, data: undefined as T };
  }

  let body: unknown;
  try {
    body = await res.json();
  } catch {
    body = {};
  }

  if (!res.ok) {
    return { ok: false, error: body as ApiError };
  }

  return { ok: true, data: body as T };
}

export const api = {
  get: <T>(path: string, options?: RequestInit) =>
    request<T>(path, { ...options, method: 'GET' }),

  post: <T>(path: string, body?: unknown, options?: RequestInit) =>
    request<T>(path, {
      ...options,
      method: 'POST',
      body: body !== undefined ? JSON.stringify(body) : undefined,
    }),

  put: <T>(path: string, body?: unknown, options?: RequestInit) =>
    request<T>(path, {
      ...options,
      method: 'PUT',
      body: body !== undefined ? JSON.stringify(body) : undefined,
    }),

  delete: <T>(path: string, options?: RequestInit) =>
    request<T>(path, { ...options, method: 'DELETE' }),
};
