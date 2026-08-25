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
  }
];
