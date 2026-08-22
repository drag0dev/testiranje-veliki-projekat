import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { App } from './app';
import { AuthService } from './core/services/auth';

describe('App', () => {
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['isLoggedIn', 'logout']);

    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([]), { provide: AuthService, useValue: authServiceSpy }]
    }).compileComponents();
  });

  it('should create the app', () => {
    authServiceSpy.isLoggedIn.and.returnValue(false);
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('does not show the log out control when logged out', () => {
    authServiceSpy.isLoggedIn.and.returnValue(false);
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('nav')).toBeNull();
  });

  it('shows a log out control that logs the user out when clicked', () => {
    authServiceSpy.isLoggedIn.and.returnValue(true);
    const router = TestBed.inject(Router);
    spyOn(router, 'navigateByUrl');

    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const logoutButton = compiled.querySelector('nav button') as HTMLButtonElement;
    expect(logoutButton).withContext('log out button should be visible when logged in').not.toBeNull();

    logoutButton.click();

    expect(authServiceSpy.logout).toHaveBeenCalled();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/login');
  });
});
