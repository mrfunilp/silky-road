export interface User {
  id: string;
  username: string;
  email: string;
  createdAt: string;
}

export type UserSummary = Pick<User, 'id' | 'username'>;

export type UserProfile = Pick<User, 'id' | 'username' | 'createdAt'>;

export interface UpdateAccount {
  username: string;
  email: string;
  currentPassword: string;
  newPassword: string | null;
}

export interface AuthResponse {
  token: string;
  expiresIn: number;
  user: User;
}
