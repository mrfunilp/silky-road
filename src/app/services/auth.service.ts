import { Injectable, computed, inject, signal } from '@angular/core';
import { User } from '../models/user';
import { Router } from '@angular/router';

export type RegisterResult = { ok: true } | { ok: false; error: string };

/** Handles login and registration. Uses in-memory users for now; will call the Spring API later. */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly router = inject(Router);
  // Hardcoded test user.
  private readonly _users = signal<User[]>([
    {
      id: crypto.randomUUID(),
      username: 'admin',
      email: 'admin@example.com',
      password: 'admin123',
      createdAt: new Date(),
    },
  ]);

  readonly users = this._users.asReadonly();

  register(username: string, email: string, password: string): RegisterResult {
    const users = this._users();
    if (users.some((u) => u.username.toLowerCase() === username.toLowerCase())) {
      return { ok: false, error: 'That username is already taken.' };
    }
    if (users.some((u) => u.email.toLowerCase() === email.toLowerCase())) {
      return { ok: false, error: 'An account with that email already exists.' };
    }

    const user: User = { id: crypto.randomUUID(), username, email, password, createdAt: new Date() };
    this._users.update((list) => [...list, user]);
    return { ok: true };
  }

  // The logged-in user, or null if nobody is logged in.
  private readonly _currentUser = signal<User | null>(null);
  readonly currentUser = this._currentUser.asReadonly();
  readonly isAuthenticated = computed(() => this._currentUser() !== null);

  findByCredentials(username: string, password: string): User | undefined {
    return this._users().find((u) => u.username === username && u.password === password);
  }

  /** Logs the user in if the credentials match. Returns whether it worked. */
  login(username: string, password: string): boolean {
    const user = this.findByCredentials(username, password);
    if (user) {
      this._currentUser.set(user);
    }
    return !!user;
  }

  logout(): void {
    this._currentUser.set(null);
    this.router.navigate(['/login']);
  }
}
