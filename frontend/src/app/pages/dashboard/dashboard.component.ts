import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { ApiService } from '../../services/api.service';
import { DashboardSummary, ActionCenterDashboard } from '../../models/dashboard.model';
import { ServiceModel, ScanExecution, SecurityFinding } from '../../models/dashboard.model';
import { forkJoin } from 'rxjs';

interface ServiceOverviewRow {
  service: ServiceModel;
  latestScan?: ScanExecution;
  tools: string;
  scanTypes: string;
  p0: number;
  p1: number;
  critical: number;
  high: number;
  open: number;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnDestroy, OnInit {
  summary: DashboardSummary | null = null;
  actionCenter: ActionCenterDashboard | null = null;
  loading = true;
  error: string | null = null;
  showAiBrief = false;
  aiBrief: SafeHtml | null = null;
  generatingBrief = false;
  sendingDailyBrief = false;
  dailyBriefMessage = '';
  persona: 'SECURITY_HEAD' | 'DEVELOPER' | 'SECURITY_ENGINEER' = 'SECURITY_HEAD';
  private refreshTimer?: ReturnType<typeof setInterval>;
  serviceOverview: ServiceOverviewRow[] = [];

  constructor(private apiService: ApiService, private sanitizer: DomSanitizer) {}

  ngOnInit() {
    if (typeof localStorage !== 'undefined') {
      this.persona = (localStorage.getItem('security-persona') as typeof this.persona) || 'SECURITY_HEAD';
    }
    this.loadDashboard();
    this.refreshTimer = setInterval(() => this.loadDashboard(), 30000);
  }

  ngOnDestroy(): void {
    if (this.refreshTimer) clearInterval(this.refreshTimer);
  }

  setPersona(persona: typeof this.persona): void {
    this.persona = persona;
    if (typeof localStorage !== 'undefined') localStorage.setItem('security-persona', persona);
  }

  loadDashboard() {
    this.loading = true;
    this.error = null;
    
    // Load both traditional summary and action center
    this.apiService.getDashboardSummary().subscribe({
      next: (data) => {
        this.summary = data || this.getDefaultSummary();
        this.loadServiceOverview();
        this.loadActionCenter();
      },
      error: (err) => {
        console.error('Dashboard error:', err);
        this.error = 'Security data is currently unavailable. Retry to load live metrics.';
        this.summary = null;
        this.loadServiceOverview();
        this.loadActionCenter();
      }
    });
  }

  loadServiceOverview() {
    forkJoin({
      services: this.apiService.getAllServices(),
      scans: this.apiService.getAllScanExecutions(),
      findings: this.apiService.getAllFindings()
    }).subscribe({
      next: ({ services, scans, findings }) => {
        this.serviceOverview = services.map(service => {
          const serviceScans = scans.filter(scan => scan.serviceName === service.serviceName)
            .sort((a, b) => new Date(b.completedAt || b.receivedAt).getTime() - new Date(a.completedAt || a.receivedAt).getTime());
          const serviceFindings = findings.filter(finding => finding.serviceName === service.serviceName && finding.status === 'OPEN');
          return {
            service,
            latestScan: serviceScans[0],
            tools: [...new Set(serviceScans.map(scan => scan.tool))].join(', ') || 'No scans',
            scanTypes: [...new Set(serviceScans.map(scan => scan.scanType))].join(', ') || 'No scans',
            p0: serviceFindings.filter(finding => finding.priority === 'P0').length,
            p1: serviceFindings.filter(finding => finding.priority === 'P1').length,
            critical: serviceFindings.filter(finding => finding.severity === 'CRITICAL').length,
            high: serviceFindings.filter(finding => finding.severity === 'HIGH').length,
            open: serviceFindings.length
          };
        }).sort((a, b) => (b.p0 + b.p1 + b.critical) - (a.p0 + a.p1 + a.critical));
      },
      error: () => this.serviceOverview = []
    });
  }

  loadActionCenter() {
    this.apiService.getActionCenterDashboard().subscribe({
      next: (data) => {
        this.actionCenter = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Action center error:', err);
        this.actionCenter = this.getDefaultActionCenter();
        this.loading = false;
      }
    });
  }

  getDefaultSummary(): DashboardSummary {
    return {
      totalFindings: 0,
      uniqueFindings: 0,
      critical: 0,
      high: 0,
      medium: 0,
      low: 0,
      p0: 0,
      p1: 0,
      p2: 0,
      p3: 0,
      p4: 0,
      topPriorities: [],
      scannerDistribution: {},
      scanTypeDistribution: {}
    };
  }

  getDefaultActionCenter(): ActionCenterDashboard {
    return {
      immediateActions: 0,
      dueThisWeek: 0,
      staleServices: 0,
      recentlyResolved: 0,
      topRemediationItems: [],
      recentScanActivity: []
    };
  }

  generateSecurityBrief() {
    this.generatingBrief = true;
    this.apiService.generateDailySecurityBrief().subscribe({
      next: (response) => {
        const formattedText = this.isAiUnavailable(response)
          ? this.buildLocalSecurityBrief()
          : this.formatResponse(response);
        this.aiBrief = this.sanitizer.bypassSecurityTrustHtml(formattedText);
        this.showAiBrief = true;
        this.generatingBrief = false;
      },
      error: (err) => {
        console.error('Failed to generate security brief:', err);
        this.aiBrief = this.sanitizer.bypassSecurityTrustHtml(this.buildLocalSecurityBrief());
        this.showAiBrief = true;
        this.generatingBrief = false;
        this.error = 'AI brief unavailable; showing the live deterministic security brief.';
      }
    });
  }

  sendDailyBriefToOwners(): void {
    this.sendingDailyBrief = true;
    this.dailyBriefMessage = '';
    this.apiService.sendDailyBrief().subscribe({
      next: response => { this.dailyBriefMessage = `Brief sent to ${response.sent || 0} service owners.`; this.sendingDailyBrief = false; },
      error: err => { this.dailyBriefMessage = err.error?.message || 'Email delivery is not configured.'; this.sendingDailyBrief = false; }
    });
  }

  private isAiUnavailable(response: any): boolean {
    const text = typeof response === 'string' ? response : response?.message || '';
    return /not configured|error calling|failed to/i.test(text);
  }

  private buildLocalSecurityBrief(): string {
    const immediate = this.actionCenter?.immediateActions || 0;
    const due = this.actionCenter?.dueThisWeek || 0;
    const stale = this.actionCenter?.staleServices || 0;
    const resolved = this.actionCenter?.recentlyResolved || 0;
    const topServices = this.serviceOverview.filter(row => row.open > 0).slice(0, 3)
      .map(row => `${row.service.serviceName} (${row.open} open findings)`).join(', ') || 'No active service findings';
    return `<h3>Today's Security Brief</h3><ul><li><strong>${immediate}</strong> P0 items need immediate attention and <strong>${due}</strong> P1 items are due this week.</li><li><strong>${stale}</strong> services have stale or missing scan coverage.</li><li>Highest active service risk: ${topServices}.</li><li><strong>${resolved}</strong> remediation items were recently resolved.</li></ul>`;
  }

  formatResponse(response: any): string {
    if (typeof response === 'string') {
      // Try to parse JSON if it's a JSON string
      try {
        const parsed = JSON.parse(response);
        if (parsed.steps && Array.isArray(parsed.steps)) {
          // Extract text content from steps array
          for (const step of parsed.steps) {
            if (step.type === 'model_output' && step.content && Array.isArray(step.content)) {
              const textContent = step.content.find((c: any) => c.text);
              if (textContent && textContent.text) {
                return this.formatMarkdown(textContent.text);
              }
            }
          }
        }
        // If no steps found, return the original string
        return this.formatMarkdown(response);
      } catch (e) {
        // Not JSON, return as-is with markdown formatting
        return this.formatMarkdown(response);
      }
    }
    if (response && response.message) {
      return this.formatMarkdown(response.message);
    }
    if (response && response.brief) {
      return this.formatMarkdown(response.brief);
    }
    if (typeof response === 'object') {
      try {
        return this.formatMarkdown(JSON.stringify(response, null, 2));
      } catch (e) {
        return 'Received complex response. Please check the console for details.';
      }
    }
    return this.formatMarkdown(String(response));
  }

  formatMarkdown(text: string): string {
    if (!text) return '';
    
    // Convert markdown to HTML for display
    let formatted = text
      // Headers
      .replace(/^### (.*$)/gim, '<h3>$1</h3>')
      .replace(/^## (.*$)/gim, '<h2>$1</h2>')
      .replace(/^# (.*$)/gim, '<h1>$1</h1>')
      // Bold
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      // Italic
      .replace(/\*(.*?)\*/g, '<em>$1</em>')
      // Code blocks
      .replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
      // Inline code
      .replace(/`([^`]+)`/g, '<code>$1</code>')
      // Lists
      .replace(/^\s*-\s(.*$)/gim, '<li>$1</li>')
      .replace(/^\s*\d+\.\s(.*$)/gim, '<li>$1</li>')
      // Line breaks
      .replace(/\n/g, '<br>');
    
    // Wrap lists
    formatted = formatted.replace(/(<li>.*<\/li>)/g, '<ul>$1</ul>');
    
    return formatted;
  }

  getSeverityClass(severity: string): string {
    return `severity-${severity.toLowerCase()}`;
  }

  getPriorityClass(priority: string): string {
    return `priority-${priority.toLowerCase()}`;
  }

  getObjectKeys(obj: any): string[] {
    return Object.keys(obj || {});
  }

  getObjectValues(obj: any): number[] {
    return Object.values(obj || {});
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString();
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

  getStatusClass(status: string): string {
    switch (status) {
      case 'SUCCESS': return 'status-success';
      case 'FAILED': return 'status-failed';
      case 'PROCESSING': return 'status-processing';
      case 'RECEIVED': return 'status-received';
      default: return '';
    }
  }

  getOverallRiskLabel(): string {
    if (!this.summary) return 'UNKNOWN';
    if (this.summary.p0 > 0 || this.summary.critical > 0) return 'CRITICAL';
    if (this.summary.p1 > 0 || this.summary.high > 0) return 'ATTENTION';
    return 'HEALTHY';
  }

  getScanFreshness(): number {
    if (!this.actionCenter) return 0;
    const total = this.actionCenter.recentScanActivity.length + this.actionCenter.staleServices;
    return total === 0 ? 0 : Math.round((this.actionCenter.recentScanActivity.length / total) * 100);
  }

  getStatusBadgeClass(status: string): string {
    if (!status) return 'badge badge-p4';
    switch (status.toUpperCase()) {
      case 'NEW':           return 'badge badge-new';
      case 'OPEN':          return 'badge badge-open';
      case 'IN_PROGRESS':   return 'badge badge-in-progress';
      case 'RESOLVED':      return 'badge badge-resolved';
      case 'ACCEPTED_RISK': return 'badge badge-accepted';
      default:              return 'badge badge-p4';
    }
  }

  getScanStatusBadgeClass(status: string): string {
    if (!status) return 'badge';
    switch (status.toUpperCase()) {
      case 'SUCCESS':    return 'badge badge-scan-success';
      case 'FAILED':     return 'badge badge-scan-failed';
      case 'PROCESSING': return 'badge badge-scan-processing';
      case 'RECEIVED':   return 'badge badge-scan-received';
      default:           return 'badge badge-p4';
    }
  }
}