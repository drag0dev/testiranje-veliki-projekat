import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login').then((m) => m.Login)
  },
  {
    path: 'rides/:id/rate',
    canActivate: [authGuard],
    loadComponent: () => import('./features/ratings/rate-ride/rate-ride').then((m) => m.RateRide)
  },
  { path: '**', redirectTo: 'login' }
];
