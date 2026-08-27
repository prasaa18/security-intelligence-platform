import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { SecurityFinding } from '../../models/dashboard.model';

@Component({
  selector: 'app-findings',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './findings.component.html',
  styleUrls: ['./findings.component.css']
})
export class FindingsComponent implements OnInit {
  findings: SecurityFinding[] = [];
  filteredFindings: SecurityFinding[] = [];
  loading = true;
  error: string | null = null;

  searchQuery = '';
  selectedSeverity = '';
  selectedPriority = '';
  selectedTool = '';
  selectedService = '';
  selectedStatus = 'OPEN';

  severities = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'UNKNOWN'];
  priorities = ['P0', 'P1', 'P2', 'P3', 'P4'];
  statuses = ['', 'OPEN', 'RESOLVED', 'ACCEPTED_RISK', 'FALSE_POSITIVE'];

  constructor(private apiService: ApiService, private route: ActivatedRoute) {}

  ngOnInit() {
    const params = this.route.snapshot.queryParamMap;
    this.selectedService  = params.get('service')  || '';
    this.selectedPriority = params.get('priority') || '';
    this.selectedStatus   = params.get('status')   || 'OPEN';
    this.selectedTool     = params.get('tool')      || '';
    this.selectedSeverity = params.get('severity')  || '';
    this.loadFindings();
  }

  loadFindings() {
    this.loading = true;
    this.error = null;
    this.apiService.getAllFindings().subscribe({
      next: (data) => {
        this.findings = (data || []).sort((a, b) =>
          (b.riskScore || 0) - (a.riskScore || 0)
        );
        this.applyFilters();
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load findings. Is the backend running?';
        this.loading = false;
        this.findings = [];
      }
    });
  }

  applyFilters() {
    this.filteredFindings = this.findings.filter(f => {
      if (this.searchQuery.trim()) {
        const q = this.searchQuery.toLowerCase();
        if (!(
          (f.cve         && f.cve.toLowerCase().includes(q)) ||
          (f.title       && f.title.toLowerCase().includes(q)) ||
          (f.serviceName && f.serviceName.toLowerCase().includes(q)) ||
          (f.packageName && f.packageName.toLowerCase().includes(q))
        )) return false;
      }
      if (this.selectedSeverity && f.severity    !== this.selectedSeverity) return false;
      if (this.selectedPriority && f.priority    !== this.selectedPriority) return false;
      if (this.selectedTool     && f.tool        !== this.selectedTool)     return false;
      if (this.selectedService  && f.serviceName !== this.selectedService)  return false;
      if (this.selectedStatus   && f.status      !== this.selectedStatus)   return false;
      return true;
    });
  }

  onSearchChange() { this.applyFilters(); }
  onFilterChange()  { this.applyFilters(); }

  clearFilters() {
    this.searchQuery = '';
    this.selectedSeverity = '';
    this.selectedPriority = '';
    this.selectedTool = '';
    this.selectedService = '';
    this.selectedStatus = 'OPEN';
    this.applyFilters();
  }

  countByPriority(priority: string): number {
    return this.filteredFindings.filter(f => f.priority === priority).length;
  }

  getUniqueValues(field: keyof SecurityFinding): string[] {
    return [...new Set(this.findings.map(f => f[field] as string).filter(Boolean))].sort();
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

  getCvssClass(score: number): string {
    if (score >= 9)   return 'risk-score-high';
    if (score >= 7)   return 'risk-score-medium';
    return 'risk-score-low';
  }

  getRiskBadgeClass(score: number): string {
    if (score >= 90) return 'risk-score-high';
    if (score >= 55) return 'risk-score-medium';
    return 'risk-score-low';
  }

  getDetectionBadgeClass(state: string): string {
    if (!state) return '';
    switch (state.toUpperCase()) {
      case 'NEW':                        return 'badge-new-finding';
      case 'PRESENT':                    return 'badge-present';
      case 'NOT_DETECTED_IN_LATEST_SCAN': return 'badge-not-detected';
      default:                           return '';
    }
  }

  getStatusBadgeClass(status: string): string {
    if (!status) return 'badge';
    switch (status.toUpperCase()) {
      case 'OPEN':           return 'badge badge-open';
      case 'RESOLVED':       return 'badge badge-resolved';
      case 'ACCEPTED_RISK':  return 'badge badge-accepted';
      case 'FALSE_POSITIVE': return 'badge badge-accepted';
      default:               return 'badge badge-p4';
    }
  }

  exportCsv() {
    // Always export filtered data client-side to respect current filters
    this.exportClientSideCsv();
  }

  private exportClientSideCsv() {
    const headers = ['CVE','Title','Service','Environment','Tool','Severity','CVSS','Risk Score','Priority','Status','Package','Installed Version','Fixed Version','First Detected At','Last Detected At'];
    const rows = this.filteredFindings.map(f => [
      f.cve || '',
      `"${(f.title || '').replace(/"/g, '""')}"`,
      f.serviceName || '',
      f.environment || '',
      f.tool || '',
      f.severity || '',
      f.cvssScore || '',
      f.riskScore || '',
      f.priority || '',
      f.status || '',
      f.packageName || '',
      f.installedVersion || '',
      f.fixedVersion || '',
      f.firstDetectedAt || f.createdAt || '',
      f.lastDetectedAt || f.updatedAt || ''
    ].join(','));
    const csv = [headers.join(','), ...rows].join('\n');
    const blob = new Blob([csv], { type: 'text/csv' });
    const url  = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `security-findings-${new Date().toISOString().slice(0,10)}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  }
}