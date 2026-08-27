// Initialize Security Intelligence Database
db = db.getSiblingDB('securityintel');

// Create collections with indexes
db.createCollection('scan_reports');
db.createCollection('security_findings');
db.createCollection('services');
db.createCollection('remediation_items');
db.createCollection('scan_executions');

// Create indexes for scan_reports
db.scan_reports.createIndex({ "tool": 1 });
db.scan_reports.createIndex({ "scanType": 1 });
db.scan_reports.createIndex({ "serviceName": 1 });
db.scan_reports.createIndex({ "environment": 1 });
db.scan_reports.createIndex({ "status": 1 });
db.scan_reports.createIndex({ "createdAt": -1 });

// Create indexes for security_findings
db.security_findings.createIndex({ "cve": 1 });
db.security_findings.createIndex({ "serviceName": 1 });
db.security_findings.createIndex({ "tool": 1 });
db.security_findings.createIndex({ "severity": 1 });
db.security_findings.createIndex({ "status": 1 });
db.security_findings.createIndex({ "fingerprint": 1 }, { unique: true });
db.security_findings.createIndex({ "priority": 1 });
db.security_findings.createIndex({ "riskScore": -1 });
db.security_findings.createIndex({ "createdAt": -1 });
db.security_findings.createIndex({ "reportId": 1 });

// Create indexes for services
db.services.createIndex({ "serviceName": 1 }, { unique: true });
db.services.createIndex({ "environment": 1 });
db.services.createIndex({ "businessCriticality": 1 });
db.services.createIndex({ "internetExposed": 1 });
db.services.createIndex({ "dataSensitivity": 1 });
db.services.createIndex({ "createdAt": -1 });

// Create indexes for remediation_items
db.remediation_items.createIndex({ "findingId": 1 });
db.remediation_items.createIndex({ "serviceName": 1 });
db.remediation_items.createIndex({ "priority": 1 });
db.remediation_items.createIndex({ "riskScore": -1 });
db.remediation_items.createIndex({ "status": 1 });
db.remediation_items.createIndex({ "remediationStatus": 1 });
db.remediation_items.createIndex({ "teamName": 1 });
db.remediation_items.createIndex({ "createdAt": -1 });

// Create indexes for scan_executions
db.scan_executions.createIndex({ "serviceName": 1 });
db.scan_executions.createIndex({ "environment": 1 });
db.scan_executions.createIndex({ "tool": 1 });
db.scan_executions.createIndex({ "scanType": 1 });
db.scan_executions.createIndex({ "status": 1 });
db.scan_executions.createIndex({ "startedAt": -1 });

print('Security Intelligence Database initialized with collections and indexes');