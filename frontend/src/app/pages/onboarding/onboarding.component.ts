import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-onboarding',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './onboarding.component.html',
  styleUrls: ['./onboarding.component.css']
})
export class OnboardingComponent implements OnInit {
  currentStep = 0;
  loading = true;
  hasData = false;
  skipped = false;

  steps = [
    {
      title: 'Welcome to Security Intelligence Platform',
      description: 'Transform security scanner findings into actionable remediation decisions for your team.',
      icon: '🎯',
      action: 'Get Started'
    },
    {
      title: 'Connect Your Services',
      description: 'Register your microservices and their business context to enable intelligent prioritization.',
      icon: '🔗',
      action: 'Setup Services'
    },
    {
      title: 'Upload Security Reports',
      description: 'Ingest vulnerability reports from Trivy, Snyk, or other scanners to populate your dashboard.',
      icon: '📊',
      action: 'Upload Reports'
    },
    {
      title: 'View Your Security Dashboard',
      description: 'See prioritized remediation items, AI-powered guidance, and team-specific action items.',
      icon: '📈',
      action: 'Go to Dashboard'
    }
  ];

  constructor(private router: Router, private apiService: ApiService) {}

  ngOnInit(): void {
    this.checkExistingData();
  }

  checkExistingData(): void {
    this.apiService.getAllServices().subscribe({
      next: (services) => {
        this.apiService.getAllFindings().subscribe({
          next: (findings) => {
            this.hasData = (services && services.length > 0) || (findings && findings.length > 0);
            this.loading = false;
            
            if (this.hasData) {
              // Skip onboarding if data exists
              this.router.navigate(['/dashboard']);
            }
          },
          error: () => {
            // If findings check fails, still consider it as no data
            this.hasData = (services && services.length > 0);
            this.loading = false;
            
            if (this.hasData) {
              this.router.navigate(['/dashboard']);
            }
          }
        });
      },
      error: () => {
        // If services check fails, assume no data and continue with onboarding
        this.loading = false;
      }
    });
  }

  nextStep(): void {
    if (this.currentStep < this.steps.length - 1) {
      this.currentStep++;
    } else {
      this.completeOnboarding();
    }
  }

  previousStep(): void {
    if (this.currentStep > 0) {
      this.currentStep--;
    }
  }

  goToStep(step: number): void {
    this.currentStep = step;
  }

  completeOnboarding(): void {
    this.router.navigate(['/dashboard']);
  }

  skipOnboarding(): void {
    this.skipped = true;
    this.router.navigate(['/dashboard']);
  }

  seedSampleData(): void {
    this.apiService.seedSampleServices().subscribe({
      next: (response) => {
        alert('Sample data seeded successfully! You can now explore the platform.');
        this.completeOnboarding();
      },
      error: (err) => {
        console.error('Failed to seed sample data:', err);
        alert('Failed to seed sample data. Please try again.');
      }
    });
  }

  navigateToServices(): void {
    this.router.navigate(['/services']);
  }

  navigateToReports(): void {
    this.router.navigate(['/reports']);
  }
}