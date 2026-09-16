import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);

  protected readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  protected readonly error = signal('');
  protected readonly loggedInUser = signal<string | null>(null);
  protected readonly showPassword = signal(false);

  protected submit(): void {
    this.error.set('');
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { username, password } = this.form.getRawValue();
    const user = this.authService.findByCredentials(username, password);
    if (user) {
      this.loggedInUser.set(user.username);
    } else {
      this.error.set('Invalid username or password.');
    }
  }

  protected logout(): void {
    this.loggedInUser.set(null);
    this.form.reset();
  }
}
