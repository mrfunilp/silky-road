import { Routes } from '@angular/router';
import { Login } from './login/login';
import { Registration } from './registration/registration';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'register', component: Registration },
];
