// MongoDB initialization script for Security Intelligence Platform
// This script runs when the MongoDB container starts for the first time

print('Starting MongoDB initialization for Security Intelligence Platform...');

// Switch to the securityintel database
db = db.getSiblingDB('securityintel');

// Create collections with validation
db.createCollection('services', {
   validator: {
      $jsonSchema: {
         bsonType: "object",
         required: ["serviceName", "createdAt"],
         properties: {
            serviceName: {
               bsonType: "string",
               description: "Service name is required and must be a string"
            },
            team: {
               bsonType: "string",
               description: "Team name must be a string"
            },
            environment: {
               enum: ["DEVELOPMENT", "TESTING", "STAGING", "PRODUCTION"],
               description: "Environment must be one of the defined values"
            },
            businessCriticality: {
               enum: ["LOW", "MEDIUM", "HIGH", "CRITICAL"],
               description: "Business criticality must be one of the defined values"
            },
            internetExposed: {
               bsonType: "bool",
               description: "Internet exposed must be a boolean"
            },
            dataSensitivity: {
               enum: ["PUBLIC", "INTERNAL", "SENSITIVE", "HIGHLY_SENSITIVE"],
               description: "Data sensitivity must be one of the defined values"
            },
            repository: {
               bsonType: "string",
               description: "Repository URL must be a string"
            },
            deploymentPlatform: {
               bsonType: "string",
               description: "Deployment platform must be a string"
            },
            owner: {
               bsonType: "string",
               description: "Owner must be a string"
            },
            createdAt: {
               bsonType: "date",
               description: "Created at is required and must be a date"
            },
            updatedAt: {
               bsonType: "date",
               description: "Updated at must be a date"
            }
         }
      }
   }
});

db.createCollection('security_findings', {
   validator: {
      $jsonSchema: {
         bsonType: "object",
         required: ["cve", "serviceName", "severity", "tool", "status", "fingerprint", "createdAt"],
         properties: {
            cve: {
               bsonType: "string",
               description: "CVE ID is required and must be a string"
            },
            title: {
               bsonType: "string",
               description: "Title must be a string"
            },
            description: {
               bsonType: "string",
               description: "Description must be a string"
            },
            severity: {
               enum: ["LOW", "MEDIUM", "HIGH", "CRITICAL"],
               description: "Severity is required and must be one of the defined values"
            },
            cvssScore: {
               bsonType: "double",
               minimum: 0.0,
               maximum: 10.0,
               description: "CVSS score must be between 0.0 and 10.0"
            },
            tool: {
               enum: ["TRIVY", "SNYK"],
               description: "Tool is required and must be one of the defined values"
            },
            serviceName: {
               bsonType: "string",
               description: "Service name is required and must be a string"
            },
            packageName: {
               bsonType: "string",
               description: "Package name must be a string"
            },
            packageVersion: {
               bsonType: "string",
               description: "Package version must be a string"
            },
            fixedVersion: {
               bsonType: "string",
               description: "Fixed version must be a string"
            },
            location: {
               bsonType: "string",
               description: "Location must be a string"
            },
            status: {
               enum: ["OPEN", "FIXED", "IGNORED", "ACCEPTED", "COMPLETED", "FAILED"],
               description: "Status is required and must be one of the defined values"
            },
            fingerprint: {
               bsonType: "string",
               description: "Fingerprint is required and must be a string"
            },
            priority: {
               enum: ["P0", "P1", "P2", "P3", "P4"],
               description: "Priority must be one of the defined values"
            },
            riskScore: {
               bsonType: "double",
               minimum: 0.0,
               maximum: 100.0,
               description: "Risk score must be between 0.0 and 100.0"
            },
            reportId: {
               bsonType: "string",
               description: "Report ID must be a string"
            },
            createdAt: {
               bsonType: "date",
               description: "Created at is required and must be a date"
            },
            updatedAt: {
               bsonType: "date",
               description: "Updated at must be a date"
            }
         }
      }
   }
});

db.createCollection('scan_reports', {
   validator: {
      $jsonSchema: {
         bsonType: "object",
         required: ["tool", "serviceName", "status", "createdAt"],
         properties: {
            tool: {
               enum: ["TRIVY", "SNYK"],
               description: "Tool is required and must be one of the defined values"
            },
            scanType: {
               enum: ["CONTAINER", "DEPENDENCY", "CODE", "INFRASTRUCTURE"],
               description: "Scan type must be one of the defined values"
            },
            serviceName: {
               bsonType: "string",
               description: "Service name is required and must be a string"
            },
            environment: {
               enum: ["DEVELOPMENT", "TESTING", "STAGING", "PRODUCTION"],
               description: "Environment must be one of the defined values"
            },
            status: {
               enum: ["OPEN", "FIXED", "IGNORED", "ACCEPTED", "COMPLETED", "FAILED"],
               description: "Status is required and must be one of the defined values"
            },
            findingsCount: {
               bsonType: "int",
               minimum: 0,
               description: "Findings count must be a non-negative integer"
            },
            uniqueVulnerabilities: {
               bsonType: "int", 
               minimum: 0,
               description: "Unique vulnerabilities must be a non-negative integer"
            },
            criticalCount: {
               bsonType: "int",
               minimum: 0,
               description: "Critical count must be a non-negative integer"
            },
            highCount: {
               bsonType: "int",
               minimum: 0,
               description: "High count must be a non-negative integer"
            },
            mediumCount: {
               bsonType: "int",
               minimum: 0,
               description: "Medium count must be a non-negative integer"
            },
            lowCount: {
               bsonType: "int",
               minimum: 0,
               description: "Low count must be a non-negative integer"
            },
            processedAt: {
               bsonType: "date",
               description: "Processed at must be a date"
            },
            createdAt: {
               bsonType: "date",
               description: "Created at is required and must be a date"
            }
         }
      }
   }
});

// Create indexes for performance
print('Creating indexes...');

// Services indexes
db.services.createIndex({ "serviceName": 1 }, { unique: true });
db.services.createIndex({ "environment": 1 });
db.services.createIndex({ "businessCriticality": 1 });
db.services.createIndex({ "team": 1 });
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
db.scan_reports.createIndex({ "processedAt": -1 });
db.scan_reports.createIndex({ "serviceName": 1, "createdAt": -1 });

// Create sample data for testing
print('Creating sample services...');

db.services.insertMany([
   {
      serviceName: "payment-service",
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
print('Created collections: services, security_findings, scan_reports');
print('Created indexes for optimized querying');
print('Inserted sample services for testing');

// Print collection stats
print('\nCollection statistics:');
print('Services: ' + db.services.countDocuments());
print('Security Findings: ' + db.security_findings.countDocuments());
print('Scan Reports: ' + db.scan_reports.countDocuments());