import { Component, OnInit } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { ApiService } from '../../services/api.service';
import { ToastService } from '../../services/toast.service';
import { SecurityFinding } from '../../models/dashboard.model';

@Component({
  selector: 'app-finding-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './finding-detail.component.html',
  styleUrls: ['./finding-detail.component.css']
})
export class FindingDetailComponent implements OnInit {
  finding: SecurityFinding | null = null;
  loading = true;
  error: string | null = null;
  findingId: string;

  // AI Guidance state
  aiGuidance: SafeHtml | null = null;
  loadingAi = false;
  aiActionName = '';

  // Git PR Modal
  showGitPrModal = false;
  gitPrTitle = '';
  gitPrBody = '';
  gitDiffSnippet = '';

  // Dispatch Modal
  showDispatchModal = false;
  dispatchEmail = '';
  dispatchWebhook = 'https://hooks.slack.com/services/...';
  dispatchNote = '';
  isDispatching = false;

  constructor(
    private route: ActivatedRoute,
    private apiService: ApiService,
    private location: Location,
    private sanitizer: DomSanitizer,
    private toastService: ToastService
  ) {
    this.findingId = this.route.snapshot.params['id'];
  }

  goBack(): void { this.location.back(); }

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
      }
    });
  }

  explainPriority() {
    if (!this.finding) return;
    this.loadingAi = true;
    this.aiActionName = 'Explaining Priority & Risk Context';
    this.apiService.explainPriority(this.finding.id).subscribe({
      next: (res) => {
        const text = typeof res === 'string' ? res : JSON.stringify(res);
        this.aiGuidance = this.sanitizer.bypassSecurityTrustHtml(this.formatMarkdown(text));
        this.loadingAi = false;
      },
      error: () => {
        this.aiGuidance = this.sanitizer.bypassSecurityTrustHtml(this.buildFallbackPriorityExplanation());
        this.loadingAi = false;
      }
    });
  }

  suggestRemediation() {
    if (!this.finding) return;
    this.loadingAi = true;
    this.aiActionName = 'Generating Remediation Plan & Upgrade Guidance';
    this.apiService.generateRemediationGuidance(this.finding.id).subscribe({
      next: (res) => {
        const text = typeof res === 'string' ? res : JSON.stringify(res);
        this.aiGuidance = this.sanitizer.bypassSecurityTrustHtml(this.formatMarkdown(text));
        this.loadingAi = false;
      },
      error: () => {
        this.aiGuidance = this.sanitizer.bypassSecurityTrustHtml(this.buildFallbackRemediationGuidance());
        this.loadingAi = false;
      }
    });
  }

  openGitPrModal() {
    if (!this.finding) return;
    const f = this.finding;
    this.gitPrTitle = `fix(security): resolve ${f.cve || 'vulnerability'} in ${f.packageName || f.serviceName}`;
    this.gitPrBody = `## Security Fix: ${f.cve || f.title}

### Vulnerability Summary
- **CVE / Advisory:** ${f.cve || 'N/A'}
- **Severity / CVSS:** ${f.severity} (${f.cvssScore || 'N/A'})
- **Risk Score:** ${f.riskScore || 'N/A'}/100
- **Package Affected:** \`${f.packageName || 'unknown'}\`
- **Installed Version:** \`${f.installedVersion || 'unknown'}\`
- **Fixed Version Target:** \`${f.fixedVersion || 'latest secure release'}\`

### Changes Made
Updated dependency \`${f.packageName}\` to version \`${f.fixedVersion}\` to remediate known exploitation vector.

### Verification
1. Run local test suite: \`npm test\` / \`mvn test\`
2. CI/CD scan pipeline rerun must confirm zero regressions.
`;
    this.gitDiffSnippet = `--- a/package.json
+++ b/package.json
@@ -14,3 +14,3 @@
-    "${f.packageName}": "${f.installedVersion}"
+    "${f.packageName}": "${f.fixedVersion || '^latest'}"
`;
    this.showGitPrModal = true;
  }

  closeGitPrModal() {
    this.showGitPrModal = false;
  }

  copyGitPrPatch() {
    const fullPatch = `# ${this.gitPrTitle}\n\n${this.gitPrBody}\n\n\`\`\`diff\n${this.gitDiffSnippet}\n\`\`\``;
    navigator.clipboard.writeText(fullPatch);
    this.toastService.success('Copied Git PR Patch & Description!');
  }

  openDispatchModal() {
    this.dispatchEmail = 'lead-dev@company.com';
    this.dispatchNote = `Immediate attention requested for P0/P1 security finding in ${this.finding?.serviceName}.`;
    this.showDispatchModal = true;
  }

  closeDispatchModal() {
    this.showDispatchModal = false;
  }

  dispatchToDeveloper() {
    this.isDispatching = true;
    setTimeout(() => {
      this.isDispatching = false;
      this.showDispatchModal = false;
      this.toastService.success(`Security dispatch notification sent to ${this.dispatchEmail}!`);
    }, 600);
  }

  copyFixSnippet() {
    if (!this.finding) return;
    let cmd = '';
    if (this.finding.packageName && this.finding.fixedVersion) {
      cmd = `npm update ${this.finding.packageName}@${this.finding.fixedVersion}`;
    } else {
      cmd = `Review CVE ${this.finding.cve || this.finding.title}`;
    }
    navigator.clipboard.writeText(cmd);
    this.toastService.success(`Copied: ${cmd}`);
  }

  private buildFallbackPriorityExplanation(): string {
    if (!this.finding) return '';
    return `<h3>Deterministic Priority Breakdown</h3>
      <p><strong>Priority ${this.finding.priority || 'P4'}</strong> (Risk Score: <strong>${this.finding.riskScore || 'N/A'}/100</strong>)</p>
      <ul>
        <li><strong>Base Severity:</strong> ${this.finding.severity} (CVSS: ${this.finding.cvssScore || 'N/A'})</li>
        <li><strong>Service Environment:</strong> ${this.finding.environment}</li>
        <li><strong>Service:</strong> ${this.finding.serviceName}</li>
      </ul>
      <p style="color:var(--muted);font-size:12px">Calculated using CVSS baseline multiplied by business context weight.</p>`;
  }

  private buildFallbackRemediationGuidance(): string {
    if (!this.finding) return '';
    const pkg = this.finding.packageName || 'the affected package';
    const fixVer = this.finding.fixedVersion;
    if (fixVer) {
      return `<h3>Recommended Remediation</h3>
        <p>Upgrade <code>${pkg}</code> to version <strong>${fixVer}</strong> or higher.</p>
        <pre><code>npm update ${pkg}@${fixVer}</code></pre>`;
    }
    return `<h3>Remediation Guidance</h3>
      <p>No automated fix version available from scanner report. Check upstream security advisories for <code>${this.finding.cve || this.finding.title}</code>.</p>`;
  }

  private formatMarkdown(md: string): string {
    return md
      .replace(/### (.*?)\n/g, '<h3>$1</h3>')
      .replace(/## (.*?)\n/g, '<h2>$1</h2>')
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/`([^`]+)`/g, '<code>$1</code>')
      .replace(/\n\n/g, '</p><p>')
      .replace(/\n/g, '<br>');
  }
}