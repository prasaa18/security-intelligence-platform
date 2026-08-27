import { Component, OnDestroy, OnInit } from '@angular/core';
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
export class ScansComponent implements OnDestroy, OnInit {
  scanExecutions: ScanExecution[] = [];
  filteredScans: ScanExecution[] = [];
  loading = true;
  error: string | null = null;

  // Filters
  serviceFilter = '';
  toolFilter = '';
  statusFilter = '';
  environmentFilter = '';

  // Upload Panel state
  showUpload = false;
  uploadServiceName = '';
  uploadEnvironment = 'PRODUCTION';
  uploadFile: File | null = null;
  uploading = false;
  uploadMessage = '';
  uploadError = false;

  private refreshTimer?: ReturnType<typeof setInterval>;

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    this.loadScanExecutions();
    this.refreshTimer = setInterval(() => this.loadScanExecutions(), 30000);
  }

  ngOnDestroy(): void {
    if (this.refreshTimer) clearInterval(this.refreshTimer);
  }

  loadScanExecutions(): void {
    this.loading = true;
    this.error = null;
    
    this.apiService.getAllScanExecutions().subscribe({
      next: (scans) => {
        this.scanExecutions = (scans || []).sort((a, b) => {
          const timeA = new Date(a.completedAt || a.receivedAt || a.createdAt).getTime();
          const timeB = new Date(b.completedAt || b.receivedAt || b.createdAt).getTime();
          return timeB - timeA;
        });
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load scan executions';
        this.loading = false;
        console.error('Error loading scan executions:', err);
        this.scanExecutions = [];
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
      if (this.environmentFilter && scan.environment !== this.environmentFilter) return false;
      return true;
    });
  }

  onFilterChange(): void {
    this.applyFilters();
  }

  clearFilters(): void {
    this.serviceFilter = '';
    this.toolFilter = '';
    this.statusFilter = '';
    this.environmentFilter = '';
    this.applyFilters();
  }

  openUploadPanel(): void {
    this.showUpload = true;
    this.uploadMessage = '';
  }

  closeUpload(): void {
    this.showUpload = false;
    this.uploadFile = null;
    this.uploadMessage = '';
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.uploadFile = input.files[0];
    }
  }

  uploadReport(): void {
    if (!this.uploadFile || !this.uploadServiceName) return;
    this.uploading = true;
    this.uploadMessage = '';
    this.uploadError = false;

    this.apiService.uploadReport(this.uploadFile, this.uploadServiceName.trim(), this.uploadEnvironment).subscribe({
      next: (res) => {
        this.uploading = false;
        this.uploadMessage = `Scan uploaded successfully! ${res.totalFindings || 0} findings processed.`;
        this.uploadError = false;
        this.loadScanExecutions();
        setTimeout(() => {
          this.closeUpload();
        }, 2000);
      },
      error: (err) => {
        this.uploading = false;
        this.uploadError = true;
        this.uploadMessage = err.error?.message || 'Failed to upload report. Check file format.';
        console.error('Upload error:', err);
      }
    });
  }

  downloadScanFindings(scan: ScanExecution): void {
    // Fetch findings for this service and export CSV
    this.apiService.getFindingsByService(scan.serviceName).subscribe({
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
        a.download = `scan-${scan.serviceName}-${scan.tool}-${new Date().toISOString().slice(0,10)}.csv`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => {
        alert('Could not export findings for this scan.');
      }
    });
  }

  formatTrigger(trigger: string): string {
    if (!trigger) return 'Manual';
    switch (trigger.toUpperCase()) {
      case 'GITHUB_ACTIONS': return 'GitHub CI';
      case 'MANUAL_UPLOAD':  return 'Manual Upload';
      case 'API':            return 'API Ingestion';
      default:               return trigger;
    }
  }

  getToolBadgeClass(tool: string): string {
    if (!tool) return 'badge badge-other';
    const t = tool.toUpperCase();
    if (t === 'TRIVY') return 'badge badge-trivy';
    if (t === 'SNYK')  return 'badge badge-snyk';
    return 'badge badge-other';
  }

  getEnvBadgeClass(env: string): string {
    if (!env) return '';
    switch (env.toUpperCase()) {
      case 'PRODUCTION':  return 'badge-env-production';
      case 'STAGING':     return 'badge-env-staging';
      case 'DEVELOPMENT': return 'badge-env-development';
      default:            return '';
    }
  }

  getTriggerBadgeClass(trigger: string): string {
    if (!trigger) return 'badge-trigger-manual';
    switch (trigger.toUpperCase()) {
      case 'GITHUB_ACTIONS': return 'badge-trigger-actions';
      case 'API':            return 'badge-trigger-api';
      default:               return 'badge-trigger-manual';
    }
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'SUCCESS':    return 'badge badge-scan-success';
      case 'FAILED':     return 'badge badge-scan-failed';
      case 'PROCESSING': return 'badge badge-scan-processing';
      case 'RECEIVED':   return 'badge badge-scan-received';
      default:           return 'badge badge-p4';
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

  formatDate(dateString: string | undefined): string {
    if (!dateString) return '—';
    const date = new Date(dateString);
    return date.toLocaleString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
  }
}