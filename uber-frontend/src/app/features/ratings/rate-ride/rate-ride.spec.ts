import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { Subject, of, throwError } from 'rxjs';
import { RateRide } from './rate-ride';
import { RatingService } from '../../../core/services/rating';
import { RatingResponse } from '../../../core/models/rating';

describe('RateRide', () => {
  let ratingServiceSpy: jasmine.SpyObj<RatingService>;

  const activatedRouteStub = {
    snapshot: { paramMap: convertToParamMap({ id: '42' }) }
  };

  beforeEach(async () => {
    ratingServiceSpy = jasmine.createSpyObj('RatingService', ['submitRating']);

    await TestBed.configureTestingModule({
      imports: [RateRide],
      providers: [
        { provide: RatingService, useValue: ratingServiceSpy },
        { provide: ActivatedRoute, useValue: activatedRouteStub }
      ]
    }).compileComponents();
  });

  function createComponent() {
    const fixture = TestBed.createComponent(RateRide);
    fixture.detectChanges();
    return fixture;
  }

  it('should create', () => {
    const fixture = createComponent();
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('starts with an invalid form (no ratings picked yet)', () => {
    const fixture = createComponent();
    expect(fixture.componentInstance['form'].valid).toBeFalse();
  });

  it('does not call the rating service when the form is invalid', () => {
    const fixture = createComponent();

    fixture.componentInstance.submit();

    expect(ratingServiceSpy.submitRating).not.toHaveBeenCalled();
  });

  it('marks out-of-range ratings as invalid', () => {
    const fixture = createComponent();
    const form = fixture.componentInstance['form'];

    form.controls.driverRating.setValue(6);
    form.controls.vehicleRating.setValue(0);

    expect(form.controls.driverRating.valid).toBeFalse();
    expect(form.controls.vehicleRating.valid).toBeFalse();
  });

  it('submits the exact entered driver rating, vehicle rating and comment for the route ride id', () => {
    const response: RatingResponse = {
      id: 1,
      rideId: 42,
      driverRating: 5,
      vehicleRating: 4,
      comment: 'Smooth ride',
      createdAt: '2026-08-22T10:00:00'
    };
    ratingServiceSpy.submitRating.and.returnValue(of(response));

    const fixture = createComponent();
    const form = fixture.componentInstance['form'];
    form.setValue({ driverRating: 5, vehicleRating: 4, comment: 'Smooth ride' });

    fixture.componentInstance.submit();

    expect(ratingServiceSpy.submitRating).toHaveBeenCalledWith(42, {
      driverRating: 5,
      vehicleRating: 4,
      comment: 'Smooth ride'
    });
  });

  it('shows a success state once the rating is submitted', () => {
    ratingServiceSpy.submitRating.and.returnValue(
      of({
        id: 1,
        rideId: 42,
        driverRating: 5,
        vehicleRating: 5,
        comment: null,
        createdAt: '2026-08-22T10:00:00'
      })
    );

    const fixture = createComponent();
    fixture.componentInstance['form'].setValue({ driverRating: 5, vehicleRating: 5, comment: '' });
    fixture.componentInstance.submit();
    fixture.detectChanges();

    expect(fixture.componentInstance['submitted']()).toBeTrue();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Thanks for rating your ride');
  });

  it('shows the server error message when submission fails', () => {
    ratingServiceSpy.submitRating.and.returnValue(
      throwError(() => ({ error: { message: 'Ride has already been rated' } }))
    );

    const fixture = createComponent();
    fixture.componentInstance['form'].setValue({ driverRating: 3, vehicleRating: 3, comment: '' });
    fixture.componentInstance.submit();
    fixture.detectChanges();

    expect(fixture.componentInstance['submitted']()).toBeFalse();
    expect(fixture.componentInstance['errorMessage']()).toBe('Ride has already been rated');
  });

  it('disables the submit button while the request is in flight', () => {
    const pending = new Subject<RatingResponse>();
    ratingServiceSpy.submitRating.and.returnValue(pending);

    const fixture = createComponent();
    fixture.componentInstance['form'].setValue({ driverRating: 5, vehicleRating: 4, comment: '' });
    fixture.componentInstance.submit();
    fixture.detectChanges();

    expect(fixture.componentInstance['submitting']()).toBeTrue();
    const button = (fixture.nativeElement as HTMLElement).querySelector(
      'button[type="submit"]'
    ) as HTMLButtonElement;
    expect(button.disabled).toBeTrue();
    expect(button.textContent).toContain('Submitting');

    pending.next({
      id: 1,
      rideId: 42,
      driverRating: 5,
      vehicleRating: 4,
      comment: null,
      createdAt: '2026-08-22T10:00:00'
    });
    pending.complete();
    fixture.detectChanges();

    expect(fixture.componentInstance['submitting']()).toBeFalse();
    expect(fixture.componentInstance['submitted']()).toBeTrue();
  });

  it('sends null for the comment when the field is left blank', () => {
    ratingServiceSpy.submitRating.and.returnValue(
      of({
        id: 1,
        rideId: 42,
        driverRating: 4,
        vehicleRating: 4,
        comment: null,
        createdAt: '2026-08-22T10:00:00'
      })
    );

    const fixture = createComponent();
    fixture.componentInstance['form'].setValue({ driverRating: 4, vehicleRating: 4, comment: '' });
    fixture.componentInstance.submit();

    expect(ratingServiceSpy.submitRating).toHaveBeenCalledWith(42, {
      driverRating: 4,
      vehicleRating: 4,
      comment: null
    });
  });

  it('clears a previous error message when a resubmission succeeds', () => {
    ratingServiceSpy.submitRating.and.returnValue(
      throwError(() => ({ error: { message: 'Ride has already been rated' } }))
    );

    const fixture = createComponent();
    const form = fixture.componentInstance['form'];
    form.setValue({ driverRating: 3, vehicleRating: 3, comment: '' });
    fixture.componentInstance.submit();

    expect(fixture.componentInstance['errorMessage']()).toBe('Ride has already been rated');

    ratingServiceSpy.submitRating.and.returnValue(
      of({
        id: 2,
        rideId: 42,
        driverRating: 4,
        vehicleRating: 4,
        comment: null,
        createdAt: '2026-08-22T10:00:00'
      })
    );
    fixture.componentInstance.submit();

    expect(fixture.componentInstance['errorMessage']()).toBeNull();
    expect(fixture.componentInstance['submitted']()).toBeTrue();
  });

  it('sends the values typed into the driver rating, vehicle rating and comment fields on submit', () => {
    ratingServiceSpy.submitRating.and.returnValue(
      of({
        id: 1,
        rideId: 42,
        driverRating: 5,
        vehicleRating: 3,
        comment: 'Nice driver',
        createdAt: '2026-08-22T10:00:00'
      })
    );

    const fixture = createComponent();
    const compiled = fixture.nativeElement as HTMLElement;

    const driverInput = compiled.querySelector<HTMLInputElement>('#driverRating')!;
    const vehicleInput = compiled.querySelector<HTMLInputElement>('#vehicleRating')!;
    const commentInput = compiled.querySelector<HTMLTextAreaElement>('#comment')!;

    driverInput.value = '5';
    driverInput.dispatchEvent(new Event('input'));
    vehicleInput.value = '3';
    vehicleInput.dispatchEvent(new Event('input'));
    commentInput.value = 'Nice driver';
    commentInput.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    const form = compiled.querySelector('form')!;
    form.dispatchEvent(new Event('submit'));
    fixture.detectChanges();

    expect(ratingServiceSpy.submitRating).toHaveBeenCalledWith(42, {
      driverRating: 5,
      vehicleRating: 3,
      comment: 'Nice driver'
    });
  });
});
