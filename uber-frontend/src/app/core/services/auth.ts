import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { JwtHelperService } from '@auth0/angular-jwt';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest } from '../models/auth-response';
import { User, UserRole } from '../models/user';

const TOKEN_KEY = 'auth_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly jwtHelper = new JwtHelperService();
  private readonly http = inject(HttpClient);

  // A real signal, not just a plain getter: this app runs zoneless (no zone.js), so a template
  // reading a signal is what makes it re-render on login/logout — a plain method call re-reading
  // localStorage on every call would only *happen* to update when something else (e.g. a router
  // navigation) triggers change detection for other reasons.
  private readonly loggedIn = signal(this.hasValidToken());

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/auth/login`, credentials)
      .pipe(tap(res => this.setToken(res.token)));
  }

  register(payload: FormData): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/auth/register`, payload);
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    this.loggedIn.set(false);
  }

  setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
    this.loggedIn.set(true);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    return this.loggedIn();
  }

  getCurrentUser(): User | null {
    const token = this.getToken();
    if (!token || this.jwtHelper.isTokenExpired(token)) return null;

    const decoded = this.jwtHelper.decodeToken(token);
    return {
      id: decoded.id,
      email: decoded.sub,
      firstName: decoded.firstName,
      lastName: decoded.lastName,
      role: decoded.role
    };
  }

  getRole(): UserRole | null {
    return this.getCurrentUser()?.role ?? null;
  }

  hasRole(...roles: UserRole[]): boolean {
    const role = this.getRole();
    return !!role && roles.includes(role);
  }

  private hasValidToken(): boolean {
    const token = this.getToken();
    return !!token && !this.jwtHelper.isTokenExpired(token);
  }
}
