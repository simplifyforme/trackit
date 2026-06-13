export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
}

export interface MessageResponse {
  message: string;
}

export interface RefreshRequest {
  refreshToken: string;
}

export interface RefreshResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface UserResponse {
  id: string;
  email: string;
  enabled: boolean;
  roles: string[];
  createdAt: string;
}

export interface FieldError {
  field: string;
  message: string;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors?: FieldError[];
}

export type Result<T> =
  | { ok: true; data: T }
  | { ok: false; error: ApiError };

// ─── Todo ────────────────────────────────────────────────────────────────────

export type ImportanceLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export interface TodoResponse {
  id: string;
  title: string;
  description: string | null;
  importance: ImportanceLevel;
  deadline: string | null;
  isDone: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTodoRequest {
  title: string;
  description?: string;
  importance?: ImportanceLevel;
  deadline?: string;
}

export interface UpdateTodoRequest {
  title: string;
  description?: string;
  importance?: ImportanceLevel;
  deadline?: string | null;
  isDone?: boolean;
}

// ─── Order ───────────────────────────────────────────────────────────────────

export type OrderStatus =
  | 'PENDING' | 'CONFIRMED' | 'SHIPPED' | 'OUT_FOR_DELIVERY'
  | 'DELIVERED' | 'CANCELLED' | 'RETURNED' | 'NEEDS_REVIEW';

export type OrderSource = 'MANUAL' | 'EMAIL';

export interface OrderStatusHistoryResponse {
  id: string;
  oldStatus: OrderStatus | null;
  newStatus: OrderStatus;
  changedAt: string;
  source: OrderSource;
  note: string | null;
}

export interface OrderResponse {
  id: string;
  title: string;
  description: string | null;
  merchant: string | null;
  amount: number | null;
  currency: string | null;
  status: OrderStatus;
  source: OrderSource;
  externalRef: string | null;
  orderDate: string | null;
  createdAt: string;
  updatedAt: string;
  statusHistory: OrderStatusHistoryResponse[];
}

export interface CreateOrderRequest {
  title: string;
  description?: string;
  merchant?: string;
  amount?: number;
  currency?: string;
  externalRef?: string;
  orderDate?: string;
}

export interface UpdateOrderRequest {
  title: string;
  description?: string;
  merchant?: string;
  amount?: number;
  currency?: string;
  status?: OrderStatus;
  externalRef?: string;
  orderDate?: string;
}

export interface ChangeOrderStatusRequest {
  status: OrderStatus;
  note?: string;
}

// ─── Settings ────────────────────────────────────────────────────────────────

export interface SettingsResponse {
  openrouterApiKeyConfigured: boolean;
  openrouterModel: string;
}

export interface SettingsRequest {
  openrouterApiKey?: string;
  openrouterModel?: string;
}

// ─── Gmail ───────────────────────────────────────────────────────────────────

export interface GmailStatusResponse {
  connected: boolean;
}

export interface GmailConnectResponse {
  authorizationUrl: string;
}

// ─── Wishlist ─────────────────────────────────────────────────────────────────

export type WishlistPriority = 'LOW' | 'MEDIUM' | 'HIGH';

export interface WishlistItemResponse {
  id: string;
  name: string;
  productUrl: string;
  imageUrl: string | null;
  notes: string | null;
  priority: WishlistPriority;
  isPurchased: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateWishlistItemRequest {
  name: string;
  productUrl: string;
  notes?: string;
  priority?: WishlistPriority;
}

export interface UpdateWishlistItemRequest {
  name: string;
  productUrl: string;
  notes?: string;
  priority?: WishlistPriority;
  isPurchased?: boolean;
}
