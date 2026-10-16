import { Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { apiErrorMessage } from '../core/api-error';

function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const password = group.get('password')?.value;
  const confirm = group.get('confirmPassword')?.value;
  return password === confirm ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-registration',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './registration.html',
})
export class Registration {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);

  protected readonly form = this.fb.nonNullable.group(
    {
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(20)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required],
    },
    { validators: passwordsMatch },
  );

  protected readonly error = signal('');
  protected readonly registeredUser = signal<string | null>(null);
  protected readonly submitting = signal(false);

  protected submit(): void {
    this.error.set('');
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { username, email, password } = this.form.getRawValue();
    this.submitting.set(true);

    this.authService.register(username.trim(), email.trim(), password).subscribe({
      next: (user) => {
        this.submitting.set(false);
        this.registeredUser.set(user.username);
        this.form.reset();
      },
      error: (error: unknown) => {
        this.submitting.set(false);
        this.error.set(apiErrorMessage(error, 'Could not create the account.'));
      },
    });
  }
}
