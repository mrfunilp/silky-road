import { Component, input } from '@angular/core';
import { MarketplaceItem } from '../../models/marketplace-item';

@Component({
  selector: 'app-item-card',
  templateUrl: './item-card.component.html',
  host: { class: 'block' },
})
export class ItemCardComponent {
  readonly item = input.required<MarketplaceItem>();
  readonly badge = input<string | null>(null);
  readonly showCreator = input(true);
}
