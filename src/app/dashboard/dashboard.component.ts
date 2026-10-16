import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MarketplaceItem } from '../models/marketplace-item';
import { MarketplaceService } from '../services/marketplace.service';
import { AuthService } from '../services/auth.service';
import { apiErrorMessage } from '../core/api-error';
import { ItemCardComponent } from '../shared/item-card/item-card.component';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, ItemCardComponent],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent {
  private readonly marketplace = inject(MarketplaceService);
  private readonly authService = inject(AuthService);

  protected readonly items = signal<MarketplaceItem[]>([]);
  protected readonly error = signal('');
  protected readonly loading = signal(false);

  private readonly currentUserId = computed(() => this.authService.currentUser()?.id ?? null);

  constructor() {
    this.load();
  }

  protected ownedByMe(item: MarketplaceItem): boolean {
    return item.creator.id === this.currentUserId();
  }

  protected load(): void {
    this.loading.set(true);
    this.error.set('');
    this.marketplace.list().subscribe({
      next: (items) => {
        this.items.set(items);
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.loading.set(false);
        this.error.set(apiErrorMessage(error, 'Could not load items.'));
      },
    });
  }
}
