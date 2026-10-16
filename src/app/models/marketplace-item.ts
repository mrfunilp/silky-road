import { UserSummary } from './user';

export type MarketplaceItemStatus = 'draft' | 'active' | 'sold' | 'archived';

export interface MarketplaceItem {
  id: string;
  name: string;
  biography: string | null;
  createdAt: string;
  creator: UserSummary;
  price: number;
  currency: string;
  category: string | null;
  imageUrls: string[];
  status: MarketplaceItemStatus;
}

export interface CreateMarketplaceItem {
  name: string;
  biography?: string;
  price: number;
  currency: string;
  category?: string;
  status?: MarketplaceItemStatus;
}

export interface UpdateMarketplaceItem extends CreateMarketplaceItem {
  status: MarketplaceItemStatus;
}
