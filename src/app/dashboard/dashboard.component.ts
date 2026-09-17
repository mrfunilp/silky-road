import { Component, signal } from '@angular/core';
import { MarketplaceItem } from '../models/marketplace-item';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent {

    items = signal<MarketplaceItem[]>([])


    addItem() {
        const item: MarketplaceItem = {
            id: crypto.randomUUID(),
            name: '',
            biography: '',
            createdAt: new Date(),
            updatedAt: new Date(),
            creator: {
                id: crypto.randomUUID(),
                username: ''
            },
            likes: 0,
            views: 0,
            price: 0,
            currency: '',
            category: '',
            tags: [],
            imageUrls: [],
            status: 'draft',
        };
        this.items.update((items) => [...items, item]);
    }
}
