import {
  AuthResponse,
  BookingResponse,
  CreateBookingRequest,
  CreateBookingResponse,
  FreeSpaceResponse,
  Location,
  SpaceResponse,
  UserProfile,
  WalkInBookingRequest,
  WalkInBookingResponse,
  ReportResponse,
  Penalty,
} from '../types';

const API_BASE = import.meta.env.VITE_API_BASE ?? '/api';
let authToken: string | null = localStorage.getItem('coworkly_token');

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE';

async function request<T>(path: string, method: HttpMethod = 'GET', body?: unknown): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(authToken ? { Authorization: `Bearer ${authToken}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });

  if (!res.ok) {
    const message = await safeReadError(res);
    throw new Error(message);
  }

  if (res.status === 204) {
    return undefined as T;
  }

  return res.json() as Promise<T>;
}

async function safeReadError(res: Response) {
  try {
    const text = await res.text();
    return text || `Request failed with status ${res.status}`;
  } catch (e) {
    return `Request failed with status ${res.status}`;
  }
}

export const api = {
  setToken: (token: string | null) => {
    authToken = token;
    if (token) {
      localStorage.setItem('coworkly_token', token);
    } else {
      localStorage.removeItem('coworkly_token');
    }
  },
  register: (payload: { email: string; password: string; fullName: string }) =>
    request<AuthResponse>('/auth/register', 'POST', payload),
  login: (payload: { email: string; password: string }) =>
    request<AuthResponse>('/auth/login', 'POST', payload),
  me: () => request<UserProfile>('/auth/me'),
  getLocations: () => request<Location[]>('/locations'),
  getSpaces: () => request<SpaceResponse[]>('/spaces'),
  getSpacesByLocation: (locationId: number) => request<SpaceResponse[]>(`/spaces/location/${locationId}`),
  getFreeSpaces: (params: { locationId: number; from: string; to: string; capacity?: number | '' }) => {
    const query = new URLSearchParams({
      locationId: String(params.locationId),
      from: params.from,
      to: params.to,
    });
    if (params.capacity) {
      query.set('capacity', String(params.capacity));
    }
    return request<FreeSpaceResponse[]>(`/spaces/free?${query.toString()}`);
  },
  getBookingsForUser: (userId: number) => request<BookingResponse[]>(`/bookings/user/${userId}`),
  createBooking: (payload: CreateBookingRequest) => request<CreateBookingResponse>('/bookings', 'POST', payload),
  confirmBooking: (bookingId: number) => request<void>(`/bookings/${bookingId}/confirm`, 'POST'),
  adminCreateWalkIn: (payload: WalkInBookingRequest) =>
    request<WalkInBookingResponse>('/admin/walkin', 'POST', payload),
  adminReport: (payload: { from: string; to: string; locationId?: number | null }) =>
    request<ReportResponse>('/admin/reports', 'POST', payload),
  adminPenalties: {
    list: (params?: { userId?: number; activeOnly?: boolean }) => {
      const query = new URLSearchParams();
      if (params?.userId) query.set('userId', String(params.userId));
      if (params?.activeOnly) query.set('activeOnly', 'true');
      const suffix = query.toString() ? `?${query.toString()}` : '';
      return request<Penalty[]>(`/admin/penalties${suffix}`);
    },
    create: (payload: {
      userId: number;
      type: string;
      reason?: string;
      limitMinutes?: number;
      amountCents?: number;
      expiresAt?: string;
    }) => request<Penalty>('/admin/penalties', 'POST', payload),
    revoke: (id: number) => request<void>(`/admin/penalties/${id}`, 'DELETE'),
  },
  myPenalties: () => request<Penalty[]>('/penalties/me'),
};
