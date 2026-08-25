import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DashboardSummary, SecurityFinding, ScanReport, ServiceModel } from '../models/dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) { }

  // Dashboard
  getDashboardSummary(): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(`${this.baseUrl}/dashboard/summary`);
  }

  getActionCenterDashboard(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/dashboard/action-center`);
  }

  // Findings
  getAllFindings(): Observable<SecurityFinding[]> {
    return this.http.get<SecurityFinding[]>(`${this.baseUrl}/findings`);
  }

  getFindingById(id: string): Observable<SecurityFinding> {
    return this.http.get<SecurityFinding>(`${this.baseUrl}/findings/${id}`);
  }

  getFindingsByService(serviceName: string): Observable<SecurityFinding[]> {
    return this.http.get<SecurityFinding[]>(`${this.baseUrl}/findings/service/${serviceName}`);
  }

  getFindingsByPriority(priority: string): Observable<SecurityFinding[]> {
    return this.http.get<SecurityFinding[]>(`${this.baseUrl}/findings/priority/${priority}`);
  }

  getTopPriorityFindings(limit: number = 10): Observable<SecurityFinding[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<SecurityFinding[]>(`${this.baseUrl}/findings/prioritized`, { params });
  }

  searchFindings(query: string): Observable<SecurityFinding[]> {
    const params = new HttpParams().set('query', query);
    return this.http.get<SecurityFinding[]>(`${this.baseUrl}/findings/search`, { params });
  }

  // Reports (Scans)
  getAllReports(): Observable<ScanReport[]> {
    return this.http.get<ScanReport[]>(`${this.baseUrl}/reports`);
  }

  getReportById(id: string): Observable<ScanReport> {
    return this.http.get<ScanReport>(`${this.baseUrl}/reports/${id}`);
  }

  uploadReport(file: File, serviceName: string, environment: string): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('serviceName', serviceName);
    formData.append('environment', environment);
    return this.http.post(`${this.baseUrl}/reports/upload`, formData);
  }

  // Scan Executions
  getAllScanExecutions(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/scans`);
  }

  getScanExecutionById(id: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/scans/${id}`);
  }

  getScanExecutionsByService(serviceName: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/scans/service/${serviceName}`);
  }

  getRecentScanExecutions(hours: number = 24): Observable<any[]> {
    const params = new HttpParams().set('hours', hours.toString());
    return this.http.get<any[]>(`${this.baseUrl}/scans/recent`, { params });
  }

  getStaleServicesSummary(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/scans/stale-summary`);
  }

  // Remediation Items
  getAllRemediationItems(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/remediation`);
  }

  getRemediationItemById(id: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/remediation/${id}`);
  }

  getRemediationItemsByService(serviceName: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/remediation/service/${serviceName}`);
  }

  getRemediationItemsByPriority(priority: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/remediation/priority/${priority}`);
  }

  getRemediationItemsByTeam(teamName: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/remediation/team/${teamName}`);
  }

  getActionCenterSummary(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/remediation/action-center`);
  }

  getTopRemediationItems(limit: number = 10): Observable<any[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<any[]>(`${this.baseUrl}/remediation/top`, { params });
  }

  updateRemediationStatus(id: string, status: string): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/remediation/${id}/status`, { remediationStatus: status });
  }

  // AI Assistant
  isAiConfigured(): Observable<boolean> {
    return this.http.get<boolean>(`${this.baseUrl}/ai-assistant/configured`);
  }

  explainPriority(findingId: string): Observable<string> {
    return this.http.post<string>(`${this.baseUrl}/ai-assistant/explain-priority`, { findingId });
  }

  generateRemediationGuidance(findingId: string): Observable<string> {
    return this.http.post<string>(`${this.baseUrl}/ai-assistant/remediation-guidance`, { findingId });
  }

  generateServiceRiskSummary(serviceId: string): Observable<string> {
    return this.http.post<string>(`${this.baseUrl}/ai-assistant/service-risk-summary`, { serviceId });
  }

  generateDailySecurityBrief(): Observable<string> {
    return this.http.post<string>(`${this.baseUrl}/ai-assistant/daily-security-brief`, {});
  }

  // Services
  getAllServices(): Observable<ServiceModel[]> {
    return this.http.get<ServiceModel[]>(`${this.baseUrl}/services`);
  }

  getServiceById(id: string): Observable<ServiceModel> {
    return this.http.get<ServiceModel>(`${this.baseUrl}/services/${id}`);
  }

  createService(service: Partial<ServiceModel>): Observable<ServiceModel> {
    return this.http.post<ServiceModel>(`${this.baseUrl}/services`, service);
  }

  updateService(id: string, service: Partial<ServiceModel>): Observable<ServiceModel> {
    return this.http.put<ServiceModel>(`${this.baseUrl}/services/${id}`, service);
  }

  deleteService(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/services/${id}`);
  }

  searchServices(query: string): Observable<ServiceModel[]> {
    const params = new HttpParams().set('query', query);
    return this.http.get<ServiceModel[]>(`${this.baseUrl}/services/search`, { params });
  }

  // Development
  seedSampleServices(): Observable<any> {
    return this.http.post(`${this.baseUrl}/dev/seed`, {});
  }
}