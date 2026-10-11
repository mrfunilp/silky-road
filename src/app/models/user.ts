export interface User {
  id: string;
  username: string;
  email: string;
  createdAt: string;
}

export type UserSummary = Pick<User, 'id' | 'username'>;

export interface AuthResponse {
  token: string;
  expiresIn: number;
  user: User;
}
