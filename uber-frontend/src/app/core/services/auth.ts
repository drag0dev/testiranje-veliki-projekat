import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { JwtHelperService } from '@auth0/angular-jwt';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest } from '../models/auth-response';
import { User, UserRole } from '../models/user';

const TOKEN_KEY = 'auth_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private jwtHelper = new JwtHelperService();

  constructor(private http: HttpClient) {}

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
  }

  setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    return !!token && !this.jwtHelper.isTokenExpired(token);
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
}
