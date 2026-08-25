export interface DashboardSummary {
  totalFindings: number;
  uniqueFindings: number;
  critical: number;
  high: number;
  medium: number;
  low: number;
  p0: number;
  p1: number;
  p2: number;
  p3: number;
  p4: number;
  topPriorities: TopPriorityFinding[];
  scannerDistribution: { [key: string]: number };
  scanTypeDistribution: { [key: string]: number };
}

export interface TopPriorityFinding {
  cve?: string;
  serviceName: string;
  severity: string;
  riskScore: number;
  priority: string;
}

export interface SecurityFinding {
  id: string;
  reportId: string;
  tool: string;
  scanType: string;
  serviceName: string;
  repository?: string;
  environment: string;
  severity: string;
  cvssScore?: number;
  cve?: string;
  cwe?: string;
  title?: string;
  description?: string;
  packageName?: string;
  installedVersion?: string;
  fixedVersion?: string;
  filePath?: string;
  lineNumber?: number;
  containerImage?: string;
  endpoint?: string;
  httpMethod?: string;
  status: string;
  fingerprint: string;
  sourceFindings?: string[];
  riskScore?: number;
  priority?: string;
  priorityReasons?: string[];
  createdAt: string;
  updatedAt: string;
  firstDetectedAt?: string;
  lastDetectedAt?: string;
  latestScanId?: string;
  detectionState?: string;
}

export interface ScanReport {
  id: string;
  tool: string;
  scanType: string;
  serviceName: string;
  repository?: string;
  branch?: string;
  commitId?: string;
  environment: string;
  uploadedFileName: string;
  totalFindings: number;
  status: string;
  createdAt: string;
}

export interface ServiceModel {
  id: string;
  serviceName: string;
  teamName?: string;
  environment: string;
  businessCriticality?: string;
  internetExposed?: boolean;
  dataSensitivity?: string;
  repository?: string;
  deploymentPlatform?: string;
  owner?: string;
  createdAt: string;
  updatedAt: string;
}

export interface RemediationItem {
  id: string;
  findingId: string;
  serviceName: string;
  teamName?: string;
  priority: string;
  riskScore?: number;
  status: string;
  remediationStatus: string;
  recommendedAction?: string;
  targetDate?: string;
  firstDetectedAt?: string;
  lastDetectedAt?: string;
  latestScanAt?: string;
  resolvedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ScanExecution {
  id: string;
  serviceName: string;
  repository?: string;
  branch?: string;
  commitId?: string;
  tool: string;
  scanType: string;
  status: string;
  triggerType: string;
  startedAt?: string;
  completedAt?: string;
  receivedAt: string;
  totalRawFindings: number;
  totalUniqueFindings: number;
  criticalCount: number;
  highCount: number;
  mediumCount: number;
  lowCount: number;
  newFindings: number;
  resolvedFindings: number;
  unchangedFindings: number;
  createdAt: string;
}

export interface ActionCenterDashboard {
  immediateActions: number;
  dueThisWeek: number;
  staleServices: number;
  recentlyResolved: number;
  topRemediationItems: RemediationItem[];
  recentScanActivity: ScanExecution[];
}

export interface ActionCenterSummary {
  immediateActions: number;
  dueThisWeek: number;
  recentlyResolved: number;
}