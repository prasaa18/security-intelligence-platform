import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { ToastService } from '../../services/toast.service';
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
  activePreset = 'ALL';

  selectedIds = new Set<string>();

  severities = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'UNKNOWN'];
  priorities = ['P0', 'P1', 'P2', 'P3', 'P4'];
  statuses = ['', 'OPEN', 'RESOLVED', 'ACCEPTED_RISK', 'FALSE_POSITIVE'];

  constructor(
    private apiService: ApiService,
    private route: ActivatedRoute,
    private toastService: ToastService
  ) {}

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

  setPreset(preset: string) {
    this.activePreset = preset;
    this.clearFilters();
    if (preset === 'P0') {
      this.selectedPriority = 'P0';
      this.selectedStatus = 'OPEN';
    } else if (preset === 'P1') {
      this.selectedPriority = 'P1';
      this.selectedStatus = 'OPEN';
    } else if (preset === 'FIXABLE') {
      this.selectedStatus = 'OPEN';
      this.filteredFindings = this.findings.filter(f => f.fixedVersion && f.status === 'OPEN');
      return;
    } else if (preset === 'RESOLVED') {
      this.selectedStatus = 'RESOLVED';
    }
    this.applyFilters();
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
    const vals = new Set<string>();
    for (const f of this.findings) {
      const v = f[field];
      if (typeof v === 'string' && v) vals.add(v);
    }
    return Array.from(vals).sort();
  }

  toggleSelect(id: string) {
    if (this.selectedIds.has(id)) {
      this.selectedIds.delete(id);
    } else {
      this.selectedIds.add(id);
    }
  }

  toggleSelectAll() {
    if (this.selectedIds.size === this.filteredFindings.length) {
      this.selectedIds.clear();
    } else {
      this.filteredFindings.forEach(f => this.selectedIds.add(f.id));
    }
  }

  isAllSelected(): boolean {
    return this.filteredFindings.length > 0 && this.selectedIds.size === this.filteredFindings.length;
  }

  copyFixSnippet(f: SecurityFinding) {
    let cmd = '';
    if (f.packageName && f.fixedVersion) {
      cmd = `npm update ${f.packageName}@${f.fixedVersion}`;
    } else {
      cmd = `Review finding ${f.cve || f.title}`;
    }
    navigator.clipboard.writeText(cmd);
    this.toastService.success(`Copied: ${cmd}`);
  }

  exportCsv() {
    this.apiService.downloadAllFindingsCsv().subscribe({
      next: (csv) => {
        const blob = new Blob([csv], { type: 'text/csv' });
        const url  = window.URL.createObjectURL(blob);
        const a    = document.createElement('a');
        a.href     = url;
        a.download = `security-findings-${new Date().toISOString().slice(0,10)}.csv`;
        a.click();
        window.URL.revokeObjectURL(url);
        this.toastService.success('Exported findings to CSV');
      },
      error: () => this.toastService.error('Failed to export CSV')
    });
  }
}