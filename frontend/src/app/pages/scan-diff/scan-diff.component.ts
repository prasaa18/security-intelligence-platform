import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { ToastService } from '../../services/toast.service';
import { retry } from 'rxjs';

@Component({
  selector: 'app-scan-diff',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './scan-diff.component.html',
  styleUrls: ['./scan-diff.component.css']
})
export class ScanDiffComponent implements OnInit {
  scansList: any[] = [];
  selectedBaselineId: string = '';
  selectedTargetId: string = '';

  diffData: any = null;
  loading: boolean = false;
  error: string | null = null;
  activeTab: 'new' | 'resolved' | 'persistent' = 'new';

  constructor(
    private apiService: ApiService,
    private route: ActivatedRoute,
    private toastService: ToastService
  ) {}

  ngOnInit() {
    this.loadScans();
  }

  loadScans() {
    this.loading = true;
    this.error = null;

    this.apiService.getAllScanExecutions().pipe(
      retry({ count: 2, delay: 800 })
    ).subscribe({
      next: (scans) => {
        this.scansList = (Array.isArray(scans) ? scans : []).filter(scan => !!scan?.id).sort((a, b) => {
          const aTime = new Date(a.completedAt || a.createdAt || a.receivedAt).getTime();
          const bTime = new Date(b.completedAt || b.createdAt || b.receivedAt).getTime();
          return bTime - aTime;
        });

        this.route.queryParams.subscribe(params => {
          if (params['targetId']) {
            this.selectedTargetId = params['targetId'];
            this.selectedBaselineId = params['baselineId'] || '';
            this.runComparison();
            return;
          }

          if (this.scansList.length === 0) {
            this.selectedTargetId = '';
            this.selectedBaselineId = '';
            this.diffData = null;
            this.loading = false;
            this.error = 'No scan executions available yet. Upload a scan to start comparing.';
            return;
          }

          this.selectedTargetId = this.scansList[0].id;
          this.selectedBaselineId = this.scansList.length > 1 ? this.scansList[1].id : '';
          this.runComparison();
        });
      },
      error: () => {
        this.loading = false;
        this.error = 'Failed to load scan executions. Retry to refresh the scan list.';
      }
    });
  }

  runComparison() {
    if (!this.selectedTargetId) {
      this.diffData = null;
      this.loading = false;
      return;
    }

    const targetExists = this.scansList.some(scan => scan.id === this.selectedTargetId);
    if (!targetExists) {
      this.selectedTargetId = this.scansList[0]?.id || '';
      this.selectedBaselineId = this.scansList.length > 1 ? this.scansList[1]?.id || '' : '';
      if (!this.selectedTargetId) {
        this.diffData = null;
        this.loading = false;
        this.error = 'No valid scan selected. Upload a report to compare results.';
        return;
      }
    }

    if (this.selectedBaselineId === this.selectedTargetId) {
      this.selectedBaselineId = '';
    }

    this.loading = true;
    this.error = null;

    if (this.selectedBaselineId) {
      this.apiService.compareScans(this.selectedBaselineId, this.selectedTargetId).subscribe({
        next: (res) => {
          this.diffData = res;
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Failed to compare scans: ' + (err.error?.message || err.message || 'Request failed');
          this.loading = false;
        }
      });
    } else {
      this.apiService.getScanDiff(this.selectedTargetId).subscribe({
        next: (res) => {
          this.diffData = res;
          if (res?.baselineScan) {
            this.selectedBaselineId = res.baselineScan.id;
          }
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Failed to get scan diff: ' + (err.error?.message || err.message || 'Request failed');
          this.loading = false;
        }
      });
    }
  }

  copyFixSnippet(finding: any) {
    let cmd = '';
    if (finding.packageName && finding.fixedVersion) {
      cmd = `npm update ${finding.packageName}@${finding.fixedVersion}`;
    } else {
      cmd = `Review finding ${finding.cve || finding.title}`;
    }
    navigator.clipboard.writeText(cmd);
    this.toastService.success(`Copied remediation snippet: ${cmd}`);
  }
}