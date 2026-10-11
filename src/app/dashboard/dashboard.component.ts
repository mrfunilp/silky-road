import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MarketplaceItem } from '../models/marketplace-item';
import { MarketplaceService } from '../services/marketplace.service';
import { AuthService } from '../services/auth.service';
import { apiErrorMessage } from '../core/api-error';

@Component({
  selector: 'app-dashboard',
  imports: [ReactiveFormsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent {
  private readonly marketplace = inject(MarketplaceService);
  private readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  protected readonly items = signal<MarketplaceItem[]>([]);
  protected readonly error = signal('');
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);

  private readonly currentUserId = computed(() => this.authService.currentUser()?.id ?? null);

  protected readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(120)]],
    price: [0, [Validators.required, Validators.min(0)]],
    currency: ['EUR', [Validators.required, Validators.minLength(3), Validators.maxLength(3)]],
  });

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

  protected addItem(): void {
    this.error.set('');
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { name, price, currency } = this.form.getRawValue();
    this.saving.set(true);

    this.marketplace.create({ name: name.trim(), price, currency }).subscribe({
      next: (created) => {
        this.saving.set(false);
        this.items.update((items) => [...items, created]);
        this.form.reset({ name: '', price: 0, currency: 'EUR' });
      },
      error: (error: unknown) => {
        this.saving.set(false);
        this.error.set(apiErrorMessage(error, 'Could not create the item.'));
      },
    });
  }

  protected removeItem(item: MarketplaceItem): void {
    this.error.set('');
    this.marketplace.remove(item.id).subscribe({
      next: () => this.items.update((items) => items.filter((i) => i.id !== item.id)),
      error: (error: unknown) => this.error.set(apiErrorMessage(error, 'Could not delete the item.')),
    });
  }
}
