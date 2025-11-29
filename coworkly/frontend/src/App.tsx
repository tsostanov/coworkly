import { useEffect, useMemo, useState } from 'react';
import { api } from './api/client';
import {
  AuthResponse,
  BookingResponse,
  BookingStatus,
  FreeSpaceResponse,
  Location,
  SpaceResponse,
  UserProfile,
} from './types';

const defaultRange = () => {
  const now = new Date();
  const start = new Date(now.getTime() + 60 * 60 * 1000);
  const end = new Date(start.getTime() + 2 * 60 * 60 * 1000);
  return { from: toInputValue(start), to: toInputValue(end) };
};

function toInputValue(date: Date) {
  const local = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return local.toISOString().slice(0, 16);
}

function toIso(input: string) {
  return new Date(input).toISOString();
}

function formatMoney(cents: number | null | undefined) {
  if (!cents && cents !== 0) return '—';
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(cents / 100);
}

function formatDateTime(iso: string) {
  return new Date(iso).toLocaleString(undefined, {
    hour12: false,
    hour: '2-digit',
    minute: '2-digit',
    day: '2-digit',
    month: 'short',
  });
}

function bookingTone(status: BookingStatus) {
  if (status === 'CONFIRMED' || status === 'COMPLETED') return 'good';
  if (status === 'PENDING' || status === 'DRAFT') return 'warn';
  return 'bad';
}

type AuthMode = 'login' | 'register';

function App() {
  const [locations, setLocations] = useState<Location[]>([]);
  const [selectedLocationId, setSelectedLocationId] = useState<number | null>(null);
  const [spaces, setSpaces] = useState<SpaceResponse[]>([]);
  const [freeSpaces, setFreeSpaces] = useState<FreeSpaceResponse[]>([]);
  const [bookings, setBookings] = useState<BookingResponse[]>([]);
  const [capacity, setCapacity] = useState<number | ''>('');
  const [actingUserId, setActingUserId] = useState<number | null>(null);
  const [range, setRange] = useState(defaultRange);
  const [status, setStatus] = useState<{ tone: 'success' | 'error' | 'info'; text: string } | null>(null);
  const [busy, setBusy] = useState(false);
  const [authUser, setAuthUser] = useState<UserProfile | null>(null);
  const [authMode, setAuthMode] = useState<AuthMode>('login');
  const [authForm, setAuthForm] = useState({ email: '', password: '', fullName: '' });
  const [authLoading, setAuthLoading] = useState(false);

  const selectedLocation = useMemo(
    () => locations.find((loc) => loc.id === selectedLocationId) ?? null,
    [locations, selectedLocationId],
  );

  useEffect(() => {
    bootstrapAuth();
  }, []);

  useEffect(() => {
    if (authUser) {
      loadLocations();
    } else {
      setLocations([]);
      setSpaces([]);
      setFreeSpaces([]);
      setBookings([]);
    }
  }, [authUser]);

  useEffect(() => {
    if (selectedLocationId) {
      loadSpaces(selectedLocationId);
    }
  }, [selectedLocationId]);

  useEffect(() => {
    if (authUser) {
      setActingUserId(authUser.id);
    }
  }, [authUser]);

  async function bootstrapAuth() {
    try {
      setAuthLoading(true);
      const profile = await api.me();
      setAuthUser(profile);
      setActingUserId(profile.id);
      setStatus({ tone: 'info', text: `Авторизован как ${profile.email}` });
    } catch {
      // no token or invalid
      api.setToken(null);
    } finally {
      setAuthLoading(false);
    }
  }

  async function handleAuth() {
    try {
      setAuthLoading(true);
      const payload = { email: authForm.email.trim(), password: authForm.password, fullName: authForm.fullName };
      let response: AuthResponse;
      if (authMode === 'login') {
        response = await api.login(payload);
      } else {
        response = await api.register(payload);
      }
      api.setToken(response.token);
      const profile = await api.me();
      setAuthUser(profile);
      setActingUserId(profile.id);
      setStatus({ tone: 'success', text: `Добро пожаловать, ${profile.fullName || profile.email}` });
    } catch (error) {
      showError(error);
    } finally {
      setAuthLoading(false);
    }
  }

  function logout() {
    api.setToken(null);
    setAuthUser(null);
    setActingUserId(null);
    setBookings([]);
    setStatus({ tone: 'info', text: 'Вышли из системы' });
  }

  async function loadLocations() {
    try {
      setBusy(true);
      const data = await api.getLocations();
      setLocations(data);
      if (data.length > 0) {
        setSelectedLocationId(data[0].id);
      }
    } catch (error) {
      showError(error);
    } finally {
      setBusy(false);
    }
  }

  async function loadSpaces(locationId: number) {
    try {
      setBusy(true);
      const data = await api.getSpacesByLocation(locationId);
      setSpaces(data);
    } catch (error) {
      showError(error);
    } finally {
      setBusy(false);
    }
  }

  async function findFreeSpaces() {
    if (!selectedLocationId || !range.from || !range.to) {
      setStatus({ tone: 'error', text: 'Выберите локацию и диапазон дат' });
      return;
    }
    if (!authUser) {
      setStatus({ tone: 'error', text: 'Авторизуйтесь, чтобы искать и бронировать' });
      return;
    }

    try {
      setBusy(true);
      const data = await api.getFreeSpaces({
        locationId: selectedLocationId,
        from: toIso(range.from),
        to: toIso(range.to),
        capacity,
      });
      setFreeSpaces(data);
      setStatus({ tone: 'info', text: `Свободно ${data.length} мест.` });
    } catch (error) {
      showError(error);
    } finally {
      setBusy(false);
    }
  }

  async function handleBook(spaceId: number) {
    if (!authUser || !actingUserId) {
      setStatus({ tone: 'error', text: 'Нужна авторизация' });
      return;
    }

    try {
      setBusy(true);
      const payload = {
        userId: actingUserId,
        spaceId,
        startsAt: toIso(range.from),
        endsAt: toIso(range.to),
      };
      const created = await api.createBooking(payload);
      setStatus({ tone: 'success', text: `Бронь #${created.bookingId} создана.` });
      await loadBookings();
    } catch (error) {
      showError(error);
    } finally {
      setBusy(false);
    }
  }

  async function loadBookings() {
    if (!authUser || !actingUserId) {
      setStatus({ tone: 'error', text: 'Нужна авторизация' });
      return;
    }

    try {
      setBusy(true);
      const data = await api.getBookingsForUser(actingUserId);
      setBookings(data);
      if (data.length === 0) {
        setStatus({ tone: 'info', text: 'Для этого пользователя нет броней' });
      }
    } catch (error) {
      showError(error);
    } finally {
      setBusy(false);
    }
  }

  async function confirm(bookingId: number) {
    if (!authUser || authUser.role !== 'ADMIN') {
      setStatus({ tone: 'error', text: 'Только администратор может подтверждать брони' });
      return;
    }
    try {
      setBusy(true);
      await api.confirmBooking(bookingId);
      setStatus({ tone: 'success', text: `Booking #${bookingId} confirmed.` });
      await loadBookings();
    } catch (error) {
      showError(error);
    } finally {
      setBusy(false);
    }
  }

  function showError(error: unknown) {
    const message = error instanceof Error ? error.message : 'Unknown error';
    setStatus({ tone: 'error', text: message });
    console.error(error);
  }

  const isAdmin = authUser?.role === 'ADMIN';

  return (
    <div className="app-shell">
      <div className="app">
        <header className="hero">
          <div>
            <p className="badge">Coworkly experience</p>
            <h1>Бронируйте вдохновляющие пространства с авторизацией</h1>
            <p>Роли админ/резидент, живые фильтры и быстрые действия по бронированиям.</p>
            <div className="button-row" style={{ marginTop: 16 }}>
              <button onClick={findFreeSpaces} disabled={busy || !authUser}>Найти свободные</button>
              <button className="ghost" onClick={loadBookings} disabled={busy || !authUser}>
                Мои брони
              </button>
            </div>
            {authUser && (
              <div style={{ marginTop: 12 }}>
                <span className="chip accent">{authUser.role}</span>{' '}
                <span className="chip">{authUser.email}</span>
              </div>
            )}
          </div>
          <div className="card" style={{ backdropFilter: 'blur(22px)' }}>
            <div className="section-title" style={{ marginBottom: 12 }}>
              <h2>{authMode === 'login' ? 'Войти' : 'Зарегистрироваться'}</h2>
              <span className="hint">JWT · BCrypt · роли</span>
            </div>
            <div className="stacked">
              <label>
                Email
                <input
                  type="email"
                  value={authForm.email}
                  onChange={(e) => setAuthForm((prev) => ({ ...prev, email: e.target.value }))}
                  placeholder="user@example.com"
                />
              </label>
              {authMode === 'register' && (
                <label>
                  Имя
                  <input
                    type="text"
                    value={authForm.fullName}
                    onChange={(e) => setAuthForm((prev) => ({ ...prev, fullName: e.target.value }))}
                    placeholder="Иван Петров"
                  />
                </label>
              )}
              <label>
                Пароль
                <input
                  type="password"
                  value={authForm.password}
                  onChange={(e) => setAuthForm((prev) => ({ ...prev, password: e.target.value }))}
                  placeholder="******"
                />
              </label>
              <div className="button-row" style={{ marginTop: 8 }}>
                <button onClick={handleAuth} disabled={authLoading}>
                  {authMode === 'login' ? 'Войти' : 'Создать аккаунт'}
                </button>
                <button
                  type="button"
                  className="ghost"
                  onClick={() => setAuthMode(authMode === 'login' ? 'register' : 'login')}
                >
                  {authMode === 'login' ? 'Регистрация' : 'У меня уже есть аккаунт'}
                </button>
                {authUser && (
                  <button type="button" className="secondary" onClick={logout}>
                    Выйти
                  </button>
                )}
              </div>
            </div>
          </div>
        </header>

        <section className="section">
          <div className="section-title">
            <h2>Фильтры</h2>
            <span className="hint">Локация, пользователь и окно времени</span>
          </div>
          <div className="grid" style={{ gap: 12 }}>
            <label>
              Локация
              <select
                value={selectedLocationId ?? ''}
                onChange={(e) => setSelectedLocationId(Number(e.target.value))}
              >
                {locations.map((loc) => (
                  <option key={loc.id} value={loc.id}>
                    {loc.name} — {loc.address}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Пользователь
              <input
                type="number"
                min={1}
                value={actingUserId ?? ''}
                onChange={(e) => setActingUserId(Number(e.target.value) || 0)}
                placeholder="ID"
                disabled={!isAdmin}
              />
              {!isAdmin && <span className="hint">Ваш ID берётся из токена</span>}
            </label>
            <label>
              От
              <input
                type="datetime-local"
                value={range.from}
                onChange={(e) => setRange((prev) => ({ ...prev, from: e.target.value }))}
              />
            </label>
            <label>
              До
              <input
                type="datetime-local"
                value={range.to}
                onChange={(e) => setRange((prev) => ({ ...prev, to: e.target.value }))}
              />
            </label>
            <label>
              Вместимость (опц.)
              <input
                type="number"
                min={1}
                value={capacity}
                onChange={(e) => {
                  const value = e.target.value;
                  setCapacity(value === '' ? '' : Number(value));
                }}
                placeholder="4"
              />
            </label>
          </div>
        </section>

        <section className="section">
          <div className="section-title">
            <h2>Локации</h2>
            <span className="hint">Выберите — появятся её пространства</span>
          </div>
          <div className="grid">
            {locations.map((loc) => (
              <div key={loc.id} className="card">
                <div className="flex" style={{ justifyContent: 'space-between' }}>
                  <div>
                    <div style={{ fontWeight: 600 }}>{loc.name}</div>
                    <div className="text-muted">{loc.address}</div>
                  </div>
                  {selectedLocationId === loc.id ? (
                    <span className="chip accent">Active</span>
                  ) : (
                    <button className="ghost" onClick={() => setSelectedLocationId(loc.id)}>
                      Сделать активной
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        </section>

        <section className="section">
          <div className="section-title">
            <h2>Пространства</h2>
            <span className="hint">Только активные в локации</span>
          </div>
          <div className="grid">
            {spaces.map((space) => (
              <div key={space.id} className="card">
                <div className="flex" style={{ justifyContent: 'space-between' }}>
                  <div>
                    <div style={{ fontWeight: 600 }}>{space.name}</div>
                    <div className="text-muted">{space.locationName}</div>
                  </div>
                  <span className="chip secondary">{space.type}</span>
                </div>
                <div className="flex" style={{ marginTop: 10 }}>
                  <span className="chip">Capacity {space.capacity}</span>
                  <span className="chip">Tariff {space.tariffPlanId ?? '—'}</span>
                  <span className="chip">{space.active ? 'Active' : 'Disabled'}</span>
                </div>
              </div>
            ))}
            {spaces.length === 0 && <div className="text-muted">Нет пространств для этой локации</div>}
          </div>
        </section>

        <section className="section">
          <div className="section-title">
            <h2>Свободно на выбранное окно</h2>
            <span className="hint">Результат поиска</span>
          </div>
          <div className="grid">
            {freeSpaces.map((space) => (
              <div key={space.spaceId} className="card">
                <div className="flex" style={{ justifyContent: 'space-between' }}>
                  <div>
                    <div style={{ fontWeight: 600 }}>{space.spaceName}</div>
                    <div className="text-muted">Вместимость {space.capacity ?? '—'}</div>
                  </div>
                  <button onClick={() => handleBook(space.spaceId)} disabled={busy || !authUser}>
                    Забронировать
                  </button>
                </div>
              </div>
            ))}
            {freeSpaces.length === 0 && (
              <div className="text-muted">Нажмите «Найти свободные», чтобы увидеть результат</div>
            )}
          </div>
        </section>

        <section className="section">
          <div className="section-title">
            <h2>Мои бронирования</h2>
            <span className="hint">Для текущего пользователя (или выбранного админом)</span>
          </div>
          <div className="list">
            {bookings.map((booking) => (
              <div key={booking.id} className="booking-item">
                <div className="flex" style={{ justifyContent: 'space-between' }}>
                  <div className="flex">
                    <span className={`tag ${bookingTone(booking.status)}`}>{booking.status}</span>
                    <span className="chip">{booking.spaceName}</span>
                  </div>
                  <div className="flex" style={{ gap: 8 }}>
                    <span className="chip">{formatMoney(booking.totalCents)}</span>
                    {isAdmin && booking.status !== 'CONFIRMED' && booking.status !== 'CANCELED' && (
                      <button className="ghost" onClick={() => confirm(booking.id)} disabled={busy}>
                        Подтвердить
                      </button>
                    )}
                  </div>
                </div>
                <div className="flex" style={{ marginTop: 10, justifyContent: 'space-between' }}>
                  <div className="text-muted">
                    {formatDateTime(booking.startsAt)} → {formatDateTime(booking.endsAt)}
                  </div>
                  <div className="text-muted">#{booking.id}</div>
                </div>
              </div>
            ))}
            {bookings.length === 0 && <div className="text-muted">Пока нет броней</div>}
          </div>
        </section>

        {status && (
          <div
            className={`section status-line ${
              status.tone === 'success' ? 'success' : status.tone === 'error' ? 'danger' : ''
            }`}
          >
            {status.text}
          </div>
        )}
      </div>
    </div>
  );
}

export default App;
