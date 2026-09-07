import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { ToastService } from '../../services/toast.service';
import { RemediationItem } from '../../models/dashboard.model';

@Component({
  selector: 'app-remediation',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe, RouterModule],
  templateUrl: './remediation.component.html',
  styleUrls: ['./remediation.component.css']
})
export class RemediationComponent implements OnDestroy, OnInit {
  remediationItems: any[] = [];
  filteredItems: any[] = [];
  loading = true;
  error: string | null = null;

  // Filters
  priorityFilter = '';
  teamFilter = '';
  serviceFilter = '';
  statusFilter = '';
  findingIdFilter = '';
  private refreshTimer?: ReturnType<typeof setInterval>;

  selectedIds = new Set<string>();

  // Ticket Modal
  selectedItemForTicket: any = null;
  ticketMarkdown = '';

  constructor(
    private apiService: ApiService,
    private route: ActivatedRoute,
    private toastService: ToastService
  ) {}

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
        this.toastService.success(`Status updated to ${newStatus}`);
        this.loadRemediationItems();
      },
      error: (err) => {
        this.toastService.error('Failed to update status');
      }
    });
  }

  batchSetStatus(status: string) {
    if (this.selectedIds.size === 0) return;
    const ids = Array.from(this.selectedIds);
    this.apiService.batchUpdateRemediationStatus(ids, status).subscribe({
      next: () => {
        this.toastService.success(`Batch updated ${ids.length} items to ${status}!`);
        this.selectedIds.clear();
        this.loadRemediationItems();
      },
      error: () => this.toastService.error('Batch status update failed')
    });
  }

  toggleSelect(id: string) {
    if (this.selectedIds.has(id)) {
      this.selectedIds.delete(id);
    } else {
      this.selectedIds.add(id);
    }
  }

  toggleSelectAll() {
    if (this.selectedIds.size === this.filteredItems.length) {
      this.selectedIds.clear();
    } else {
      this.filteredItems.forEach(i => this.selectedIds.add(i.id));
    }
  }

  isAllSelected(): boolean {
    return this.filteredItems.length > 0 && this.selectedIds.size === this.filteredItems.length;
  }

  copyFixSnippet(item: any) {
    const cmd = item.recommendedAction || `Remediate item ${item.findingId}`;
    navigator.clipboard.writeText(cmd);
    this.toastService.success('Copied recommended fix action!');
  }

  openTicketModal(item: any) {
    this.selectedItemForTicket = item;
    this.ticketMarkdown = `### 🚨 Security Remediation Ticket: [${item.priority}] ${item.serviceName}
**Service:** ${item.serviceName}
**Assigned Team:** ${item.teamName || 'Unassigned'}
**Priority:** ${item.priority} (Risk Score: ${item.riskScore})
**Status:** ${item.remediationStatus}

#### Recommended Action
${item.recommendedAction || 'Update package to secure version and redeploy service.'}

#### Reproduction & Verification
1. Bump dependency in repository build file (\`pom.xml\` / \`package.json\` / \`requirements.txt\`).
2. Run automated test suite to ensure non-breaking changes.
3. Push to CI/CD pipeline to verify scan resolves vulnerability.
`;
  }

  closeTicketModal() {
    this.selectedItemForTicket = null;
    this.ticketMarkdown = '';
  }

  copyTicketMarkdown() {
    const content = this.ticketMarkdown?.trim();
    if (!content) {
      this.toastService.error('No ticket content to copy.');
      return;
    }

    const copyText = async (text: string) => {
      if (navigator.clipboard && window.isSecureContext) {
        try {
          await navigator.clipboard.writeText(text);
          this.toastService.success('Copied Jira / GitHub Issue markdown!');
          return;
        } catch {
          // Fall through to manual textarea copy for insecure contexts.
        }
      }

      const textarea = document.createElement('textarea');
      textarea.value = text;
      textarea.setAttribute('readonly', '');
      textarea.style.position = 'fixed';
      textarea.style.opacity = '0';
      document.body.appendChild(textarea);
      textarea.select();
      try {
        document.execCommand('copy');
        this.toastService.success('Copied Jira / GitHub Issue markdown!');
      } catch {
        this.toastService.error('Copy failed in this browser context. Please copy manually.');
      } finally {
        document.body.removeChild(textarea);
      }
    };

    void copyText(content);
  }

  exportCsv(): void {
    this.apiService.downloadAllFindingsCsv().subscribe({
      next: (csv) => {
        const blob = new Blob([csv], { type: 'text/csv' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `remediation-plan-${new Date().toISOString().slice(0, 10)}.csv`;
        a.click();
        this.toastService.success('Exported remediation plan to CSV');
      },
      error: () => this.toastService.error('Failed to export CSV')
    });
  }

  getUniqueTeams(): string[] {
    const teams = new Set<string>();
    this.remediationItems.forEach(i => { if (i.teamName) teams.add(i.teamName); });
    return Array.from(teams).sort();
  }

  getUniqueServices(): string[] {
    const services = new Set<string>();
    this.remediationItems.forEach(i => { if (i.serviceName) services.add(i.serviceName); });
    return Array.from(services).sort();
  }
}