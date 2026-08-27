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

  // Quick actions
  quickActions = [
    { label: 'Explain Priority', action: 'explain_priority', icon: '🔍' },
    { label: 'Remediation Guidance', action: 'remediation_guidance', icon: '🛠️' },
    { label: 'Service Risk Summary', action: 'service_risk', icon: '📊' },
    { label: 'Daily Security Brief', action: 'daily_brief', icon: '📋' }
  ];

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
    // Load findings and services for dropdowns
    this.apiService.getAllFindings().subscribe({
      next: (findings) => {
        this.availableFindings = findings.slice(0, 50); // Limit to 50 for performance
        this.loadingData = false;
      },
      error: (err) => {
        console.error('Error loading findings:', err);
        this.loadingData = false;
      }
    });

    this.apiService.getAllServices().subscribe({
      next: (services) => {
        this.availableServices = services;
        this.runRequestedAction();
      },
      error: (err) => {
        console.error('Error loading services:', err);
      }
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
      error: (err) => {
        this.error = 'Failed to check AI configuration';
        this.loading = false;
        console.error('Error checking AI configuration:', err);
      }
    });
  }

  executeQuickAction(action: string): void {
    this.isProcessing = true;
    this.addChatMessage('user', `Execute: ${action}`);

    switch (action) {
      case 'explain_priority':
        if (this.selectedFindingId) {
          this.executeExplainPriority();
        } else {
          this.addChatMessage('system', 'Please select a finding ID first.');
          this.isProcessing = false;
        }
        break;
      case 'remediation_guidance':
        if (this.selectedFindingId) {
          this.executeRemediationGuidance();
        } else {
          this.addChatMessage('system', 'Please select a finding ID first.');
          this.isProcessing = false;
        }
        break;
      case 'service_risk':
        if (this.selectedServiceId) {
          this.executeServiceRiskSummary();
        } else {
          this.addChatMessage('system', 'Please select a service ID first.');
          this.isProcessing = false;
        }
        break;
      case 'daily_brief':
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
      error: (err) => {
        this.addChatMessage('system', 'Failed to get priority explanation: ' + this.formatError(err));
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
      error: (err) => {
        this.addChatMessage('system', 'Failed to get remediation guidance: ' + this.formatError(err));
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
      error: (err) => {
        this.addChatMessage('system', 'Failed to get service risk summary: ' + this.formatError(err));
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
      error: (err) => {
        this.addChatMessage('system', 'Failed to generate daily security brief: ' + this.formatError(err));
        this.isProcessing = false;
      }
    });
  }

  sendChatMessage(): void {
    if (!this.chatMessage.trim()) {
      return;
    }

    this.addChatMessage('user', this.chatMessage);
    this.isProcessing = true;

    const question = this.chatMessage.trim();
    this.apiService.askAi(question, this.selectedServiceId || undefined, this.selectedFindingId || undefined).subscribe({
      next: (response) => {
        const message = this.formatResponse(response);
        this.addChatMessage('assistant', message);
        this.isProcessing = false;
        this.chatMessage = '';
      },
      error: (err) => {
        this.addChatMessage('system', 'Failed to process message: ' + this.formatError(err));
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
  }

  clearChat(): void {
    this.chatHistory = [];
  }

  formatTime(date: Date): string {
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  formatResponse(response: any): string {
    if (typeof response === 'string') {
      // Try to parse JSON if it's a JSON string
      try {
        const parsed = JSON.parse(response);
        return this.formatResponse(parsed);
      } catch (e) {
        // Not JSON, return as-is with markdown formatting
        return this.formatMarkdown(response);
      }
    }
    if (response && response.steps && Array.isArray(response.steps)) {
      const output = response.steps.find((step: any) => step.content)?.content;
      const text = Array.isArray(output) ? output.find((item: any) => item.text)?.text : output;
      if (text) return this.formatMarkdown(text);
    }
    if (response && response.answer) {
      return this.formatMarkdown(response.answer);
    }
    if (response && response.content && typeof response.content === 'string') {
      return this.formatMarkdown(response.content);
    }
    if (response && response.message) {
      return this.formatMarkdown(response.message);
    }
    if (response && response.explanation) {
      return this.formatMarkdown(response.explanation);
    }
    if (response && response.guidance) {
      return this.formatMarkdown(response.guidance);
    }
    // Handle object responses by converting to readable format
    if (typeof response === 'object') {
      try {
        return this.formatMarkdown(JSON.stringify(response, null, 2));
      } catch (e) {
        return 'Received complex response. Please check the console for details.';
      }
    }
    return this.formatMarkdown(String(response));
  }

  formatMarkdown(text: string): string {
    if (!text) return '';
    
    // Convert markdown to HTML for display
    let formatted = text
      // Headers
      .replace(/^### (.*$)/gim, '<h3>$1</h3>')
      .replace(/^## (.*$)/gim, '<h2>$1</h2>')
      .replace(/^# (.*$)/gim, '<h1>$1</h1>')
      // Bold
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      // Italic
      .replace(/\*(.*?)\*/g, '<em>$1</em>')
      // Code blocks
      .replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
      // Inline code
      .replace(/`([^`]+)`/g, '<code>$1</code>')
      // Lists
      .replace(/^\s*-\s(.*$)/gim, '<li>$1</li>')
      .replace(/^\s*\d+\.\s(.*$)/gim, '<li>$1</li>')
      // Line breaks
      .replace(/\n/g, '<br>');
    
    // Wrap lists
    formatted = formatted.replace(/(<li>.*<\/li>)/g, '<ul>$1</ul>');
    
    return formatted;
  }

  formatError(error: any): string {
    if (error && error.message) {
      return error.message;
    }
    if (error && error.error && error.error.message) {
      return error.error.message;
    }
    return 'Unknown error occurred';
  }

  getFindingLabel(finding: any): string {
    const cve = finding.cve || 'No CVE';
    const title = finding.title || finding.packageName || 'Unknown';
    const severity = finding.severity || 'Unknown';
    return `${cve} - ${title} (${severity})`;
  }

  getServiceLabel(service: any): string {
    return `${service.serviceName} - ${service.environment}`;
  }
}