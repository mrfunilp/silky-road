import { UserSummary } from './user';

export type MarketplaceItemStatus = 'draft' | 'active' | 'sold' | 'archived';

export interface MarketplaceItem {
  id: string;
  name: string;
  biography: string | null;
  createdAt: string;
  updatedAt: string;
  creator: UserSummary;
  likes: number;
  views: number;
  price: number;
  currency: string;
  category: string | null;
  tags: string[];
  imageUrls: string[];
  status: MarketplaceItemStatus;
}

export interface CreateMarketplaceItem {
  name: string;
  biography?: string;
  price: number;
  currency: string;
  category?: string;
  tags?: string[];
  imageUrls?: string[];
  status?: MarketplaceItemStatus;
}
