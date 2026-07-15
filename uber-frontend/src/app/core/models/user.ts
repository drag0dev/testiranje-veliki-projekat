export type UserRole = 'PASSENGER' | 'DRIVER' | 'ADMIN';

export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
}
