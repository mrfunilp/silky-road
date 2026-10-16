import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MarketplaceItem } from '../models/marketplace-item';
import { UserProfile } from '../models/user';
import { MarketplaceService } from '../services/marketplace.service';
import { UserService } from '../services/user.service';
import { AuthService } from '../services/auth.service';
import { apiErrorMessage } from '../core/api-error';
import { ItemCardComponent } from '../shared/item-card/item-card.component';

@Component({
  selector: 'app-user-profile',
  imports: [RouterLink, ItemCardComponent],
  templateUrl: './user-profile.component.html',
})
export class UserProfileComponent {
  private readonly users = inject(UserService);
  private readonly marketplace = inject(MarketplaceService);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);

  protected readonly profile = signal<UserProfile | null>(null);
  protected readonly items = signal<MarketplaceItem[]>([]);
  protected readonly error = signal('');
  protected readonly loading = signal(false);

  protected readonly isMe = computed(() => this.profile()?.id === this.authService.currentUser()?.id);

  constructor() {
    this.route.paramMap.subscribe((params) => {
      const id = params.get('id');
      if (id) {
        this.load(id);
      }
    });
  }

  private load(id: string): void {
    this.loading.set(true);
    this.error.set('');
    this.items.set([]);
    this.users.getProfile(id).subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.loading.set(false);
        this.error.set(apiErrorMessage(error, 'Could not load this user.'));
      },
    });
    this.marketplace.listByCreator(id).subscribe({
      next: (items) => this.items.set(items),
      error: (error: unknown) => this.error.set(apiErrorMessage(error, 'Could not load the listings.')),
    });
  }
}
