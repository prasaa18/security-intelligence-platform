import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
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
  
  // Search and filters
  searchQuery = '';
  selectedSeverity = '';
  selectedPriority = '';
  selectedTool = '';
  selectedService = '';
  selectedStatus = 'OPEN';
  
  severities = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'UNKNOWN'];
  priorities = ['P0', 'P1', 'P2', 'P3', 'P4'];
  tools = ['TRIVY', 'SNYK', 'SONARQUBE', 'FORTIFY', 'BLACK_DUCK', 'OWASP_ZAP', 'CRUNCH_42'];
  statuses = ['OPEN', 'RESOLVED', 'IGNORED'];

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.loadFindings();
  }

  loadFindings() {
    this.loading = true;
    this.error = null;
    
    this.apiService.getAllFindings().subscribe({
      next: (data) => {
        this.findings = data.sort((a, b) => {
          // Sort by risk score desc, then by created date desc
          if (a.riskScore !== b.riskScore) {
            return (b.riskScore || 0) - (a.riskScore || 0);
          }
          return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
        });
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load findings';
        this.loading = false;
        console.error('Findings error:', err);
      }
    });
  }

  applyFilters() {
    this.filteredFindings = this.findings.filter(finding => {
      // Search filter
      if (this.searchQuery.trim()) {
        const query = this.searchQuery.toLowerCase();
        const matchesSearch = 
          (finding.cve && finding.cve.toLowerCase().includes(query)) ||
          (finding.title && finding.title.toLowerCase().includes(query)) ||
          (finding.serviceName && finding.serviceName.toLowerCase().includes(query)) ||
          (finding.packageName && finding.packageName.toLowerCase().includes(query));
        
        if (!matchesSearch) return false;
      }

      // Severity filter
      if (this.selectedSeverity && finding.severity !== this.selectedSeverity) {
        return false;
      }

      // Priority filter
      if (this.selectedPriority && finding.priority !== this.selectedPriority) {
        return false;
      }

      // Tool filter
      if (this.selectedTool && finding.tool !== this.selectedTool) {
        return false;
      }

      // Service filter
      if (this.selectedService && finding.serviceName !== this.selectedService) {
        return false;
      }

      // Status filter
      if (this.selectedStatus && finding.status !== this.selectedStatus) {
        return false;
      }

      return true;
    });
  }

  onSearchChange() {
    this.applyFilters();
  }

  onFilterChange() {
    this.applyFilters();
  }

  clearFilters() {
    this.searchQuery = '';
    this.selectedSeverity = '';
    this.selectedPriority = '';
    this.selectedTool = '';
    this.selectedService = '';
    this.selectedStatus = 'OPEN';
    this.applyFilters();
  }

  getSeverityClass(severity: string): string {
    return `severity-${severity.toLowerCase()}`;
  }

  getPriorityClass(priority: string): string {
    return `priority-${priority.toLowerCase()}`;
  }

  getToolClass(tool: string): string {
    return `tool-${tool.toLowerCase()}`;
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString();
  }

  getUniqueValues(field: keyof SecurityFinding): string[] {
    const values = this.findings.map(f => f[field]).filter(v => v != null);
    return [...new Set(values as string[])].sort();
  }
}