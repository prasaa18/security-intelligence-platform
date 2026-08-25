import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-service-security',
  standalone: true,
  imports: [CommonModule, DatePipe, RouterLink],
  templateUrl: './service-security.component.html',
  styleUrls: ['./service-security.component.css']
})
export class ServiceSecurityComponent implements OnInit {
  service: any = null;
  scanExecutions: any[] = [];
  remediationItems: any[] = [];
  securityState: string = 'UNKNOWN';
  loading = true;
  error: string | null = null;

  constructor(
    public route: ActivatedRoute,
    public router: Router,
    private apiService: ApiService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadServiceData(id);
    } else {
      this.error = 'Invalid service ID';
      this.loading = false;
    }
  }

  loadServiceData(id: string): void {
    this.loading = true;
    
    // Load service information
    this.apiService.getServiceById(id).subscribe({
      next: (service) => {
        this.service = service;
        this.loadServiceScans(service.serviceName);
        this.loadServiceRemediation(service.serviceName);
      },
      error: (err) => {
        this.error = 'Failed to load service data';
        this.loading = false;
        console.error('Error loading service:', err);
      }
    });
  }

  loadServiceScans(serviceName: string): void {
    this.apiService.getScanExecutionsByService(serviceName).subscribe({
      next: (scans) => {
        this.scanExecutions = scans;
        this.calculateSecurityState();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading scans:', err);
        this.loading = false;
      }
    });
  }

  loadServiceRemediation(serviceName: string): void {
    this.apiService.getRemediationItemsByService(serviceName).subscribe({
      next: (items) => {
        this.remediationItems = items;
      },
      error: (err) => {
        console.error('Error loading remediation items:', err);
      }
    });
  }

  calculateSecurityState(): void {
    // Simple security state calculation based on scan freshness and open findings
    if (this.scanExecutions.length === 0) {
      this.securityState = 'UNKNOWN';
      return;
    }

    const latestScan = this.scanExecutions[0];
    const scanTime = new Date(latestScan.completedAt || latestScan.createdAt);
    const hoursSinceScan = (Date.now() - scanTime.getTime()) / (1000 * 60 * 60);
    
    const environment = this.service?.environment;
    const staleThreshold = environment === 'PRODUCTION' ? 24 : 168; // 24h for prod, 7d for dev
    
    if (hoursSinceScan > staleThreshold) {
      this.securityState = 'STALE';
      return;
    }

    const openP0 = this.remediationItems.filter(i => i.priority === 'P0' && i.status === 'OPEN').length;
    const openP1 = this.remediationItems.filter(i => i.priority === 'P1' && i.status === 'OPEN').length;
    
    if (openP0 > 0) {
      this.securityState = 'CRITICAL';
    } else if (openP1 > 0) {
      this.securityState = 'ATTENTION';
    } else {
      this.securityState = 'HEALTHY';
    }
  }

  getSecurityStateClass(state: string): string {
    switch (state) {
      case 'HEALTHY': return 'state-healthy';
      case 'ATTENTION': return 'state-attention';
      case 'CRITICAL': return 'state-critical';
      case 'STALE': return 'state-stale';
      case 'UNKNOWN': return 'state-unknown';
      default: return '';
    }
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString();
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'SUCCESS': return 'status-success';
      case 'FAILED': return 'status-failed';
      case 'PROCESSING': return 'status-processing';
      case 'RECEIVED': return 'status-received';
      default: return '';
    }
  }
}