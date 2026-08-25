import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { ServiceModel } from '../../models/dashboard.model';

@Component({
  selector: 'app-services',
  standalone: true,
  imports: [CommonModule, FormsModule],
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

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.loadServices();
  }

  loadServices() {
    this.loading = true;
    this.error = null;
    
    this.apiService.getAllServices().subscribe({
      next: (data) => {
        this.services = data.sort((a, b) => a.serviceName.localeCompare(b.serviceName));
        this.applySearch();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load services';
        this.loading = false;
        console.error('Services error:', err);
      }
    });
  }

  applySearch() {
    if (!this.searchQuery.trim()) {
      this.filteredServices = [...this.services];
      return;
    }

    const query = this.searchQuery.toLowerCase();
    this.filteredServices = this.services.filter(service => 
      service.serviceName.toLowerCase().includes(query) ||
      (service.teamName && service.teamName.toLowerCase().includes(query)) ||
      (service.owner && service.owner.toLowerCase().includes(query))
    );
  }

  onSearchChange() {
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
}