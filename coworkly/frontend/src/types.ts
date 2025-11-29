export type Location = {
  id: number;
  name: string;
  address: string;
};

export type SpaceType = 'OPEN_DESK' | 'MEETING_ROOM';

export type SpaceResponse = {
  id: number;
  locationId: number;
  locationName: string;
  locationAddress: string;
  name: string;
  capacity: number;
  type: SpaceType;
  tariffPlanId: number | null;
  active: boolean;
};

export type FreeSpaceResponse = {
  spaceId: number;
  spaceName: string;
  capacity: number | null;
};

export type BookingStatus =
  | 'DRAFT'
  | 'PENDING'
  | 'CONFIRMED'
  | 'CANCELED'
  | 'COMPLETED'
  | 'NO_SHOW';

export type BookingResponse = {
  id: number;
  userId: number;
  spaceId: number;
  spaceName: string;
  spaceType: SpaceType;
  locationId: number;
  locationName: string;
  startsAt: string;
  endsAt: string;
  status: BookingStatus;
  totalCents: number;
  createdAt: string;
};

export type CreateBookingRequest = {
  userId: number;
  spaceId: number;
  startsAt: string;
  endsAt: string;
};

export type CreateBookingResponse = {
  bookingId: number;
};

export type AuthResponse = {
  token: string;
  userId: number;
  email: string;
  fullName: string;
  role: string;
};

export type UserProfile = {
  id: number;
  email: string;
  fullName: string;
  role: string;
  status: string;
};

export type WalkInBookingRequest = {
  email: string;
  fullName: string;
  spaceId: number;
  startsAt: string;
  endsAt: string;
};

export type WalkInBookingResponse = {
  userId: number;
  bookingId: number;
  tempPassword: string | null;
  existingUser: boolean;
};
