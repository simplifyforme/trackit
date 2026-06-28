import { api } from './client';
import type {
  ArticleMetadataResponse,
  ArticleResponse,
  BookMetadataResponse,
  BookResponse,
  ChangeOrderStatusRequest,
  ChangePasswordRequest,
  CreateArticleRequest,
  CreateBookRequest,
  CreateOrderRequest,
  CreateTodoRequest,
  CreateWishlistItemRequest,
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
  UpdateArticleRequest,
  UpdateArticleStatusRequest,
  UpdateBookRequest,
  UpdateBookStatusRequest,
  UpdateOrderRequest,
  UpdateTodoRequest,
  UpdateWishlistItemRequest,
  UserResponse,
  WishlistItemResponse,
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

export const wishlistApi = {
  list: () => api.get<WishlistItemResponse[]>('/api/wishlist'),
  get: (id: string) => api.get<WishlistItemResponse>(`/api/wishlist/${id}`),
  create: (data: CreateWishlistItemRequest) => api.post<WishlistItemResponse>('/api/wishlist', data),
  update: (id: string, data: UpdateWishlistItemRequest) =>
    api.put<WishlistItemResponse>(`/api/wishlist/${id}`, data),
  delete: (id: string) => api.delete<void>(`/api/wishlist/${id}`),
  refreshImage: (id: string) => api.post<WishlistItemResponse>(`/api/wishlist/${id}/refresh-image`, {}),
};

export const bookApi = {
  list: () => api.get<BookResponse[]>('/api/books'),
  get: (id: string) => api.get<BookResponse>(`/api/books/${id}`),
  create: (data: CreateBookRequest) => api.post<BookResponse>('/api/books', data),
  update: (id: string, data: UpdateBookRequest) => api.put<BookResponse>(`/api/books/${id}`, data),
  updateStatus: (id: string, data: UpdateBookStatusRequest) =>
    api.put<BookResponse>(`/api/books/${id}/status`, data),
  delete: (id: string) => api.delete<void>(`/api/books/${id}`),
  refreshMetadata: (id: string) => api.post<BookResponse>(`/api/books/${id}/refresh-metadata`, {}),
  fetchMetadataPreview: (url: string) =>
    api.post<BookMetadataResponse>('/api/books/metadata-preview', { url }),
};

export const articleApi = {
  list: () => api.get<ArticleResponse[]>('/api/articles'),
  get: (id: string) => api.get<ArticleResponse>(`/api/articles/${id}`),
  create: (data: CreateArticleRequest) => api.post<ArticleResponse>('/api/articles', data),
  update: (id: string, data: UpdateArticleRequest) => api.put<ArticleResponse>(`/api/articles/${id}`, data),
  updateStatus: (id: string, data: UpdateArticleStatusRequest) =>
    api.put<ArticleResponse>(`/api/articles/${id}/status`, data),
  delete: (id: string) => api.delete<void>(`/api/articles/${id}`),
  refreshMetadata: (id: string) => api.post<ArticleResponse>(`/api/articles/${id}/refresh-metadata`, {}),
  fetchMetadataPreview: (url: string) =>
    api.post<ArticleMetadataResponse>('/api/articles/metadata-preview', { url }),
};
