import { Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { AuthService } from '../services/auth.service';
import { apiErrorMessage } from '../core/api-error';

function newPasswordsMatch(group: AbstractControl): ValidationErrors | null {
  const password = group.get('newPassword')?.value;
  const confirm = group.get('confirmPassword')?.value;
  return password === confirm ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-account',
  imports: [ReactiveFormsModule],
  templateUrl: './account.component.html',
})
export class AccountComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);

  protected readonly error = signal('');
  protected readonly success = signal('');
  protected readonly saving = signal(false);

  protected readonly form = this.fb.nonNullable.group(
    {
      username: [
        this.authService.currentUser()?.username ?? '',
        [Validators.required, Validators.minLength(3), Validators.maxLength(20)],
      ],
      email: [this.authService.currentUser()?.email ?? '', [Validators.required, Validators.email]],
      newPassword: ['', Validators.minLength(8)],
      confirmPassword: [''],
      currentPassword: ['', Validators.required],
    },
    { validators: newPasswordsMatch },
  );

  protected submit(): void {
    this.error.set('');
    this.success.set('');
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { username, email, newPassword, currentPassword } = this.form.getRawValue();
    this.saving.set(true);

    this.authService
      .updateAccount({
        username: username.trim(),
        email: email.trim(),
        currentPassword,
        newPassword: newPassword || null,
      })
      .subscribe({
        next: (user) => {
          this.saving.set(false);
          this.success.set('Your account has been updated.');
          this.form.reset({
            username: user.username,
            email: user.email,
            newPassword: '',
            confirmPassword: '',
            currentPassword: '',
          });
        },
        error: (error: unknown) => {
          this.saving.set(false);
          this.error.set(apiErrorMessage(error, 'Could not update your account.'));
        },
      });
  }
}
