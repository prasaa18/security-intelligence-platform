import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-scan-detail',
  standalone: true,
  imports: [CommonModule, DatePipe, RouterLink],
  templateUrl: './scan-detail.component.html',
  styleUrls: ['./scan-detail.component.css']
})
export class ScanDetailComponent implements OnInit {
  scanExecution: any = null;
  loading = true;
  error: string | null = null;

  constructor(
    public route: ActivatedRoute,
    public router: Router,
    private apiService: ApiService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadScanExecution(id);
    } else {
      this.error = 'Invalid scan execution ID';
      this.loading = false;
    }
  }

  loadScanExecution(id: string): void {
    this.loading = true;
    this.apiService.getScanExecutionById(id).subscribe({
      next: (scan) => {
        this.scanExecution = scan;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load scan execution';
        this.loading = false;
        console.error('Error loading scan execution:', err);
      }
    });
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

  getTriggerTypeClass(triggerType: string): string {
    switch (triggerType) {
      case 'GITHUB_ACTIONS': return 'trigger-github';
      case 'MANUAL_UPLOAD': return 'trigger-manual';
      case 'API': return 'trigger-api';
      default: return '';
    }
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString();
  }

  calculateDuration(): string {
    if (!this.scanExecution?.startedAt || !this.scanExecution?.completedAt) {
      return 'N/A';
    }
    const start = new Date(this.scanExecution.startedAt);
    const end = new Date(this.scanExecution.completedAt);
    const duration = end.getTime() - start.getTime();
    
    const seconds = Math.floor(duration / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    
    if (hours > 0) {
      return `${hours}h ${minutes % 60}m ${seconds % 60}s`;
    } else if (minutes > 0) {
      return `${minutes}m ${seconds % 60}s`;
    } else {
      return `${seconds}s`;
    }
  }
}