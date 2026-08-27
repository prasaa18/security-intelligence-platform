import { Component, OnInit } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
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

  // AI Guidance state
  aiGuidance: SafeHtml | null = null;
  loadingAi = false;
  aiActionName = '';

  constructor(
    private route: ActivatedRoute,
    private apiService: ApiService,
    private location: Location,
    private sanitizer: DomSanitizer
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
        console.error('Finding detail error:', err);
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

  private buildFallbackPriorityExplanation(): string {
    if (!this.finding) return '';
    return `<h3>Deterministic Priority Breakdown</h3>
      <p><strong>Priority ${this.finding.priority || 'P4'}</strong> (Risk Score: <strong>${this.finding.riskScore || 'N/A'}/100</strong>)</p>
      <ul>
        <li><strong>Base Severity:</strong> ${this.finding.severity} (CVSS: ${this.finding.cvssScore || 'N/A'})</li>
        <li><strong>Service Environment:</strong> ${this.finding.environment}</li>
        <li><strong>Service:</strong> ${this.finding.serviceName}</li>
      </ul>
      <p><em>Note: Rule-based deterministic risk assessment. Add or configure GEMINI_API_KEY for dynamic contextual analysis.</em></p>`;
  }

  private buildFallbackRemediationGuidance(): string {
    if (!this.finding) return '';
    const pkg = this.finding.packageName || 'the vulnerable package';
    const curVer = this.finding.installedVersion || 'current version';
    const fixVer = this.finding.fixedVersion || 'latest patched version';
    return `<h3>Remediation Steps for ${this.finding.cve || 'this vulnerability'}</h3>
      <ol>
        <li><strong>Upgrade Dependency:</strong> Update <code>${pkg}</code> from <code>${curVer}</code> to <code>${fixVer}</code>.</li>
        <li><strong>Verification:</strong> Re-run scanner (<code>Trivy</code> / <code>Snyk</code>) to confirm the fingerprint is resolved.</li>
        <li><strong>Impact Check:</strong> Run regression/integration tests on <code>${this.finding.serviceName}</code>.</li>
      </ol>`;
  }

  formatMarkdown(text: string): string {
    if (!text) return '';
    return text
      .replace(/^### (.*$)/gim, '<h3>$1</h3>')
      .replace(/^## (.*$)/gim, '<h2>$1</h2>')
      .replace(/^# (.*$)/gim, '<h1>$1</h1>')
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.*?)\*/g, '<em>$1</em>')
      .replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
      .replace(/`([^`]+)`/g, '<code>$1</code>')
      .replace(/^\s*-\s(.*$)/gim, '<li>$1</li>')
      .replace(/^\s*\d+\.\s(.*$)/gim, '<li>$1</li>')
      .replace(/\n/g, '<br>')
      .replace(/(<li>.*<\/li>)/g, '<ul>$1</ul>');
  }

  getSeverityBadgeClass(severity: string): string {
    return 'badge-' + (severity || 'unknown').toLowerCase();
  }

  getPriorityBadgeClass(priority: string): string {
    return 'badge-' + (priority || 'p4').toLowerCase();
  }

  getToolBadgeClass(tool: string): string {
    if (!tool) return 'badge-other';
    const t = tool.toUpperCase();
    if (t === 'TRIVY') return 'badge-trivy';
    if (t === 'SNYK')  return 'badge-snyk';
    return 'badge-other';
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return '—';
    return new Date(dateString).toLocaleString('en-US', { month: 'short', day: 'numeric', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  }

  getSourceTools(): string[] {
    if (!this.finding?.sourceFindings) return [];
    return this.finding.sourceFindings.map(sf => sf.split(':')[0]);
  }
}