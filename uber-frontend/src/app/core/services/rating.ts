import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { RatingRequest, RatingResponse } from '../models/rating';

@Injectable({ providedIn: 'root' })
export class RatingService {
  private readonly http = inject(HttpClient);

  submitRating(rideId: number, payload: RatingRequest): Observable<RatingResponse> {
    return this.http.post<RatingResponse>(`${environment.apiUrl}/rides/${rideId}/rating`, payload);
  }
}
