import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { RatingService } from './rating';
import { RatingResponse } from '../models/rating';

describe('RatingService', () => {
  let service: RatingService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(RatingService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('POSTs the rating payload to /rides/{id}/rating', () => {
    const response: RatingResponse = {
      id: 1,
      rideId: 42,
      driverRating: 5,
      vehicleRating: 4,
      comment: 'Great ride',
      createdAt: '2026-08-22T10:00:00'
    };

    service
      .submitRating(42, { driverRating: 5, vehicleRating: 4, comment: 'Great ride' })
      .subscribe((res) => expect(res).toEqual(response));

    const req = httpMock.expectOne(`${environment.apiUrl}/rides/42/rating`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ driverRating: 5, vehicleRating: 4, comment: 'Great ride' });
    req.flush(response);
  });

  it('propagates a server error to the caller', () => {
    let errorStatus: number | undefined;

    service.submitRating(42, { driverRating: 5, vehicleRating: 4 }).subscribe({
      next: () => fail('expected an error'),
      error: (err) => (errorStatus = err.status)
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/rides/42/rating`);
    req.flush({ message: 'Ride has already been rated' }, { status: 409, statusText: 'Conflict' });

    expect(errorStatus).toBe(409);
  });
});
