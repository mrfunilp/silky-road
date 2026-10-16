import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Observable, of, switchMap, tap } from 'rxjs';
import {
  CreateMarketplaceItem,
  MarketplaceItem,
  MarketplaceItemStatus,
} from '../models/marketplace-item';
import { MarketplaceService } from '../services/marketplace.service';
import { AuthService } from '../services/auth.service';
import { apiErrorMessage } from '../core/api-error';
import { ItemCardComponent } from '../shared/item-card/item-card.component';
import { IMAGE_ACCEPT, MAX_IMAGES_PER_ITEM, imageFileError } from '../core/image-upload';

const EMPTY_FORM = {
  name: '',
  biography: '',
  price: 0,
  currency: 'EUR',
  category: '',
  status: 'draft' as MarketplaceItemStatus,
};

@Component({
  selector: 'app-my-items',
  imports: [ReactiveFormsModule, RouterLink, ItemCardComponent],
  templateUrl: './my-items.component.html',
})
export class MyItemsComponent {
  private readonly marketplace = inject(MarketplaceService);
  private readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  protected readonly statuses: MarketplaceItemStatus[] = ['draft', 'active', 'sold', 'archived'];
  protected readonly imageAccept = IMAGE_ACCEPT;
  protected readonly maxImages = MAX_IMAGES_PER_ITEM;

  protected readonly items = signal<MarketplaceItem[]>([]);
  protected readonly editing = signal<MarketplaceItem | null>(null);
  protected readonly pendingFiles = signal<File[]>([]);
  protected readonly error = signal('');
  protected readonly fileError = signal('');
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);

  protected readonly slotsLeft = computed(
    () => MAX_IMAGES_PER_ITEM - (this.editing()?.imageUrls.length ?? 0) - this.pendingFiles().length,
  );

  protected readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(120)]],
    biography: ['', Validators.maxLength(2000)],
    price: [0, [Validators.required, Validators.min(0)]],
    currency: ['EUR', [Validators.required, Validators.minLength(3), Validators.maxLength(3)]],
    category: ['', Validators.maxLength(60)],
    status: ['draft' as MarketplaceItemStatus, Validators.required],
  });

  constructor() {
    this.load();
  }

  protected load(): void {
    const userId = this.authService.currentUser()?.id;
    if (!userId) {
      return;
    }

    this.loading.set(true);
    this.error.set('');
    this.marketplace.listByCreator(userId).subscribe({
      next: (items) => {
        this.items.set(items);
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.loading.set(false);
        this.error.set(apiErrorMessage(error, 'Could not load your items.'));
      },
    });
  }

  protected startEdit(item: MarketplaceItem): void {
    this.error.set('');
    this.fileError.set('');
    this.pendingFiles.set([]);
    this.editing.set(item);
    this.form.reset({
      name: item.name,
      biography: item.biography ?? '',
      price: item.price,
      currency: item.currency,
      category: item.category ?? '',
      status: item.status,
    });
  }

  protected cancelEdit(): void {
    this.editing.set(null);
    this.pendingFiles.set([]);
    this.fileError.set('');
    this.form.reset(EMPTY_FORM);
  }

  protected onFilesSelected(input: HTMLInputElement): void {
    const files = Array.from(input.files ?? []);
    input.value = '';

    const problems: string[] = [];
    const accepted: File[] = [];
    for (const file of files) {
      const problem = imageFileError(file);
      if (problem) {
        problems.push(problem);
      } else {
        accepted.push(file);
      }
    }

    const room = Math.max(0, this.slotsLeft());
    if (accepted.length > room) {
      problems.push(`An item can have at most ${MAX_IMAGES_PER_ITEM} pictures.`);
    }

    this.pendingFiles.update((pending) => [...pending, ...accepted.slice(0, room)]);
    this.fileError.set(problems.join(' '));
  }

  protected removePending(file: File): void {
    this.pendingFiles.update((pending) => pending.filter((f) => f !== file));
  }

  protected removeImage(imageUrl: string): void {
    const item = this.editing();
    if (!item) {
      return;
    }

    this.error.set('');
    this.marketplace.removeImage(item.id, imageUrl).subscribe({
      next: (updated) => this.replaceItem(updated),
      error: (error: unknown) => this.error.set(apiErrorMessage(error, 'Could not remove the picture.')),
    });
  }

  protected save(): void {
    this.error.set('');
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const body: CreateMarketplaceItem = {
      name: value.name.trim(),
      biography: value.biography.trim() || undefined,
      price: value.price,
      currency: value.currency.trim(),
      category: value.category.trim() || undefined,
      status: value.status,
    };

    const editing = this.editing();
    const request = editing
      ? this.marketplace.update(editing.id, { ...body, status: value.status })
      : this.marketplace.create(body);

    let saved: MarketplaceItem | null = null;
    this.saving.set(true);
    request
      .pipe(
        tap((item) => (saved = item)),
        switchMap((item) => this.uploadPending(item)),
      )
      .subscribe({
        next: (item) => {
          this.saving.set(false);
          this.items.update((items) =>
            items.some((i) => i.id === item.id)
              ? items.map((i) => (i.id === item.id ? item : i))
              : [...items, item],
          );
          this.cancelEdit();
        },
        error: (error: unknown) => {
          this.saving.set(false);
          if (!saved) {
            this.error.set(apiErrorMessage(error, 'Could not save the item.'));
            return;
          }
          this.error.set(
            'The item was saved, but a picture could not be uploaded: ' +
              apiErrorMessage(error, 'unknown error.'),
          );
          this.marketplace.get(saved.id).subscribe((fresh) => this.startEdit(fresh));
          this.load();
        },
      });
  }

  protected removeItem(item: MarketplaceItem): void {
    if (!confirm(`Delete "${item.name}"?`)) {
      return;
    }

    this.error.set('');
    this.marketplace.remove(item.id).subscribe({
      next: () => {
        this.items.update((items) => items.filter((i) => i.id !== item.id));
        if (this.editing()?.id === item.id) {
          this.cancelEdit();
        }
      },
      error: (error: unknown) => this.error.set(apiErrorMessage(error, 'Could not delete the item.')),
    });
  }

  private uploadPending(item: MarketplaceItem): Observable<MarketplaceItem> {
    return this.pendingFiles().reduce<Observable<MarketplaceItem>>(
      (chain, file) => chain.pipe(switchMap(() => this.marketplace.uploadImage(item.id, file))),
      of(item),
    );
  }

  private replaceItem(updated: MarketplaceItem): void {
    this.items.update((items) => items.map((i) => (i.id === updated.id ? updated : i)));
    if (this.editing()?.id === updated.id) {
      this.editing.set(updated);
    }
  }
}
