import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ApiService } from '../../services/api.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-ingestion-studio',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './ingestion-studio.component.html',
  styleUrls: ['./ingestion-studio.component.css']
})
export class IngestionStudioComponent implements OnInit {
  serviceName = 'payment-service';
  environment = 'PRODUCTION';
  branch = 'main';
  commitId = '9fa81b2';
  workflowRunId = 'run-88219';
  selectedTemplate = 'trivy';

  rawPayload = '';
  isSubmitting = false;
  ingestionResponse: any = null;
  error: string | null = null;

  servicesList: any[] = [];

  constructor(
    private apiService: ApiService,
    private http: HttpClient,
    private toastService: ToastService,
    private router: Router
  ) {}

  ngOnInit() {
    this.apiService.getAllServices().subscribe({
      next: (svcs) => { this.servicesList = svcs || []; }
    });
    this.loadTemplate('trivy');
  }

  loadTemplate(type: string) {
    this.selectedTemplate = type;
    if (type === 'trivy') {
      this.rawPayload = JSON.stringify({
        "SchemaVersion": 2,
        "ArtifactName": "payment-service:v2.4.1",
        "ArtifactType": "container_image",
        "Metadata": {
          "OS": { "Family": "alpine", "Name": "3.18.2" }
        },
        "Results": [
          {
            "Target": "payment-service:v2.4.1 (alpine 3.18.2)",
            "Class": "os-pkgs",
            "Type": "alpine",
            "Vulnerabilities": [
              {
                "VulnerabilityID": "CVE-2023-44487",
                "PkgName": "nghttp2",
                "InstalledVersion": "1.55.1-r0",
                "FixedVersion": "1.55.1-r1",
                "Severity": "CRITICAL",
                "Title": "HTTP/2 Rapid Reset Attack (Denial of Service)",
                "Description": "The HTTP/2 protocol allows a client to trigger a stream reset which leads to excessive CPU and resource exhaustion on target services.",
                "PrimaryURL": "https://nvd.nist.gov/vuln/detail/CVE-2023-44487",
                "CVSS": { "nvd": { "V3Score": 7.5 } }
              },
              {
                "VulnerabilityID": "CVE-2024-21626",
                "PkgName": "runc",
                "InstalledVersion": "1.1.11-r0",
                "FixedVersion": "1.1.12-r0",
                "Severity": "CRITICAL",
                "Title": "Container breakout through file descriptor leak",
                "Description": "runc contains an internal file descriptor leak allowing host filesystem access during container execution.",
                "PrimaryURL": "https://nvd.nist.gov/vuln/detail/CVE-2024-21626",
                "CVSS": { "nvd": { "V3Score": 8.6 } }
              }
            ]
          }
        ]
      }, null, 2);
    } else if (type === 'snyk') {
      this.rawPayload = JSON.stringify({
        "vulnerabilities": [
          {
            "id": "SNYK-JAVA-ORGSPRINGFRAMEWORK-7913328",
            "title": "Path Traversal in Spring Framework Resource Handling",
            "packageName": "org.springframework:spring-webmvc",
            "version": "6.1.13",
            "severity": "high",
            "identifiers": {
              "CVE": ["CVE-2024-38816"]
            },
            "fixedIn": ["6.1.14"],
            "cvssScore": 7.5
          }
        ],
        "projectName": "payment-service",
        "displayTargetFile": "pom.xml"
      }, null, 2);
    } else if (type === 'gitleaks') {
      this.rawPayload = JSON.stringify([
        {
          "Description": "AWS Access Key ID",
          "StartLine": 42,
          "EndLine": 42,
          "StartColumn": 12,
          "EndColumn": 32,
          "Match": "AKIAIOSFODNN7EXAMPLE",
          "Secret": "AKIAIOSFODNN7EXAMPLE",
          "File": "src/main/resources/application.properties",
          "Commit": "9fa81b2",
          "Author": "dev-user",
          "Email": "dev@company.com",
          "Date": "2026-09-07T08:00:00Z",
          "Message": "Add payment gateway config",
          "RuleID": "aws-access-key-id"
        }
      ], null, 2);
    }
  }

  submitIngestion() {
    this.isSubmitting = true;
    this.error = null;
    this.ingestionResponse = null;

    const fileBlob = new Blob([this.rawPayload], { type: 'application/json' });
    const formData = new FormData();
    formData.append('file', fileBlob, `${this.selectedTemplate}-report.json`);
    formData.append('serviceName', this.serviceName);
    formData.append('environment', this.environment);
    formData.append('branch', this.branch);
    formData.append('commitId', this.commitId);
    formData.append('workflowRunId', this.workflowRunId);
    formData.append('token', 'test-token-123');

    this.http.post<any>('/api/integrations/scans/github-actions', formData).subscribe({
      next: (res) => {
        this.ingestionResponse = res;
        this.isSubmitting = false;
        this.toastService.success(`Ingested scan report for ${this.serviceName}!`);
      },
      error: (err) => {
        this.error = 'Failed to ingest scan: ' + (err.error?.message || err.message);
        this.isSubmitting = false;
        this.toastService.error('Ingestion failed');
      }
    });
  }
}