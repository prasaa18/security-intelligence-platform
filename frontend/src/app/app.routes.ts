import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./pages/dashboard/dashboard.component').then(c => c.DashboardComponent)
  },
  {
    path: 'remediation',
    loadComponent: () => import('./pages/remediation/remediation.component').then(c => c.RemediationComponent)
  },
  {
    path: 'remediation/:id',
    loadComponent: () => import('./pages/remediation-detail/remediation-detail.component').then(c => c.RemediationDetailComponent)
  },
  {
    path: 'scans',
    loadComponent: () => import('./pages/scans/scans.component').then(c => c.ScansComponent)
  },
  {
    path: 'scans/:id',
    loadComponent: () => import('./pages/scan-detail/scan-detail.component').then(c => c.ScanDetailComponent)
  },
  {
    path: 'reports',
    loadComponent: () => import('./pages/reports/reports.component').then(c => c.ReportsComponent)
  },
  {
    path: 'findings',
    loadComponent: () => import('./pages/findings/findings.component').then(c => c.FindingsComponent)
  },
  {
    path: 'findings/:id',
    loadComponent: () => import('./pages/finding-detail/finding-detail.component').then(c => c.FindingDetailComponent)
  },
  {
    path: 'services',
    loadComponent: () => import('./pages/services/services.component').then(c => c.ServicesComponent)
  },
  {
    path: 'services/:id/security',
    loadComponent: () => import('./pages/service-security/service-security.component').then(c => c.ServiceSecurityComponent)
  },
  {
    path: 'ai-assistant',
    loadComponent: () => import('./pages/ai-assistant/ai-assistant.component').then(c => c.AiAssistantComponent)
  }
];
