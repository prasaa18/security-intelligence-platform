import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { SecurityFinding } from '../../models/dashboard.model';

@Component({
  selector: 'app-finding-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './finding-detail.component.html',
  styleUrls: ['./finding-detail.component.css']
})
export class FindingDetailComponent implements OnInit {
  finding: SecurityFinding | null = null;
  loading = true;
  error: string | null = null;
  findingId: string;

  constructor(
    private route: ActivatedRoute,
    private apiService: ApiService
  ) {
    this.findingId = this.route.snapshot.params['id'];
  }

  ngOnInit() {
    this.loadFinding();
  }

  loadFinding() {
    this.loading = true;
    this.error = null;
    
    this.apiService.getFindingById(this.findingId).subscribe({
      next: (data) => {
        this.finding = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load finding details';
        this.loading = false;
        console.error('Finding detail error:', err);
      }
    });
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
    return new Date(dateString).toLocaleString();
  }

  getSourceTools(): string[] {
    if (!this.finding?.sourceFindings) return [];
    return this.finding.sourceFindings.map(sf => sf.split(':')[0]);
  }
}