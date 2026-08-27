import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ApiService } from '../../services/api.service';
import { ServiceModel, ScanExecution, SecurityFinding } from '../../models/dashboard.model';

interface ServiceRow {
  service: ServiceModel;
  p0Count: number;
  p1Count: number;
  totalFindings: number;
  latestScanTime?: string;
  securityState: 'CRITICAL' | 'ATTENTION' | 'HEALTHY' | 'STALE' | 'UNKNOWN';
}

@Component({
  selector: 'app-services',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './services.component.html',
  styleUrls: ['./services.component.css']
})
export class ServicesComponent implements OnInit {
  services: ServiceModel[] = [];
  serviceRows: ServiceRow[] = [];
  filteredServiceRows: ServiceRow[] = [];
  loading = true;
  error: string | null = null;
  
  // Search
  searchQuery = '';
  
  // Form modal
  showForm = false;
  formLoading = false;
  formError: string | null = null;
  isEditMode = false;
  currentService: Partial<ServiceModel> = {};
  
  environments = ['DEVELOPMENT', 'STAGING', 'PRODUCTION'];
  businessCriticalities = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
  dataSensitivities = ['PUBLIC', 'INTERNAL', 'CONFIDENTIAL', 'SENSITIVE'];
  sortField: 'serviceName' | 'environment' | 'businessCriticality' | 'teamName' = 'serviceName';
  sortDirection: 'asc' | 'desc' = 'asc';

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.loading = true;
    this.error = null;

    forkJoin({
      services: this.apiService.getAllServices(),
      scans: this.apiService.getAllScanExecutions(),
      findings: this.apiService.getAllFindings()
    }).subscribe({
      next: ({ services, scans, findings }) => {
        this.services = services || [];
        this.buildServiceRows(this.services, scans || [], findings || []);
        this.applySearch();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load services data';
        this.loading = false;
        console.error('Services error:', err);
      }
    });
  }

  private buildServiceRows(services: ServiceModel[], scans: ScanExecution[], findings: SecurityFinding[]) {
    this.serviceRows = services.map(svc => {
      const svcFindings = findings.filter(f => f.serviceName === svc.serviceName && f.status === 'OPEN');
      const p0 = svcFindings.filter(f => f.priority === 'P0').length;
      const p1 = svcFindings.filter(f => f.priority === 'P1').length;
      const total = svcFindings.length;

      const svcScans = scans.filter(s => s.serviceName === svc.serviceName)
        .sort((a, b) => new Date(b.completedAt || b.receivedAt || b.createdAt).getTime() - new Date(a.completedAt || a.receivedAt || a.createdAt).getTime());
      
      const latestScan = svcScans[0];
      const latestScanTime = latestScan ? (latestScan.completedAt || latestScan.receivedAt || latestScan.createdAt) : undefined;

      let securityState: 'CRITICAL' | 'ATTENTION' | 'HEALTHY' | 'STALE' | 'UNKNOWN' = 'UNKNOWN';
      if (!latestScan) {
        securityState = 'UNKNOWN';
      } else {
        const scanAgeHours = (Date.now() - new Date(latestScanTime!).getTime()) / (1000 * 60 * 60);
        const staleLimit = svc.environment === 'PRODUCTION' ? 24 : 168; // 24h prod, 7d dev

        if (p0 > 0) {
          securityState = 'CRITICAL';
        } else if (p1 > 0) {
          securityState = 'ATTENTION';
        } else if (scanAgeHours > staleLimit) {
          securityState = 'STALE';
        } else {
          securityState = 'HEALTHY';
        }
      }

      return {
        service: svc,
        p0Count: p0,
        p1Count: p1,
        totalFindings: total,
        latestScanTime,
        securityState
      };
    });
  }

  applySearch() {
    if (!this.searchQuery.trim()) {
      this.filteredServiceRows = [...this.serviceRows];
    } else {
      const q = this.searchQuery.toLowerCase();
      this.filteredServiceRows = this.serviceRows.filter(r => 
        r.service.serviceName.toLowerCase().includes(q) ||
        (r.service.teamName && r.service.teamName.toLowerCase().includes(q)) ||
        (r.service.owner && r.service.owner.toLowerCase().includes(q)) ||
        r.service.environment.toLowerCase().includes(q)
      );
    }

    this.filteredServiceRows.sort((a, b) => {
      const valA = String(a.service[this.sortField] || '');
      const valB = String(b.service[this.sortField] || '');
      return (this.sortDirection === 'asc' ? 1 : -1) * valA.localeCompare(valB);
    });
  }

  onSearchChange() {
    this.applySearch();
  }

  sortBy(field: typeof this.sortField): void {
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = 'asc';
    }
    this.applySearch();
  }

  openAddForm() {
    this.isEditMode = false;
    this.currentService = {
      environment: 'PRODUCTION',
      businessCriticality: 'MEDIUM',
      dataSensitivity: 'INTERNAL',
      internetExposed: false
    };
    this.showForm = true;
    this.formError = null;
  }

  openEditForm(service: ServiceModel) {
    this.isEditMode = true;
    this.currentService = { ...service };
    this.showForm = true;
    this.formError = null;
  }

  closeForm() {
    this.showForm = false;
    this.formError = null;
    this.currentService = {};
  }

  saveService() {
    if (!this.currentService.serviceName?.trim()) {
      this.formError = 'Service name is required';
      return;
    }

    this.formLoading = true;
    this.formError = null;

    const serviceData = {
      ...this.currentService,
      serviceName: this.currentService.serviceName!.trim()
    };

    const request = this.isEditMode 
      ? this.apiService.updateService(this.currentService.id!, serviceData)
      : this.apiService.createService(serviceData);

    request.subscribe({
      next: () => {
        this.formLoading = false;
        this.closeForm();
        this.loadData();
      },
      error: (err) => {
        this.formLoading = false;
        this.formError = err.error?.message || 'Failed to save service';
        console.error('Save service error:', err);
      }
    });
  }

  deleteService(service: ServiceModel) {
    if (!confirm(`Are you sure you want to delete the service "${service.serviceName}"?`)) {
      return;
    }

    this.apiService.deleteService(service.id).subscribe({
      next: () => {
        this.loadData();
      },
      error: (err) => {
        this.error = 'Failed to delete service';
        console.error('Delete service error:', err);
        setTimeout(() => this.error = null, 3000);
      }
    });
  }

  downloadServiceCsv(serviceName: string) {
    this.apiService.getFindingsByService(serviceName).subscribe({
      next: (findings) => {
        const headers = ['CVE', 'Title', 'Service', 'Tool', 'Severity', 'CVSS', 'Risk Score', 'Priority', 'Status', 'Package', 'Installed Version', 'Fixed Version'];
        const rows = (findings || []).map(f => [
          f.cve || '',
          `"${(f.title || '').replace(/"/g, '""')}"`,
          f.serviceName || '',
          f.tool || '',
          f.severity || '',
          f.cvssScore || '',
          f.riskScore || '',
          f.priority || '',
          f.status || '',
          f.packageName || '',
          f.installedVersion || '',
          f.fixedVersion || ''
        ].join(','));
        const csv = [headers.join(','), ...rows].join('\n');
        const blob = new Blob([csv], { type: 'text/csv' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `service-${serviceName}-findings.csv`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => {
        alert('Could not download report for ' + serviceName);
      }
    });
  }

  getStateBadgeClass(state: string): string {
    switch (state) {
      case 'CRITICAL':  return 'state-critical';
      case 'ATTENTION': return 'state-attention';
      case 'HEALTHY':   return 'state-healthy';
      case 'STALE':     return 'state-stale';
      default:          return 'state-unknown';
    }
  }

  getEnvBadgeClass(env: string): string {
    switch (env) {
      case 'PRODUCTION':  return 'badge-env-production';
      case 'STAGING':     return 'badge-env-staging';
      case 'DEVELOPMENT': return 'badge-env-development';
      default:            return '';
    }
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return 'Never scanned';
    return new Date(dateString).toLocaleDateString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
  }
}