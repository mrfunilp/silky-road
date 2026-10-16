import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MarketplaceItem } from '../models/marketplace-item';
import { ItemComment } from '../models/item-comment';
import { MarketplaceService } from '../services/marketplace.service';
import { AuthService } from '../services/auth.service';
import { apiErrorMessage } from '../core/api-error';

@Component({
  selector: 'app-item-details',
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './item-details.component.html',
})
export class ItemDetailsComponent {
  private readonly marketplace = inject(MarketplaceService);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);

  protected readonly item = signal<MarketplaceItem | null>(null);
  protected readonly comments = signal<ItemComment[]>([]);
  protected readonly selected = signal(0);
  protected readonly error = signal('');
  protected readonly commentError = signal('');
  protected readonly loading = signal(false);
  protected readonly posting = signal(false);

  private readonly currentUserId = computed(() => this.authService.currentUser()?.id ?? null);

  protected readonly ownedByMe = computed(() => this.item()?.creator.id === this.currentUserId());

  protected readonly commentForm = this.fb.nonNullable.group({
    text: ['', [Validators.required, Validators.maxLength(500)]],
  });

  constructor() {
    this.route.paramMap.subscribe((params) => {
      const id = params.get('id');
      if (id) {
        this.load(id);
      }
    });
  }

  protected isMine(comment: ItemComment): boolean {
    return comment.author.id === this.currentUserId();
  }

  protected postComment(): void {
    const item = this.item();
    this.commentError.set('');
    if (!item || this.commentForm.invalid || !this.commentForm.controls.text.value.trim()) {
      this.commentForm.markAllAsTouched();
      return;
    }

    this.posting.set(true);
    this.marketplace.addComment(item.id, this.commentForm.controls.text.value.trim()).subscribe({
      next: (comment) => {
        this.posting.set(false);
        this.comments.update((comments) => [...comments, comment]);
        this.commentForm.reset();
      },
      error: (error: unknown) => {
        this.posting.set(false);
        this.commentError.set(apiErrorMessage(error, 'Could not post the comment.'));
      },
    });
  }

  protected removeComment(comment: ItemComment): void {
    const item = this.item();
    if (!item) {
      return;
    }

    this.commentError.set('');
    this.marketplace.removeComment(item.id, comment.id).subscribe({
      next: () => this.comments.update((comments) => comments.filter((c) => c.id !== comment.id)),
      error: (error: unknown) =>
        this.commentError.set(apiErrorMessage(error, 'Could not delete the comment.')),
    });
  }

  private load(id: string): void {
    this.loading.set(true);
    this.error.set('');
    this.commentError.set('');
    this.selected.set(0);
    this.comments.set([]);
    this.marketplace.get(id).subscribe({
      next: (item) => {
        this.item.set(item);
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.loading.set(false);
        this.error.set(apiErrorMessage(error, 'Could not load the item.'));
      },
    });
    this.marketplace.listComments(id).subscribe({
      next: (comments) => this.comments.set(comments),
      error: (error: unknown) =>
        this.commentError.set(apiErrorMessage(error, 'Could not load the comments.')),
    });
  }
}
