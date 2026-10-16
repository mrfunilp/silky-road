import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../core/api.config';
import {
  CreateMarketplaceItem,
  MarketplaceItem,
  UpdateMarketplaceItem,
} from '../models/marketplace-item';
import { ItemComment } from '../models/item-comment';

@Injectable({ providedIn: 'root' })
export class MarketplaceService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_BASE_URL}/items`;

  list(): Observable<MarketplaceItem[]> {
    return this.http.get<MarketplaceItem[]>(this.url);
  }

  listByCreator(creatorId: string): Observable<MarketplaceItem[]> {
    return this.http.get<MarketplaceItem[]>(this.url, { params: { creatorId } });
  }

  get(id: string): Observable<MarketplaceItem> {
    return this.http.get<MarketplaceItem>(`${this.url}/${id}`);
  }

  create(item: CreateMarketplaceItem): Observable<MarketplaceItem> {
    return this.http.post<MarketplaceItem>(this.url, item);
  }

  update(id: string, item: UpdateMarketplaceItem): Observable<MarketplaceItem> {
    return this.http.put<MarketplaceItem>(`${this.url}/${id}`, item);
  }

  remove(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  uploadImage(id: string, file: File): Observable<MarketplaceItem> {
    const body = new FormData();
    body.append('file', file);
    return this.http.post<MarketplaceItem>(`${this.url}/${id}/images`, body);
  }

  listComments(id: string): Observable<ItemComment[]> {
    return this.http.get<ItemComment[]>(`${this.url}/${id}/comments`);
  }

  addComment(id: string, text: string): Observable<ItemComment> {
    return this.http.post<ItemComment>(`${this.url}/${id}/comments`, { text });
  }

  removeComment(id: string, commentId: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}/comments/${commentId}`);
  }

  removeImage(id: string, imageUrl: string): Observable<MarketplaceItem> {
    const name = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
    return this.http.delete<MarketplaceItem>(`${this.url}/${id}/images/${encodeURIComponent(name)}`);
  }
}
