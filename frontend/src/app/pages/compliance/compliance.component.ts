import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { ToastService } from '../../services/toast.service';

interface ComplianceControl {
  framework: 'OWASP' | 'SOC2' | 'PCI-DSS';
  id: string;
  name: string;
  status: 'COMPLIANT' | 'NEEDS_ATTENTION' | 'NON_COMPLIANT';
  findingCount: number;
  description: string;
}

@Component({
  selector: 'app-compliance',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './compliance.component.html',
  styleUrls: ['./compliance.component.css']
})
export class ComplianceComponent implements OnInit {
  loading = true;
  totalFindings = 0;
  p0Count = 0;
  p1Count = 0;
  complianceScore = 88;

  controls: ComplianceControl[] = [];
  activeFramework: 'ALL' | 'OWASP' | 'SOC2' | 'PCI-DSS' = 'ALL';

  constructor(
    private apiService: ApiService,
    private toastService: ToastService
  ) {}

  ngOnInit() {
    this.loadComplianceData();
  }

  loadComplianceData() {
    this.loading = true;
    this.apiService.getAllFindings().subscribe({
      next: (findings) => {
        const list = findings || [];
        this.totalFindings = list.length;
        this.p0Count = list.filter(f => f.priority === 'P0').length;
        this.p1Count = list.filter(f => f.priority === 'P1').length;

        // Calculate dynamic compliance score
        if (this.p0Count > 5) {
          this.complianceScore = 65;
        } else if (this.p0Count > 0) {
          this.complianceScore = 78;
        } else if (this.p1Count > 10) {
          this.complianceScore = 85;
        } else {
          this.complianceScore = 94;
        }

        // Build compliance controls based on findings
        const vulnComponentFindings = list.filter(f => f.packageName != null).length;
        const secretFindings = list.filter(f => (f.title && f.title.toLowerCase().includes('secret')) || (f.cve && f.cve.includes('KEY'))).length;
        const highCvssFindings = list.filter(f => f.cvssScore && f.cvssScore >= 7.0).length;

        this.controls = [
          {
            framework: 'OWASP',
            id: 'A06:2021',
            name: 'Vulnerable & Outdated Components',
            status: this.p0Count > 0 ? 'NON_COMPLIANT' : (vulnComponentFindings > 0 ? 'NEEDS_ATTENTION' : 'COMPLIANT'),
            findingCount: vulnComponentFindings,
            description: 'Application components, OS packages, and third-party libraries must be scanned and kept up to date.'
          },
          {
            framework: 'OWASP',
            id: 'A02:2021',
            name: 'Cryptographic Failures',
            status: list.some(f => f.title && f.title.toLowerCase().includes('crypto')) ? 'NEEDS_ATTENTION' : 'COMPLIANT',
            findingCount: list.filter(f => f.title && f.title.toLowerCase().includes('crypto')).length,
            description: 'Sensitive data transmission and storage must employ modern authenticated encryption.'
          },
          {
            framework: 'OWASP',
            id: 'A07:2021',
            name: 'Identification & Authentication Failures',
            status: secretFindings > 0 ? 'NON_COMPLIANT' : 'COMPLIANT',
            findingCount: secretFindings,
            description: 'Hardcoded credentials, API keys, and authentication token exposure prevention.'
          },
          {
            framework: 'SOC2',
            id: 'CC6.8',
            name: 'Vulnerability Detection & Patch Management',
            status: this.p0Count > 0 ? 'NON_COMPLIANT' : 'COMPLIANT',
            findingCount: this.p0Count + this.p1Count,
            description: 'System prevents the installation of unauthorized software and patches known critical vulnerabilities within SLA.'
          },
          {
            framework: 'SOC2',
            id: 'CC7.1',
            name: 'Vulnerability Scanning & Continuous Monitoring',
            status: 'COMPLIANT',
            findingCount: 0,
            description: 'Continuous CI/CD pipeline scans are enforced across all services with daily executive visibility.'
          },
          {
            framework: 'PCI-DSS',
            id: 'Req 6.3.3',
            name: 'Patching Critical Vulnerabilities within 30 Days',
            status: this.p0Count > 0 ? 'NON_COMPLIANT' : (highCvssFindings > 0 ? 'NEEDS_ATTENTION' : 'COMPLIANT'),
            findingCount: highCvssFindings,
            description: 'All critical and high-severity security patches are installed within 30 days of release.'
          },
          {
            framework: 'PCI-DSS',
            id: 'Req 6.5.1',
            name: 'Common Coding Vulnerability Prevention',
            status: list.filter(f => f.severity === 'CRITICAL' || f.severity === 'HIGH').length > 5 ? 'NEEDS_ATTENTION' : 'COMPLIANT',
            findingCount: list.filter(f => f.severity === 'CRITICAL' || f.severity === 'HIGH').length,
            description: 'Software development life cycle includes automated SAST and container security analysis.'
          }
        ];

        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  get filteredControls(): ComplianceControl[] {
    if (this.activeFramework === 'ALL') {
      return this.controls;
    }
    return this.controls.filter(c => c.framework === this.activeFramework);
  }

  printAuditReport() {
    window.print();
    this.toastService.info('Opening print dialog for Executive Audit Brief...');
  }
}