import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { ServiceModel } from '../../models/dashboard.model';

@Component({
  selector: 'app-services',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './services.component.html',
  styleUrls: ['./services.component.css']
})
export class ServicesComponent implements OnInit {
  services: ServiceModel[] = [];
  filteredServices: ServiceModel[] = [];
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
    this.loadServices();
  }

  loadServices() {
    this.loading = true;
    this.error = null;
    
    this.apiService.getAllServices().subscribe({
      next: (data) => {
        this.services = data || [];
        this.applySearch();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load services';
        this.loading = false;
        console.error('Services error:', err);
        this.services = [];
      }
    });
  }

  applySearch() {
    if (!this.searchQuery.trim()) {
      this.filteredServices = [...this.services];
    } else {
      const query = this.searchQuery.toLowerCase();
      this.filteredServices = this.services.filter(service => 
        service.serviceName.toLowerCase().includes(query) ||
        (service.teamName && service.teamName.toLowerCase().includes(query)) ||
        (service.owner && service.owner.toLowerCase().includes(query))
      );
    }
    this.filteredServices.sort((left, right) => {
      const first = String(left[this.sortField] || '');
      const second = String(right[this.sortField] || '');
      return (this.sortDirection === 'asc' ? 1 : -1) * first.localeCompare(second);
    });
  }

  onSearchChange() {
    this.applySearch();
  }

  sortBy(field: typeof this.sortField): void {
    if (this.sortField === field) this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    else { this.sortField = field; this.sortDirection = 'asc'; }
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
        this.loadServices();
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
        this.loadServices();
      },
      error: (err) => {
        this.error = 'Failed to delete service';
        console.error('Delete service error:', err);
        setTimeout(() => this.error = null, 3000);
      }
    });
  }

  getBusinessCriticalityClass(criticality: string | undefined): string {
    if (!criticality) return '';
    return `criticality-${criticality.toLowerCase()}`;
  }

  getDataSensitivityClass(sensitivity: string | undefined): string {
    if (!sensitivity) return '';
    return `sensitivity-${sensitivity.toLowerCase()}`;
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString();
  }

  // Service security functionality
  selectedServiceForSecurity: ServiceModel | null = null;
  showSecurityDetails = false;
  securitySummary: any = null;
  loadingSecuritySummary = false;

  viewServiceSecurity(service: ServiceModel) {
    this.selectedServiceForSecurity = service;
    this.showSecurityDetails = true;
    this.loadServiceSecuritySummary(service.serviceName);
  }

  loadServiceSecuritySummary(serviceName: string) {
    this.loadingSecuritySummary = true;
    this.apiService.getServiceSecuritySummary(serviceName).subscribe({
      next: (data) => {
        this.securitySummary = data;
        this.loadingSecuritySummary = false;
      },
      error: (err) => {
        console.error('Failed to load security summary:', err);
        this.loadingSecuritySummary = false;
        this.securitySummary = null;
      }
    });
  }

  closeSecurityDetails() {
    this.showSecurityDetails = false;
    this.selectedServiceForSecurity = null;
    this.securitySummary = null;
  }

  downloadServiceReport(serviceName: string) {
    this.apiService.downloadServiceSecurityReportCsv(serviceName).subscribe({
      next: (data) => {
        const blob = new Blob([data], { type: 'text/csv' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${serviceName}-security-report.csv`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        console.error('Failed to download report:', err);
        alert('Failed to download security report');
      }
    });
  }

  getPriorityCount(priority: string): number {
    if (!this.securitySummary?.priorityBreakdown) return 0;
    return this.securitySummary.priorityBreakdown[priority] || 0;
  }

  getSeverityCount(severity: string): number {
    if (!this.securitySummary?.severityBreakdown) return 0;
    return this.securitySummary.severityBreakdown[severity] || 0;
  }
}