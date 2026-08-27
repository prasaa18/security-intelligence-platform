import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { RemediationItem } from '../../models/dashboard.model';

@Component({
  selector: 'app-remediation',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe, RouterModule],
  templateUrl: './remediation.component.html',
  styleUrls: ['./remediation.component.css']
})
export class RemediationComponent implements OnDestroy, OnInit {
  remediationItems: RemediationItem[] = [];
  filteredItems: RemediationItem[] = [];
  loading = true;
  error: string | null = null;

  // Filters
  priorityFilter = '';
  teamFilter = '';
  serviceFilter = '';
  statusFilter = '';
  findingIdFilter = '';
  private refreshTimer?: ReturnType<typeof setInterval>;

  constructor(private apiService: ApiService, private route: ActivatedRoute) {}

  ngOnInit(): void {
    const params = this.route.snapshot.queryParamMap;
    this.serviceFilter = params.get('service') || '';
    this.priorityFilter = params.get('priority') || '';
    this.statusFilter = params.get('status') || '';
    this.findingIdFilter = params.get('findingId') || '';
    this.loadRemediationItems();
    this.refreshTimer = setInterval(() => this.loadRemediationItems(), 30000);
  }

  ngOnDestroy(): void { if (this.refreshTimer) clearInterval(this.refreshTimer); }

  loadRemediationItems(): void {
    this.loading = true;
    this.error = null;
    
    this.apiService.getAllRemediationItems().subscribe({
      next: (items) => {
        this.remediationItems = items || [];
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load remediation items';
        this.loading = false;
        console.error('Error loading remediation items:', err);
        this.remediationItems = [];
      }
    });
  }

  applyFilters(): void {
    this.filteredItems = this.remediationItems.filter(item => {
      if (this.priorityFilter && item.priority !== this.priorityFilter) return false;
      if (this.teamFilter && item.teamName !== this.teamFilter) return false;
      if (this.serviceFilter && item.serviceName !== this.serviceFilter) return false;
      if (this.statusFilter && item.remediationStatus !== this.statusFilter) return false;
      if (this.findingIdFilter && item.findingId !== this.findingIdFilter) return false;
      return true;
    });
  }

  onFilterChange(): void {
    this.applyFilters();
  }

  updateStatus(itemId: string, event: Event): void {
    const target = event.target as HTMLSelectElement;
    const newStatus = target.value;
    this.apiService.updateRemediationStatus(itemId, newStatus).subscribe({
      next: () => {
        this.loadRemediationItems();
      },
      error: (err) => {
        console.error('Error updating status:', err);
        alert('Failed to update status');
      }
    });
  }

  getPriorityClass(priority: string): string {
    switch (priority) {
      case 'P0': return 'priority-p0';
      case 'P1': return 'priority-p1';
      case 'P2': return 'priority-p2';
      case 'P3': return 'priority-p3';
      case 'P4': return 'priority-p4';
      default: return '';
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'NEW': return 'status-new';
      case 'OPEN': return 'status-open';
      case 'IN_PROGRESS': return 'status-in-progress';
      case 'RESOLVED': return 'status-resolved';
      case 'ACCEPTED_RISK': return 'status-accepted-risk';
      default: return '';
    }
  }

  getUniqueTeams(): string[] {
    const teams = new Set(this.remediationItems.map(item => item.teamName).filter((t): t is string => t !== undefined && t !== null));
    return Array.from(teams).sort();
  }

  getUniqueServices(): string[] {
    const services = new Set(this.remediationItems.map(item => item.serviceName));
    return Array.from(services).sort();
  }

  getUniqueStatuses(): string[] {
    const statuses = new Set(this.remediationItems.map(item => item.remediationStatus));
    return Array.from(statuses).sort();
  }

  getP0Count(): number {
    return this.filteredItems.filter(i => i.priority === 'P0').length;
  }

  getP1Count(): number {
    return this.filteredItems.filter(i => i.priority === 'P1').length;
  }

  getInProgressCount(): number {
    return this.filteredItems.filter(i => i.remediationStatus === 'IN_PROGRESS').length;
  }

  getRiskClass(riskScore: number | undefined): string {
    if (!riskScore) return 'risk-low';
    if (riskScore >= 90) return 'risk-high';
    if (riskScore >= 75) return 'risk-medium';
    return 'risk-low';
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return '—';
    return new Date(dateString).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  }

  clearFilters(): void {
    this.priorityFilter = '';
    this.teamFilter = '';
    this.serviceFilter = '';
    this.statusFilter = '';
    this.findingIdFilter = '';
    this.applyFilters();
  }

  getRiskBadgeClass(score: number): string {
    if (score >= 90) return 'risk-score-high';
    if (score >= 55) return 'risk-score-medium';
    return 'risk-score-low';
  }

  exportCsv(): void {
    const headers = ['Priority', 'CVE', 'Title', 'Service', 'Team', 'Risk Score', 'Status', 'Package', 'Installed Version', 'Fixed Version', 'Recommended Action', 'First Detected', 'Last Detected'];
    const rows = this.filteredItems.map(i => [
      i.priority || '',
      i.cve || '',
      `"${(i.title || '').replace(/"/g, '""')}"`,
      i.serviceName || '',
      i.teamName || '',
      i.riskScore || '',
      i.remediationStatus || '',
      i.packageName || '',
      i.installedVersion || '',
      i.fixedVersion || '',
      `"${(i.recommendedAction || '').replace(/"/g, '""')}"`,
      i.firstDetectedAt || '',
      i.lastDetectedAt || ''
    ].join(','));
    const csv = [headers.join(','), ...rows].join('\n');
    const blob = new Blob([csv], { type: 'text/csv' });
    const url  = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `remediation-plan-${new Date().toISOString().slice(0,10)}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  }
}