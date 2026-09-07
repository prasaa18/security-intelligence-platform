import { Component, OnInit, OnDestroy, HostListener, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { ThemeService } from '../../services/theme.service';
import { ToastService } from '../../services/toast.service';

export interface CommandItem {
  id: string;
  category: 'Navigation' | 'Actions' | 'Services' | 'Findings';
  title: string;
  subtitle?: string;
  icon: string;
  badge?: string;
  action: () => void;
}

@Component({
  selector: 'app-command-palette',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './command-palette.component.html',
  styleUrls: ['./command-palette.component.css']
})
export class CommandPaletteComponent implements OnInit, OnDestroy {
  isOpen = false;
  searchQuery = '';
  selectedIndex = 0;
  
  @ViewChild('searchInput') searchInput!: ElementRef<HTMLInputElement>;

  servicesList: any[] = [];
  findingsList: any[] = [];

  constructor(
    private router: Router,
    private apiService: ApiService,
    private themeService: ThemeService,
    private toastService: ToastService
  ) {}

  ngOnInit() {
    this.loadData();
  }

  ngOnDestroy() {}

  loadData() {
    this.apiService.getAllServices().subscribe({
      next: (services) => { this.servicesList = services || []; },
      error: () => {}
    });
    this.apiService.getTopPriorityFindings(20).subscribe({
      next: (findings) => { this.findingsList = findings || []; },
      error: () => {}
    });
  }

  @HostListener('window:keydown', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent) {
    if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
      event.preventDefault();
      this.toggle();
    } else if (event.key === 'Escape' && this.isOpen) {
      event.preventDefault();
      this.close();
    } else if (this.isOpen) {
      const items = this.filteredItems;
      if (event.key === 'ArrowDown') {
        event.preventDefault();
        this.selectedIndex = (this.selectedIndex + 1) % (items.length || 1);
      } else if (event.key === 'ArrowUp') {
        event.preventDefault();
        this.selectedIndex = (this.selectedIndex - 1 + items.length) % (items.length || 1);
      } else if (event.key === 'Enter') {
        event.preventDefault();
        if (items.length > 0 && items[this.selectedIndex]) {
          items[this.selectedIndex].action();
          this.close();
        }
      }
    }
  }

  toggle() {
    this.isOpen ? this.close() : this.open();
  }

  open() {
    this.isOpen = true;
    this.searchQuery = '';
    this.selectedIndex = 0;
    this.loadData();
    setTimeout(() => {
      if (this.searchInput) {
        this.searchInput.nativeElement.focus();
      }
    }, 50);
  }

  close() {
    this.isOpen = false;
  }

  get filteredItems(): CommandItem[] {
    const q = this.searchQuery.trim().toLowerCase();

    // Static Navigation
    const navItems: CommandItem[] = [
      { id: 'nav-dashboard', category: 'Navigation', title: 'Action Center', subtitle: 'Executive dashboard & immediate actions', icon: '🎯', action: () => this.router.navigate(['/dashboard']) },
      { id: 'nav-remediation', category: 'Navigation', title: 'Remediation Plan', subtitle: 'Triage, fix & track remediation tasks', icon: '🔧', action: () => this.router.navigate(['/remediation']) },
      { id: 'nav-findings', category: 'Navigation', title: 'Security Findings', subtitle: 'Browse deduplicated CVEs & vulnerabilities', icon: '🔍', action: () => this.router.navigate(['/findings']) },
      { id: 'nav-services', category: 'Navigation', title: 'Services Catalog', subtitle: 'Manage service business context & health', icon: '🏢', action: () => this.router.navigate(['/services']) },
      { id: 'nav-scans', category: 'Navigation', title: 'Scan Executions', subtitle: 'View scan runs, CI/CD pushes & history', icon: '📡', action: () => this.router.navigate(['/scans']) },
      { id: 'nav-scan-diff', category: 'Navigation', title: 'Scan Diff & Time Travel', subtitle: 'Compare scan runs for regressions & fixes', icon: '⚖️', action: () => this.router.navigate(['/scans/compare']) },
      { id: 'nav-compliance', category: 'Navigation', title: 'Compliance & Audit Center', subtitle: 'OWASP Top 10, SOC 2, PCI-DSS posture', icon: '📜', action: () => this.router.navigate(['/compliance']) },
      { id: 'nav-ingestion', category: 'Navigation', title: 'CI/CD Ingestion Studio', subtitle: 'Simulate & test scanner report payloads', icon: '🚀', action: () => this.router.navigate(['/ingestion-studio']) },
      { id: 'nav-ai', category: 'Navigation', title: 'AI Assistant', subtitle: 'Ask security copilot for remediation guidance', icon: '🤖', action: () => this.router.navigate(['/ai-assistant']) }
    ];

    // Static Actions
    const actionItems: CommandItem[] = [
      {
        id: 'act-theme', category: 'Actions', title: 'Toggle Dark / Light Theme',
        subtitle: `Currently ${this.themeService.isDark() ? 'Dark' : 'Light'} Mode`,
        icon: this.themeService.isDark() ? '☀️' : '🌙',
        action: () => {
          this.themeService.toggleTheme();
          this.toastService.info(`Switched to ${this.themeService.currentTheme} theme`);
        }
      },
      {
        id: 'act-export', category: 'Actions', title: 'Export All Findings CSV',
        subtitle: 'Download deduplicated findings report',
        icon: '📥',
        action: () => {
          this.apiService.downloadAllFindingsCsv().subscribe({
            next: (csv) => {
              const blob = new Blob([csv], { type: 'text/csv' });
              const url = window.URL.createObjectURL(blob);
              const a = document.createElement('a');
              a.href = url;
              a.download = `security-findings-${new Date().toISOString().slice(0, 10)}.csv`;
              a.click();
              this.toastService.success('CSV export started');
            },
            error: () => this.toastService.error('Failed to export CSV')
          });
        }
      },
      {
        id: 'act-brief', category: 'Actions', title: 'Send Daily Security Brief',
        subtitle: 'Trigger daily security email/slack notification',
        icon: '✉️',
        action: () => {
          this.apiService.sendDailyBrief().subscribe({
            next: () => this.toastService.success('Daily brief notification dispatched!'),
            error: () => this.toastService.error('Failed to dispatch daily brief')
          });
        }
      },
      {
        id: 'act-seed', category: 'Actions', title: 'Seed Sample Data',
        subtitle: 'Load demonstration services, scans & vulnerabilities',
        icon: '🌱',
        action: () => {
          this.apiService.seedSampleData().subscribe({
            next: () => {
              this.toastService.success('Sample data loaded successfully');
              window.location.reload();
            },
            error: () => this.toastService.error('Failed to seed sample data')
          });
        }
      }
    ];

    // Dynamic Services
    const serviceItems: CommandItem[] = this.servicesList.map(s => ({
      id: `svc-${s.id}`,
      category: 'Services',
      title: s.serviceName,
      subtitle: `${s.teamName || 'Unassigned'} • ${s.businessCriticality || 'MEDIUM'}`,
      icon: '🏢',
      badge: s.environment || 'PRODUCTION',
      action: () => this.router.navigate(['/services', s.serviceName, 'security'])
    }));

    // Dynamic Findings
    const findingItems: CommandItem[] = this.findingsList.map(f => ({
      id: `fnd-${f.id}`,
      category: 'Findings',
      title: `${f.cve || 'VULN'} - ${f.title || f.packageName}`,
      subtitle: `${f.serviceName} • ${f.packageName || ''} (${f.installedVersion || ''} → ${f.fixedVersion || 'No fix'})`,
      icon: '🛡️',
      badge: f.priority || f.severity,
      action: () => this.router.navigate(['/findings', f.id])
    }));

    const all = [...navItems, ...actionItems, ...serviceItems, ...findingItems];
    if (!q) {
      return all;
    }

    return all.filter(item =>
      item.title.toLowerCase().includes(q) ||
      (item.subtitle && item.subtitle.toLowerCase().includes(q)) ||
      item.category.toLowerCase().includes(q)
    );
  }

  selectItem(index: number) {
    this.selectedIndex = index;
    const items = this.filteredItems;
    if (items[index]) {
      items[index].action();
      this.close();
    }
  }
}