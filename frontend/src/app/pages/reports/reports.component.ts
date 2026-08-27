import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { ScanReport, ServiceModel, SecurityFinding, RemediationItem, ScanExecution } from '../../models/dashboard.model';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.css']
})
export class ReportsComponent implements OnDestroy, OnInit {
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
  reportBeingShared: ScanReport | null = null;
  developerEmail = '';
  actionError: string | null = null;
  
  environments = ['DEVELOPMENT', 'STAGING', 'PRODUCTION'];
  private refreshTimer?: ReturnType<typeof setInterval>;

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.loadReports();
    this.loadServices();
    this.refreshTimer = setInterval(() => this.loadReports(), 30000);
  }

  ngOnDestroy(): void { if (this.refreshTimer) clearInterval(this.refreshTimer); }

  loadReports() {
    this.loading = true;
    this.error = null;
    
    this.apiService.getAllReports().subscribe({
      next: (data) => {
        this.reports = (data || []).sort((a, b) => 
          new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        );
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load reports';
        this.loading = false;
        console.error('Reports error:', err);
        this.reports = [];
      }
    });
  }

  loadServices() {
    this.apiService.getAllServices().subscribe({
      next: (data) => {
        this.services = (data || []).sort((a, b) => a.serviceName.localeCompare(b.serviceName));
      },
      error: (err) => {
        console.error('Services error:', err);
        this.services = [];
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

  downloadReport(report: ScanReport) {
    this.actionError = null;
    this.apiService.downloadReport(report.id).subscribe({
      next: (file) => {
        const url = URL.createObjectURL(file);
        const link = document.createElement('a');
        link.href = url;
        link.download = report.uploadedFileName || `${report.serviceName}-security-report.json`;
        link.click();
        URL.revokeObjectURL(url);
      },
      error: (err) => {
        this.actionError = err.error?.message || 'Original report is unavailable for this scan.';
      }
    });
  }

  openShareForm(report: ScanReport) {
    this.reportBeingShared = report;
    this.developerEmail = '';
    this.actionError = null;
  }

  closeShareForm() {
    this.reportBeingShared = null;
    this.developerEmail = '';
  }

  sendToDeveloper() {
    if (!this.reportBeingShared || !this.developerEmail.trim()) return;
    const report = this.reportBeingShared;
    const subject = `Security report: ${report.serviceName} (${report.tool})`;
    const body = [
      `Please review the ${report.tool} ${report.scanType} report for ${report.serviceName}.`,
      `Environment: ${report.environment}`,
      `Findings: ${report.totalFindings}`,
      `Uploaded: ${this.formatDate(report.createdAt)}`,
      '',
      'Download the original report from the Security Intelligence Platform.'
    ].join('\n');
    window.location.href = `mailto:${encodeURIComponent(this.developerEmail.trim())}?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;
    this.closeShareForm();
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
        if (response && (response.success === true || response.success === 'true')) {
          const tool = response.tool || 'Unknown';
          const rawFindings = response.rawFindings || response.totalRawFindings || 0;
          const uniqueFindings = response.uniqueFindings || response.totalUniqueFindings || 0;
          this.uploadSuccess = `Report processed successfully! Tool: ${tool}, Raw Findings: ${rawFindings}, Unique Findings: ${uniqueFindings}`;
          this.loadReports(); // Refresh the reports list
          setTimeout(() => {
            this.closeUploadForm();
          }, 3000);
        } else {
          this.uploadError = response?.message || 'Upload failed';
        }
      },
      error: (err) => {
        this.uploadLoading = false;
        this.uploadError = err.error?.message || err.message || 'Upload failed';
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

  exportConsolidatedReport() {
    this.actionError = null;
    forkJoin({
      services: this.apiService.getAllServices(),
      findings: this.apiService.getAllFindings(),
      remediation: this.apiService.getAllRemediationItems(),
      scans: this.apiService.getAllScanExecutions()
    }).subscribe({
      next: data => {
        const rows = [
          ['Record Type', 'Service', 'Owner', 'Environment', 'Priority', 'Severity', 'CVE', 'Title', 'Package', 'Installed Version', 'Fixed Version', 'Scanner', 'Scan Type', 'Status', 'Detection State', 'Risk Score', 'Scan Date'],
          ...data.findings.map((f: SecurityFinding) => ['Finding', f.serviceName, '', f.environment, f.priority || '', f.severity, f.cve || '', f.title || '', f.packageName || '', f.installedVersion || '', f.fixedVersion || '', f.tool, f.scanType, f.status, f.detectionState || '', String(f.riskScore || ''), f.lastDetectedAt || f.updatedAt]),
          ...data.remediation.map((r: RemediationItem) => ['Remediation', r.serviceName, r.teamName || '', '', r.priority, '', r.findingId, r.recommendedAction || '', '', '', '', '', '', r.remediationStatus, '', String(r.riskScore || ''), r.updatedAt]),
          ...data.scans.map((s: ScanExecution) => ['Scan', s.serviceName, '', s.environment, '', '', '', `${s.newFindings} new / ${s.resolvedFindings} resolved`, '', '', '', s.tool, s.scanType, s.status, '', String(s.totalUniqueFindings), s.completedAt || s.receivedAt])
        ];
        const csv = rows.map(row => row.map(value => `"${String(value ?? '').replace(/"/g, '""')}"`).join(',')).join('\r\n');
        const link = document.createElement('a');
        link.href = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' }));
        link.download = `security-intelligence-${new Date().toISOString().slice(0, 10)}.csv`;
        link.click();
        URL.revokeObjectURL(link.href);
      },
      error: () => this.actionError = 'Could not assemble the consolidated report. Retry after the API is available.'
    });
  }
}