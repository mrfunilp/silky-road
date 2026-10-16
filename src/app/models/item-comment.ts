import { UserSummary } from './user';

export interface ItemComment {
  id: string;
  author: UserSummary;
  text: string;
  createdAt: string;
}
