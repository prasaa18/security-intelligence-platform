import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  username = '';
  password = '';
  submitting = false;
  error = '';

  constructor(
    private auth: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  submit(): void {
    if (!this.username.trim() || !this.password) {
      this.error = 'Enter your secure deployment credentials.';
      return;
    }

    this.submitting = true;
    this.error = '';
    this.auth.login(this.username.trim(), this.password).subscribe(success => {
      this.submitting = false;
      if (!success) {
        this.error = 'Access denied. Check the username and password configured on the server.';
        return;
      }
      const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') || '/dashboard';
      this.router.navigateByUrl(returnUrl.startsWith('/') ? returnUrl : '/dashboard');
    });
  }
}
