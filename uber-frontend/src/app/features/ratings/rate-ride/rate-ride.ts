import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RatingService } from '../../../core/services/rating';

@Component({
  selector: 'app-rate-ride',
  imports: [ReactiveFormsModule],
  templateUrl: './rate-ride.html'
})
export class RateRide {
  private readonly fb = inject(FormBuilder);
  private readonly ratingService = inject(RatingService);
  private readonly route = inject(ActivatedRoute);

  private readonly rideId = Number(this.route.snapshot.paramMap.get('id'));

  protected readonly submitting = signal(false);
  protected readonly submitted = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    driverRating: [0, [Validators.required, Validators.min(1), Validators.max(5)]],
    vehicleRating: [0, [Validators.required, Validators.min(1), Validators.max(5)]],
    comment: ['']
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.errorMessage.set(null);
    this.submitting.set(true);

    const { driverRating, vehicleRating, comment } = this.form.getRawValue();

    this.ratingService
      .submitRating(this.rideId, {
        driverRating,
        vehicleRating,
        comment: comment || null
      })
      .subscribe({
        next: () => {
          this.submitting.set(false);
          this.submitted.set(true);
        },
        error: (err) => {
          this.submitting.set(false);
          this.errorMessage.set(
            err?.error?.message ?? 'Could not submit your rating. Please try again.'
          );
        }
      });
  }
}
