export interface User {
  id: string;
  username: string;
  email: string;
  // Plain text only while testing without a backend. Spring will store a hash instead.
  password: string;
  createdAt: Date;
}

/** Public user info that is safe to show on items, profiles, etc. */
export type UserSummary = Pick<User, 'id' | 'username'>;
