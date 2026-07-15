import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { UserRole } from '../models/user';

export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const allowedRoles = route.data['roles'] as UserRole[];

  if (!authService.isLoggedIn()) {
    router.navigate(['/login']);
    return false;
  }

  if (allowedRoles && !authService.hasRole(...allowedRoles)) {
    router.navigate(['/unauthorized']);
    return false;
  }

  return true;
};
