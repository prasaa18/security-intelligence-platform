import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
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
  loading = true;
  error: string | null = null;

  // Chat interface
  selectedFindingId = '';
  selectedServiceId = '';
  chatMessage = '';
  chatHistory: { role: string; message: string; timestamp: Date }[] = [];
  isProcessing = false;

  // Quick actions
  quickActions = [
    { label: 'Explain Priority', action: 'explain_priority', icon: '🔍' },
    { label: 'Remediation Guidance', action: 'remediation_guidance', icon: '🛠️' },
    { label: 'Service Risk Summary', action: 'service_risk', icon: '📊' },
    { label: 'Daily Security Brief', action: 'daily_brief', icon: '📋' }
  ];

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    this.checkConfiguration();
  }

  checkConfiguration(): void {
    this.apiService.isAiConfigured().subscribe({
      next: (configured) => {
        this.isConfigured = configured;
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
    if (!this.isConfigured) {
      this.addChatMessage('system', 'AI is not configured. Please set GEMINI_API_KEY in backend environment variables.');
      return;
    }

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
        this.addChatMessage('assistant', response);
        this.isProcessing = false;
      },
      error: (err) => {
        this.addChatMessage('system', 'Failed to get priority explanation: ' + err.message);
        this.isProcessing = false;
      }
    });
  }

  executeRemediationGuidance(): void {
    this.apiService.generateRemediationGuidance(this.selectedFindingId).subscribe({
      next: (response) => {
        this.addChatMessage('assistant', response);
        this.isProcessing = false;
      },
      error: (err) => {
        this.addChatMessage('system', 'Failed to get remediation guidance: ' + err.message);
        this.isProcessing = false;
      }
    });
  }

  executeServiceRiskSummary(): void {
    this.apiService.generateServiceRiskSummary(this.selectedServiceId).subscribe({
      next: (response) => {
        this.addChatMessage('assistant', response);
        this.isProcessing = false;
      },
      error: (err) => {
        this.addChatMessage('system', 'Failed to get service risk summary: ' + err.message);
        this.isProcessing = false;
      }
    });
  }

  executeDailySecurityBrief(): void {
    this.apiService.generateDailySecurityBrief().subscribe({
      next: (response) => {
        this.addChatMessage('assistant', response);
        this.isProcessing = false;
      },
      error: (err) => {
        this.addChatMessage('system', 'Failed to generate daily security brief: ' + err.message);
        this.isProcessing = false;
      }
    });
  }

  sendChatMessage(): void {
    if (!this.chatMessage.trim() || !this.isConfigured) {
      return;
    }

    this.addChatMessage('user', this.chatMessage);
    this.isProcessing = true;

    // For custom chat messages, we'll use the daily brief as a fallback
    // In a real implementation, you'd have a more sophisticated chat endpoint
    this.apiService.generateDailySecurityBrief().subscribe({
      next: (response) => {
        this.addChatMessage('assistant', response);
        this.isProcessing = false;
        this.chatMessage = '';
      },
      error: (err) => {
        this.addChatMessage('system', 'Failed to process message: ' + err.message);
        this.isProcessing = false;
      }
    });
  }

  addChatMessage(role: string, message: string): void {
    this.chatHistory.push({
      role,
      message,
      timestamp: new Date()
    });
  }

  clearChat(): void {
    this.chatHistory = [];
  }

  formatTime(date: Date): string {
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }
}