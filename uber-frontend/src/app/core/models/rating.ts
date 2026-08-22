export interface RatingRequest {
  driverRating: number;
  vehicleRating: number;
  comment?: string | null;
}

export interface RatingResponse {
  id: number;
  rideId: number;
  driverRating: number;
  vehicleRating: number;
  comment: string | null;
  createdAt: string;
}
