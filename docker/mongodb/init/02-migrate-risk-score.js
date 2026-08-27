// Migration script to convert riskScore from Integer to Double
db = db.getSiblingDB('securityintel');

var findingsUpdated = 0;
var remediationUpdated = 0;

// Convert riskScore to Double in security_findings collection
if (db.security_findings.exists()) {
    db.security_findings.find({ riskScore: { $type: "int" } }).forEach(function(doc) {
        db.security_findings.updateOne(
            { _id: doc._id },
            { $set: { riskScore: Number(doc.riskScore) } }
        );
        findingsUpdated++;
    });
}

// Convert riskScore to Double in remediation_items collection
if (db.remediation_items.exists()) {
    db.remediation_items.find({ riskScore: { $type: "int" } }).forEach(function(doc) {
        db.remediation_items.updateOne(
            { _id: doc._id },
            { $set: { riskScore: Number(doc.riskScore) } }
        );
        remediationUpdated++;
    });
}

print('Migration completed: ' + findingsUpdated + ' security_findings updated, ' + remediationUpdated + ' remediation_items updated');
