import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { DashboardSummary, ActionCenterDashboard } from '../../models/dashboard.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  summary: DashboardSummary | null = null;
  actionCenter: ActionCenterDashboard | null = null;
  loading = true;
  error: string | null = null;
  showAiBrief = false;
  aiBrief: string | null = null;
  generatingBrief = false;

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.loadDashboard();
  }

  loadDashboard() {
    this.loading = true;
    this.error = null;
    
    // Load both traditional summary and action center
    this.apiService.getDashboardSummary().subscribe({
      next: (data) => {
        this.summary = data;
        this.loadActionCenter();
      },
      error: (err) => {
        this.error = 'Failed to load dashboard data';
        this.loading = false;
        console.error('Dashboard error:', err);
      }
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
        this.loading = false;
      }
    });
  }

  generateSecurityBrief() {
    this.generatingBrief = true;
    this.apiService.generateDailySecurityBrief().subscribe({
      next: (brief) => {
        this.aiBrief = brief;
        this.showAiBrief = true;
        this.generatingBrief = false;
      },
      error: (err) => {
        console.error('Failed to generate security brief:', err);
        this.generatingBrief = false;
        alert('Failed to generate security brief. AI may not be configured.');
      }
    });
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
}