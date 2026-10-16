import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../core/api.config';
import { UserProfile } from '../models/user';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);

  getProfile(id: string): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${API_BASE_URL}/users/${id}`);
  }
}
