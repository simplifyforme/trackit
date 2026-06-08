import { api } from './client';
import type {
  ChangeOrderStatusRequest,
  ChangePasswordRequest,
  CreateOrderRequest,
  CreateTodoRequest,
  GmailConnectResponse,
  GmailStatusResponse,
  LoginRequest,
  LoginResponse,
  MessageResponse,
  OrderResponse,
  OrderStatus,
  RefreshResponse,
  RegisterRequest,
  ResetPasswordRequest,
  SettingsRequest,
  SettingsResponse,
  TodoResponse,
  UpdateOrderRequest,
  UpdateTodoRequest,
  UserResponse,
} from '../../types/api';

export const authApi = {
  register: (data: RegisterRequest) =>
    api.post<MessageResponse>('/api/auth/register', data),

  confirm: (token: string) =>
    api.get<MessageResponse>(`/api/auth/confirm?token=${encodeURIComponent(token)}`),

  login: (data: LoginRequest) =>
    api.post<LoginResponse>('/api/auth/login', data),

  logout: (refreshToken: string) =>
    api.post<void>('/api/auth/logout', { refreshToken }),

  refresh: (refreshToken: string) =>
    api.post<RefreshResponse>('/api/auth/refresh', { refreshToken }),

  forgotPassword: (email: string) =>
    api.post<MessageResponse>('/api/auth/forgot-password', { email }),

  resetPassword: (data: ResetPasswordRequest) =>
    api.post<MessageResponse>('/api/auth/reset-password', data),

  changePassword: (data: ChangePasswordRequest) =>
    api.post<MessageResponse>('/api/auth/change-password', data),
};

export const userApi = {
  me: () => api.get<UserResponse>('/api/users/me'),
};

export const todoApi = {
  list: (params?: { sortBy?: string; showDone?: boolean }) => {
    const qs = new URLSearchParams();
    if (params?.sortBy) qs.set('sortBy', params.sortBy);
    if (params?.showDone != null) qs.set('showDone', String(params.showDone));
    const q = qs.toString();
    return api.get<TodoResponse[]>(`/api/todos${q ? '?' + q : ''}`);
  },
  get: (id: string) => api.get<TodoResponse>(`/api/todos/${id}`),
  create: (data: CreateTodoRequest) => api.post<TodoResponse>('/api/todos', data),
  update: (id: string, data: UpdateTodoRequest) => api.put<TodoResponse>(`/api/todos/${id}`, data),
  delete: (id: string) => api.delete<void>(`/api/todos/${id}`),
};

export const orderApi = {
  list: (params?: { status?: OrderStatus; sortBy?: string }) => {
    const qs = new URLSearchParams();
    if (params?.status) qs.set('status', params.status);
    if (params?.sortBy) qs.set('sortBy', params.sortBy);
    const q = qs.toString();
    return api.get<OrderResponse[]>(`/api/orders${q ? '?' + q : ''}`);
  },
  get: (id: string) => api.get<OrderResponse>(`/api/orders/${id}`),
  create: (data: CreateOrderRequest) => api.post<OrderResponse>('/api/orders', data),
  update: (id: string, data: UpdateOrderRequest) => api.put<OrderResponse>(`/api/orders/${id}`, data),
  changeStatus: (id: string, data: ChangeOrderStatusRequest) =>
    api.post<OrderResponse>(`/api/orders/${id}/status`, data),
  delete: (id: string) => api.delete<void>(`/api/orders/${id}`),
};

export const settingsApi = {
  get: () => api.get<SettingsResponse>('/api/settings'),
  save: (data: SettingsRequest) => api.put<SettingsResponse>('/api/settings', data),
};

export const gmailApi = {
  status: () => api.get<GmailStatusResponse>('/api/gmail/status'),
  connect: () => api.post<GmailConnectResponse>('/api/gmail/connect'),
  disconnect: () => api.delete<MessageResponse>('/api/gmail/disconnect'),
};
