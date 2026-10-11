import { HttpErrorResponse } from '@angular/common/http';

interface ApiError {
  status: number;
  error: string;
  message: string;
  fieldErrors: Record<string, string> | null;
}

export function apiErrorMessage(error: unknown, fallback = 'Something went wrong.'): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return 'Could not reach the server. Is the backend running?';
    }
    const body = error.error as ApiError | null;
    if (body?.message) {
      return body.message;
    }
  }
  return fallback;
}
