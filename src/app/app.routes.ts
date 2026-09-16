import { Routes } from '@angular/router';
import { Login } from './login/login';
import { Registration } from './registration/registration';
import { authGuard } from './Guards/auth.guard';
import { ShellComponent } from './shell/shell.component';
import { DashboardComponent } from './dashboard/dashboard.component';

export const routes: Routes = [
  //public pages
  { path: 'login', component: Login },
  { path: 'register', component: Registration },
  //protected routes
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
    ],
  },
  //unknown urls
  { path: '**', redirectTo: '' },
];
