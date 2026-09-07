import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { ToastService } from '../../services/toast.service';

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
    this.apiService.getAllScanExecutions().subscribe({
      next: (scans) => {
        this.scansList = scans || [];
        this.route.queryParams.subscribe(params => {
          if (params['targetId']) {
            this.selectedTargetId = params['targetId'];
            this.selectedBaselineId = params['baselineId'] || '';
            this.runComparison();
          } else if (this.scansList.length >= 2) {
            this.selectedTargetId = this.scansList[0].id;
            this.selectedBaselineId = this.scansList[1].id;
            this.runComparison();
          } else if (this.scansList.length === 1) {
            this.selectedTargetId = this.scansList[0].id;
            this.runComparison();
          }
        });
      },
      error: () => {
        this.error = 'Failed to load scan executions';
      }
    });
  }

  runComparison() {
    if (!this.selectedTargetId) return;
    this.loading = true;
    this.error = null;

    if (this.selectedBaselineId) {
      this.apiService.compareScans(this.selectedBaselineId, this.selectedTargetId).subscribe({
        next: (res) => {
          this.diffData = res;
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Failed to compare scans: ' + (err.error?.message || err.message);
          this.loading = false;
        }
      });
    } else {
      this.apiService.getScanDiff(this.selectedTargetId).subscribe({
        next: (res) => {
          this.diffData = res;
          if (res.baselineScan) {
            this.selectedBaselineId = res.baselineScan.id;
          }
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Failed to get scan diff: ' + (err.error?.message || err.message);
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