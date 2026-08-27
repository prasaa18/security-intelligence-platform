import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-ai-assistant',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ai-assistant.component.html',
  styleUrls: ['./ai-assistant.component.css']
})
export class AiAssistantComponent implements OnInit {
  isConfigured = false;
  aiMode = 'SECURITY_ANALYST';
  aiModel = '';
  loading = true;
  error: string | null = null;

  // Chat interface
  selectedFindingId = '';
  selectedServiceId = '';
  requestedAction = '';
  chatMessage = '';
  chatHistory: { role: string; message: string | SafeHtml; timestamp: Date }[] = [];
  isProcessing = false;

  // Available data for dropdowns
  availableFindings: any[] = [];
  availableServices: any[] = [];
  loadingData = false;

  constructor(private apiService: ApiService, private sanitizer: DomSanitizer, private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.selectedFindingId = this.route.snapshot.queryParamMap.get('findingId') || '';
    this.selectedServiceId = this.route.snapshot.queryParamMap.get('serviceId') || '';
    this.requestedAction = this.route.snapshot.queryParamMap.get('action') || '';
    this.checkConfiguration();
    this.loadAvailableData();
  }

  loadAvailableData(): void {
    this.loadingData = true;
    this.apiService.getAllFindings().subscribe({
      next: (findings) => {
        this.availableFindings = findings.slice(0, 50);
        this.loadingData = false;
      },
      error: () => {
        this.loadingData = false;
      }
    });

    this.apiService.getAllServices().subscribe({
      next: (services) => {
        this.availableServices = services;
        this.runRequestedAction();
      },
      error: () => {}
    });
  }

  private runRequestedAction(): void {
    if (this.requestedAction === 'priority' && this.selectedFindingId) this.executeQuickAction('explain_priority');
    if (this.requestedAction === 'remediation' && this.selectedFindingId) this.executeQuickAction('remediation_guidance');
    if (this.requestedAction === 'service' && this.selectedServiceId) this.executeQuickAction('service_risk');
  }

  checkConfiguration(): void {
    this.apiService.getAiStatus().subscribe({
      next: (status) => {
        this.isConfigured = status.configured;
        this.aiMode = status.mode;
        this.aiModel = status.model;
        this.loading = false;
      },
      error: () => {
        this.isConfigured = false;
        this.loading = false;
      }
    });
  }

  executeQuickAction(action: string): void {
    this.isProcessing = true;

    switch (action) {
      case 'explain_priority':
        if (this.selectedFindingId) {
          const finding = this.availableFindings.find(f => f.id === this.selectedFindingId);
          this.addChatMessage('user', `Explain priority for finding: ${finding ? (finding.cve || finding.title) : this.selectedFindingId}`);
          this.executeExplainPriority();
        } else {
          this.addChatMessage('user', 'Explain priority calculation & scoring model');
          this.addChatMessage('assistant', this.formatMarkdown(`### Security Intelligence Prioritization Model
Priorities are calculated deterministically:
* **Base Score:** Derived from Severity (CRITICAL: 70, HIGH: 55, MEDIUM: 35, LOW: 15) + CVSS Score (0-10).
* **Business Multipliers:** Production environment (+15), Internet-exposed asset (+20), Critical Business Criticality (+20), Sensitive Data (+10).
* **Priority Mapping:**
  - **P0 (Score 90–100):** Fix Immediately (Fix Today).
  - **P1 (Score 75–89):** Due this sprint/week.
  - **P2 (Score 55–74):** Standard remediation queue.
  - **P3–P4 (Score 0–54):** Backlog / Informational.`));
          this.isProcessing = false;
        }
        break;

      case 'remediation_guidance':
        if (this.selectedFindingId) {
          const finding = this.availableFindings.find(f => f.id === this.selectedFindingId);
          this.addChatMessage('user', `Provide remediation guidance for: ${finding ? (finding.cve || finding.title) : this.selectedFindingId}`);
          this.executeRemediationGuidance();
        } else {
          this.addChatMessage('user', 'How do we remediate security findings effectively?');
          this.addChatMessage('assistant', this.formatMarkdown(`### Remediation Best Practices
1. **Identify Fixed Version:** Check if scanner (Trivy/Snyk) provides a \`fixedVersion\` for the vulnerable package.
2. **Upgrade & Verify:** Update package manifest (e.g. \`pom.xml\`, \`package.json\`, \`Dockerfile\`).
3. **Re-run Pipeline Scan:** Trigger CI scan. If the vulnerability is resolved, our deduplication engine automatically transitions state to \`NOT_DETECTED_IN_LATEST_SCAN\` and marks the item resolved.`));
          this.isProcessing = false;
        }
        break;

      case 'service_risk':
        if (this.selectedServiceId) {
          const service = this.availableServices.find(s => s.id === this.selectedServiceId);
          this.addChatMessage('user', `Assess risk for service: ${service ? service.serviceName : this.selectedServiceId}`);
          this.executeServiceRiskSummary();
        } else {
          this.addChatMessage('system', 'Please select a service asset from the dropdown first.');
          this.isProcessing = false;
        }
        break;

      case 'daily_brief':
        this.addChatMessage('user', 'Generate Daily Security Brief');
        this.executeDailySecurityBrief();
        break;
    }
  }

  executeExplainPriority(): void {
    this.apiService.explainPriority(this.selectedFindingId).subscribe({
      next: (response) => {
        const message = this.formatResponse(response);
        this.addChatMessage('assistant', message);
        this.isProcessing = false;
      },
      error: () => {
        const finding = this.availableFindings.find(f => f.id === this.selectedFindingId);
        const fb = finding 
          ? `### Deterministic Risk Breakdown for ${finding.cve || finding.title}
* **Assigned Priority:** ${finding.priority || 'P4'} (Risk Score: ${finding.riskScore || 'N/A'}/100)
* **Severity:** ${finding.severity} (CVSS: ${finding.cvssScore || 'N/A'})
* **Service:** ${finding.serviceName} (${finding.environment})
* **Reasons:** ${(finding.priorityReasons || ['Rule-based deterministic scoring']).join(', ')}`
          : 'Priority analysis based on deterministic rules.';
        this.addChatMessage('assistant', this.formatMarkdown(fb));
        this.isProcessing = false;
      }
    });
  }

  executeRemediationGuidance(): void {
    this.apiService.generateRemediationGuidance(this.selectedFindingId).subscribe({
      next: (response) => {
        const message = this.formatResponse(response);
        this.addChatMessage('assistant', message);
        this.isProcessing = false;
      },
      error: () => {
        const finding = this.availableFindings.find(f => f.id === this.selectedFindingId);
        const pkg = finding?.packageName || 'vulnerable dependency';
        const fix = finding?.fixedVersion || 'patched version';
        const fb = `### Remediation Steps for ${finding?.cve || 'selected finding'}
1. **Package Upgrade:** Upgrade \`${pkg}\` to version \`${fix}\`.
2. **Re-scan:** Trigger security scan (Trivy / Snyk) via GitHub Actions or manual upload.
3. **Verify Resolution:** The finding will automatically clear upon next scan comparison.`;
        this.addChatMessage('assistant', this.formatMarkdown(fb));
        this.isProcessing = false;
      }
    });
  }

  executeServiceRiskSummary(): void {
    this.apiService.generateServiceRiskSummary(this.selectedServiceId).subscribe({
      next: (response) => {
        const message = this.formatResponse(response);
        this.addChatMessage('assistant', message);
        this.isProcessing = false;
      },
      error: () => {
        const svc = this.availableServices.find(s => s.id === this.selectedServiceId);
        const fb = `### Service Risk Summary: ${svc ? svc.serviceName : 'Service'}
* **Environment:** ${svc?.environment || 'PRODUCTION'}
* **Criticality:** ${svc?.businessCriticality || 'MEDIUM'}
* **Internet Exposed:** ${svc?.internetExposed ? 'Yes (Public)' : 'No (Internal)'}
* **Data Sensitivity:** ${svc?.dataSensitivity || 'INTERNAL'}`;
        this.addChatMessage('assistant', this.formatMarkdown(fb));
        this.isProcessing = false;
      }
    });
  }

  executeDailySecurityBrief(): void {
    this.apiService.generateDailySecurityBrief().subscribe({
      next: (response) => {
        const message = this.formatResponse(response);
        this.addChatMessage('assistant', message);
        this.isProcessing = false;
      },
      error: () => {
        const p0Count = this.availableFindings.filter(f => f.priority === 'P0').length;
        const p1Count = this.availableFindings.filter(f => f.priority === 'P1').length;
        const fb = `### Daily Security Brief
* **Immediate Attention:** **${p0Count}** P0 findings require resolution today.
* **This Week:** **${p1Count}** P1 findings due for remediation.
* **Scan Health:** Continuous monitoring active across configured services.`;
        this.addChatMessage('assistant', this.formatMarkdown(fb));
        this.isProcessing = false;
      }
    });
  }

  askSamplePrompt(prompt: string): void {
    this.chatMessage = prompt;
    this.sendChatMessage();
  }

  sendChatMessage(): void {
    if (!this.chatMessage.trim()) return;

    const question = this.chatMessage.trim();
    this.addChatMessage('user', question);
    this.chatMessage = '';
    this.isProcessing = true;

    this.apiService.askAi(question, this.selectedServiceId || undefined, this.selectedFindingId || undefined).subscribe({
      next: (response) => {
        const message = this.formatResponse(response);
        this.addChatMessage('assistant', message);
        this.isProcessing = false;
      },
      error: () => {
        // Deterministic intelligent answer if AI key is missing or exhausted
        const lower = question.toLowerCase();
        let fallback = '';
        if (lower.includes('p0') || lower.includes('priority')) {
          fallback = `### P0 Priority Guidance
P0 items represent critical risk on production / high-criticality assets. These must be addressed before standard feature work. Check the **Remediation Plan** tab for step-by-step upgrade instructions.`;
        } else if (lower.includes('trivy') || lower.includes('verify') || lower.includes('fixed')) {
          fallback = `### Verification Workflow
When you push updated code or upload a new scan report, our **Scan Comparison Engine** computes deltas against previous fingerprints. Any CVE no longer detected transitions to \`NOT_DETECTED_IN_LATEST_SCAN\` and the corresponding remediation item is closed automatically.`;
        } else if (lower.includes('cvss') || lower.includes('score')) {
          fallback = `### CVSS vs Business Risk Score
* **CVSS:** Measures technical vulnerability severity in isolation (0–10).
* **Business Risk Score (0–100):** Combines CVSS with asset context: Production environment (+15), Internet exposure (+20), Business Criticality (+20), and Data Sensitivity (+10).`;
        } else {
          fallback = `### Security Assistant Guidance
I am operating with grounded security intelligence from your live scanners. To explore specific findings, select a finding from the dropdown or visit the **Remediation Plan** page.`;
        }
        this.addChatMessage('assistant', this.formatMarkdown(fallback));
        this.isProcessing = false;
      }
    });
  }

  addChatMessage(role: string, message: string): void {
    const formattedMessage = role === 'assistant' ? this.formatResponse(message) : message;
    this.chatHistory.push({
      role,
      message: role === 'assistant' ? this.sanitizer.bypassSecurityTrustHtml(formattedMessage) : formattedMessage,
      timestamp: new Date()
    });
    setTimeout(() => {
      const feed = document.getElementById('chatFeed');
      if (feed) feed.scrollTop = feed.scrollHeight;
    }, 100);
  }

  clearChat(): void {
    this.chatHistory = [];
  }

  formatTime(date: Date): string {
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  formatResponse(response: any): string {
    if (typeof response === 'string') {
      try {
        const parsed = JSON.parse(response);
        return this.formatResponse(parsed);
      } catch (e) {
        return this.formatMarkdown(response);
      }
    }
    if (response && response.steps && Array.isArray(response.steps)) {
      const output = response.steps.find((step: any) => step.content)?.content;
      const text = Array.isArray(output) ? output.find((item: any) => item.text)?.text : output;
      if (text) return this.formatMarkdown(text);
    }
    if (response && response.answer) return this.formatMarkdown(response.answer);
    if (response && response.message) return this.formatMarkdown(response.message);
    if (response && response.explanation) return this.formatMarkdown(response.explanation);
    if (response && response.guidance) return this.formatMarkdown(response.guidance);
    if (typeof response === 'object') {
      try {
        return this.formatMarkdown(JSON.stringify(response, null, 2));
      } catch (e) {
        return 'Received complex response.';
      }
    }
    return this.formatMarkdown(String(response));
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
      .replace(/^\s*\*\s(.*$)/gim, '<li>$1</li>')
      .replace(/^\s*-\s(.*$)/gim, '<li>$1</li>')
      .replace(/^\s*\d+\.\s(.*$)/gim, '<li>$1</li>')
      .replace(/\n/g, '<br>')
      .replace(/(<li>.*<\/li>)/g, '<ul>$1</ul>');
  }

  getFindingLabel(finding: any): string {
    const cve = finding.cve || 'No CVE';
    const title = finding.title || finding.packageName || 'Unknown';
    const sev = finding.severity || 'Unknown';
    const svc = finding.serviceName || '';
    return `${cve} · ${svc} (${sev})`;
  }

  getServiceLabel(service: any): string {
    return `${service.serviceName} (${service.environment})`;
  }
}