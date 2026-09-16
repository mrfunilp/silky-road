import { UserSummary } from './user';

export type MarketplaceItemStatus = 'draft' | 'active' | 'sold' | 'archived';

export interface MarketplaceItem {
  id: string;
  name: string;
  biography: string;
  createdAt: Date;
  updatedAt: Date;
  creator: UserSummary;
  likes: number;
  views: number;
  price: number;
  currency: string;
  category: string;
  tags: string[];
  imageUrls: string[];
  status: MarketplaceItemStatus;
}
