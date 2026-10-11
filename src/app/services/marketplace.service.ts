import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../core/api.config';
import { CreateMarketplaceItem, MarketplaceItem } from '../models/marketplace-item';

@Injectable({ providedIn: 'root' })
export class MarketplaceService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_BASE_URL}/items`;

  list(): Observable<MarketplaceItem[]> {
    return this.http.get<MarketplaceItem[]>(this.url);
  }

  create(item: CreateMarketplaceItem): Observable<MarketplaceItem> {
    return this.http.post<MarketplaceItem>(this.url, item);
  }

  remove(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
