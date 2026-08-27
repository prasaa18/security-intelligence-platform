// MongoDB initialization script for Security Intelligence Platform
// This script runs when the MongoDB container starts for the first time

print('Starting MongoDB initialization for Security Intelligence Platform...');

// Switch to the securityintel database
db = db.getSiblingDB('securityintel');

// Create collections
db.createCollection('services');
db.createCollection('security_findings');
db.createCollection('scan_reports');
db.createCollection('remediation_items');
db.createCollection('scan_executions');

// Create indexes for performance
print('Creating indexes...');

// Services indexes
db.services.createIndex({ "serviceName": 1 }, { unique: true });
db.services.createIndex({ "environment": 1 });
db.services.createIndex({ "businessCriticality": 1 });
db.services.createIndex({ "teamName": 1 });
db.services.createIndex({ "createdAt": -1 });

// Security findings indexes
db.security_findings.createIndex({ "fingerprint": 1 }, { unique: true });
db.security_findings.createIndex({ "serviceName": 1 });
db.security_findings.createIndex({ "cve": 1 });
db.security_findings.createIndex({ "severity": 1 });
db.security_findings.createIndex({ "priority": 1 });
db.security_findings.createIndex({ "status": 1 });
db.security_findings.createIndex({ "tool": 1 });
db.security_findings.createIndex({ "packageName": 1 });
db.security_findings.createIndex({ "createdAt": -1 });
db.security_findings.createIndex({ "updatedAt": -1 });
db.security_findings.createIndex({ "riskScore": -1 });
db.security_findings.createIndex({ "scanExecutionId": 1 });

// Compound indexes for common queries
db.security_findings.createIndex({ "serviceName": 1, "status": 1 });
db.security_findings.createIndex({ "severity": 1, "status": 1 });
db.security_findings.createIndex({ "priority": 1, "status": 1 });
db.security_findings.createIndex({ "serviceName": 1, "severity": 1, "status": 1 });

// Scan reports indexes
db.scan_reports.createIndex({ "serviceName": 1 });
db.scan_reports.createIndex({ "tool": 1 });
db.scan_reports.createIndex({ "status": 1 });
db.scan_reports.createIndex({ "environment": 1 });
db.scan_reports.createIndex({ "createdAt": -1 });
db.scan_reports.createIndex({ "serviceName": 1, "createdAt": -1 });

// Remediation items indexes
db.remediation_items.createIndex({ "findingId": 1 });
db.remediation_items.createIndex({ "serviceName": 1 });
db.remediation_items.createIndex({ "teamName": 1 });
db.remediation_items.createIndex({ "priority": 1 });
db.remediation_items.createIndex({ "status": 1 });
db.remediation_items.createIndex({ "remediationStatus": 1 });

// Scan executions indexes
db.scan_executions.createIndex({ "serviceName": 1 });
db.scan_executions.createIndex({ "createdAt": -1 });
db.scan_executions.createIndex({ "status": 1 });

// Create sample data for testing
print('Creating sample services...');

db.services.insertMany([
   {
      serviceName: "payment-service",
      teamName: "payments-team",
      team: "payments-team",
      environment: "PRODUCTION",
      businessCriticality: "CRITICAL",
      internetExposed: true,
      dataSensitivity: "HIGHLY_SENSITIVE",
      repository: "https://github.com/company/payment-service",
      deploymentPlatform: "kubernetes",
      owner: "payments-team@company.com",
      createdAt: new Date(),
      updatedAt: new Date()
   },
   {
      serviceName: "auth-service", 
      teamName: "identity-team",
      team: "identity-team",
      environment: "PRODUCTION",
      businessCriticality: "CRITICAL",
      internetExposed: true,
      dataSensitivity: "HIGHLY_SENSITIVE",
      repository: "https://github.com/company/auth-service",
      deploymentPlatform: "kubernetes",
      owner: "identity-team@company.com",
      createdAt: new Date(),
      updatedAt: new Date()
   },
   {
      serviceName: "user-service",
      teamName: "user-experience-team",
      team: "user-experience-team", 
      environment: "PRODUCTION",
      businessCriticality: "HIGH",
      internetExposed: true,
      dataSensitivity: "SENSITIVE",
      repository: "https://github.com/company/user-service",
      deploymentPlatform: "kubernetes", 
      owner: "ux-team@company.com",
      createdAt: new Date(),
      updatedAt: new Date()
   },
   {
      serviceName: "notification-service",
      teamName: "communications-team",
      team: "communications-team",
      environment: "PRODUCTION", 
      businessCriticality: "MEDIUM",
      internetExposed: false,
      dataSensitivity: "INTERNAL",
      repository: "https://github.com/company/notification-service",
      deploymentPlatform: "kubernetes",
      owner: "comms-team@company.com", 
      createdAt: new Date(),
      updatedAt: new Date()
   },
   {
      serviceName: "analytics-service",
      teamName: "data-team",
      team: "data-team",
      environment: "PRODUCTION",
      businessCriticality: "MEDIUM", 
      internetExposed: false,
      dataSensitivity: "INTERNAL",
      repository: "https://github.com/company/analytics-service",
      deploymentPlatform: "kubernetes",
      owner: "data-team@company.com",
      createdAt: new Date(),
      updatedAt: new Date()
   }
]);

print('MongoDB initialization completed successfully!');
print('Created collections: services, security_findings, scan_reports, remediation_items, scan_executions');
print('Created indexes for optimized querying');
print('Inserted sample services for testing');