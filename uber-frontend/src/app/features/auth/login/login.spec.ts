import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { Login } from './login';
import { AuthService } from '../../../core/services/auth';

describe('Login', () => {
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  function configureTestBed(returnUrl: string | null) {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['login']);

    return TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { queryParamMap: convertToParamMap(returnUrl ? { returnUrl } : {}) }
          }
        }
      ]
    }).compileComponents();
  }

  beforeEach(async () => {
    await configureTestBed(null);
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(Login);
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('does not call AuthService.login when the form is invalid', () => {
    const fixture = TestBed.createComponent(Login);
    fixture.detectChanges();

    fixture.componentInstance.submit();

    expect(authServiceSpy.login).not.toHaveBeenCalled();
  });

  it('logs in and navigates to returnUrl on valid submit', async () => {
    await configureTestBed('/rides/42/rate');
    authServiceSpy.login.and.returnValue(of({ token: 'jwt-token' }));
    const router = TestBed.inject(Router);
    spyOn(router, 'navigateByUrl');

    const fixture = TestBed.createComponent(Login);
    fixture.detectChanges();
    fixture.componentInstance['form'].setValue({
      email: 'alice@example.com',
      password: 'password123'
    });

    fixture.componentInstance.submit();

    expect(authServiceSpy.login).toHaveBeenCalledWith({
      email: 'alice@example.com',
      password: 'password123'
    });
    expect(router.navigateByUrl).toHaveBeenCalledWith('/rides/42/rate');
  });

  it('does not navigate when there is no returnUrl to go back to', () => {
    authServiceSpy.login.and.returnValue(of({ token: 'jwt-token' }));
    const router = TestBed.inject(Router);
    spyOn(router, 'navigateByUrl');

    const fixture = TestBed.createComponent(Login);
    fixture.detectChanges();
    fixture.componentInstance['form'].setValue({
      email: 'alice@example.com',
      password: 'password123'
    });

    fixture.componentInstance.submit();

    expect(router.navigateByUrl).not.toHaveBeenCalled();
  });

  it('shows an error message when login fails', () => {
    authServiceSpy.login.and.returnValue(throwError(() => new Error('unauthorized')));

    const fixture = TestBed.createComponent(Login);
    fixture.detectChanges();
    fixture.componentInstance['form'].setValue({
      email: 'alice@example.com',
      password: 'wrong'
    });

    fixture.componentInstance.submit();
    fixture.detectChanges();

    expect(fixture.componentInstance['errorMessage']()).toBe('Invalid email or password.');
  });
});
