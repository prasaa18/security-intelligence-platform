import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { ScanReport, ServiceModel } from '../../models/dashboard.model';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.css']
})
export class ReportsComponent implements OnInit {
  reports: ScanReport[] = [];
  services: ServiceModel[] = [];
  loading = true;
  error: string | null = null;
  
  // Upload form
  showUploadForm = false;
  uploadLoading = false;
  uploadError: string | null = null;
  uploadSuccess: string | null = null;
  selectedFile: File | null = null;
  selectedService = '';
  selectedEnvironment = 'PRODUCTION';
  
  environments = ['DEVELOPMENT', 'STAGING', 'PRODUCTION'];

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.loadReports();
    this.loadServices();
  }

  loadReports() {
    this.loading = true;
    this.error = null;
    
    this.apiService.getAllReports().subscribe({
      next: (data) => {
        this.reports = data.sort((a, b) => 
          new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        );
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load reports';
        this.loading = false;
        console.error('Reports error:', err);
      }
    });
  }

  loadServices() {
    this.apiService.getAllServices().subscribe({
      next: (data) => {
        this.services = data.sort((a, b) => a.serviceName.localeCompare(b.serviceName));
      },
      error: (err) => {
        console.error('Services error:', err);
      }
    });
  }

  onFileSelected(event: Event) {
    const target = event.target as HTMLInputElement;
    if (target.files && target.files.length > 0) {
      this.selectedFile = target.files[0];
      this.uploadError = null;
    }
  }

  openUploadForm() {
    this.showUploadForm = true;
    this.uploadError = null;
    this.uploadSuccess = null;
    this.selectedFile = null;
    this.selectedService = '';
    this.selectedEnvironment = 'PRODUCTION';
  }

  closeUploadForm() {
    this.showUploadForm = false;
    this.uploadError = null;
    this.uploadSuccess = null;
    this.selectedFile = null;
  }

  uploadReport() {
    if (!this.selectedFile || !this.selectedService) {
      this.uploadError = 'Please select a file and service';
      return;
    }

    this.uploadLoading = true;
    this.uploadError = null;
    this.uploadSuccess = null;

    this.apiService.uploadReport(this.selectedFile, this.selectedService, this.selectedEnvironment).subscribe({
      next: (response) => {
        this.uploadLoading = false;
        if (response.success) {
          this.uploadSuccess = `Report processed successfully! Tool: ${response.tool}, Raw Findings: ${response.rawFindings}, Unique Findings: ${response.uniqueFindings}`;
          this.loadReports(); // Refresh the reports list
          setTimeout(() => {
            this.closeUploadForm();
          }, 3000);
        } else {
          this.uploadError = response.message || 'Upload failed';
        }
      },
      error: (err) => {
        this.uploadLoading = false;
        this.uploadError = err.error?.message || 'Upload failed';
        console.error('Upload error:', err);
      }
    });
  }

  getToolClass(tool: string): string {
    return `tool-${tool.toLowerCase()}`;
  }

  getScanTypeClass(scanType: string): string {
    return `scan-type-${scanType.toLowerCase().replace('_', '-')}`;
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString();
  }

  seedSampleData() {
    this.apiService.seedSampleServices().subscribe({
      next: (response) => {
        if (response.success) {
          this.loadServices();
          // Show success message
          this.uploadSuccess = `Sample services created: ${response.servicesCreated}`;
          setTimeout(() => {
            this.uploadSuccess = null;
          }, 3000);
        }
      },
      error: (err) => {
        console.error('Seed error:', err);
        this.uploadError = 'Failed to seed sample services';
        setTimeout(() => {
          this.uploadError = null;
        }, 3000);
      }
    });
  }
}