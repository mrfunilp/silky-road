import { Routes } from '@angular/router';
import { Login } from './login/login';
import { Registration } from './registration/registration';
import { authGuard } from './Guards/auth.guard';
import { ShellComponent } from './shell/shell.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { MyItemsComponent } from './my-items/my-items.component';
import { ItemDetailsComponent } from './item-details/item-details.component';
import { AccountComponent } from './account/account.component';
import { UserProfileComponent } from './user-profile/user-profile.component';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'register', component: Registration },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'my-items', component: MyItemsComponent },
      { path: 'items/:id', component: ItemDetailsComponent },
      { path: 'account', component: AccountComponent },
      { path: 'users/:id', component: UserProfileComponent },
    ],
  },
  { path: '**', redirectTo: '' },
];
