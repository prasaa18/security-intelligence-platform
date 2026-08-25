import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { ScanExecution } from '../../models/dashboard.model';

@Component({
  selector: 'app-scans',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe, RouterLink],
  templateUrl: './scans.component.html',
  styleUrls: ['./scans.component.css']
})
export class ScansComponent implements OnInit {
  scanExecutions: ScanExecution[] = [];
  filteredScans: ScanExecution[] = [];
  loading = true;
  error: string | null = null;

  // Filters
  serviceFilter = '';
  toolFilter = '';
  statusFilter = '';

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    this.loadScanExecutions();
  }

  loadScanExecutions(): void {
    this.loading = true;
    this.error = null;
    
    this.apiService.getAllScanExecutions().subscribe({
      next: (scans) => {
        this.scanExecutions = scans;
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load scan executions';
        this.loading = false;
        console.error('Error loading scan executions:', err);
      }
    });
  }

  applyFilters(): void {
    this.filteredScans = this.scanExecutions.filter(scan => {
      if (this.serviceFilter && !scan.serviceName.toLowerCase().includes(this.serviceFilter.toLowerCase())) {
        return false;
      }
      if (this.toolFilter && scan.tool !== this.toolFilter) {
        return false;
      }
      if (this.statusFilter && scan.status !== this.statusFilter) {
        return false;
      }
      return true;
    });
  }

  onFilterChange(): void {
    this.applyFilters();
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

  getTriggerTypeClass(triggerType: string): string {
    switch (triggerType) {
      case 'GITHUB_ACTIONS': return 'trigger-github';
      case 'MANUAL_UPLOAD': return 'trigger-manual';
      case 'API': return 'trigger-api';
      default: return '';
    }
  }

  getUniqueServices(): string[] {
    const services = new Set(this.scanExecutions.map(scan => scan.serviceName));
    return Array.from(services).sort();
  }

  getUniqueTools(): string[] {
    const tools = new Set(this.scanExecutions.map(scan => scan.tool));
    return Array.from(tools).sort();
  }

  getUniqueStatuses(): string[] {
    const statuses = new Set(this.scanExecutions.map(scan => scan.status));
    return Array.from(statuses).sort();
  }

  formatScanTime(timeString: string | undefined): string {
    if (!timeString) return 'N/A';
    const date = new Date(timeString);
    return date.toLocaleString();
  }

  getSeverityCounts(scan: ScanExecution): { critical: number, high: number, medium: number, low: number } {
    return {
      critical: scan.criticalCount,
      high: scan.highCount,
      medium: scan.mediumCount,
      low: scan.lowCount
    };
  }

  getSuccessfulScansCount(): number {
    return this.filteredScans.filter(s => s.status === 'SUCCESS').length;
  }

  getFailedScansCount(): number {
    return this.filteredScans.filter(s => s.status === 'FAILED').length;
  }

  getTotalFindingsCount(): number {
    return this.filteredScans.reduce((sum, s) => sum + s.totalUniqueFindings, 0);
  }
}