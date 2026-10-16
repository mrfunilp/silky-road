import { Injectable, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { API_BASE_URL } from '../core/api.config';
import { AuthResponse, UpdateAccount, User } from '../models/user';

const STORAGE_KEY = 'silkyroad.session';

interface Session {
  token: string;
  expiresAt: number;
  user: User;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  private readonly _session = signal<Session | null>(this.restore());

  readonly currentUser = computed<User | null>(() => this._session()?.user ?? null);
  readonly isAuthenticated = computed(() => this._session() !== null);

  get token(): string | null {
    return this._session()?.token ?? null;
  }

  register(username: string, email: string, password: string): Observable<User> {
    return this.http.post<User>(`${API_BASE_URL}/users`, { username, email, password });
  }

  login(username: string, password: string): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${API_BASE_URL}/users/login`, { username, password })
      .pipe(
        tap((response) =>
          this.persist({
            token: response.token,
            expiresAt: Date.now() + response.expiresIn * 1000,
            user: response.user,
          }),
        ),
      );
  }

  updateAccount(changes: UpdateAccount): Observable<User> {
    return this.http.put<User>(`${API_BASE_URL}/users/me`, changes).pipe(
      tap((user) => {
        const session = this._session();
        if (session) {
          this.persist({ ...session, user });
        }
      }),
    );
  }

  logout(): void {
    this._session.set(null);
    if (this.isBrowser) {
      try {
        localStorage.removeItem(STORAGE_KEY);
      } catch {
      }
    }
    this.router.navigate(['/login']);
  }

  private persist(session: Session): void {
    this._session.set(session);
    if (this.isBrowser) {
      try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
      } catch {
      }
    }
  }

  private restore(): Session | null {
    if (!this.isBrowser) {
      return null;
    }
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) {
        return null;
      }
      const session = JSON.parse(raw) as Session;
      if (!session?.token || !session.user || session.expiresAt <= Date.now()) {
        localStorage.removeItem(STORAGE_KEY);
        return null;
      }
      return session;
    } catch {
      return null;
    }
  }
}
