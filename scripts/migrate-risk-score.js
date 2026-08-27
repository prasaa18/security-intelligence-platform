// Manual migration script for existing MongoDB deployment
// Run this on your server using: mongosh "mongodb+srv://admin:admin@cluster0.hs3mybp.mongodb.net/securityintel" --file migrate-risk-score.js

var findingsUpdated = 0;
var remediationUpdated = 0;

// Convert riskScore to Double in security_findings collection
db.security_findings.find({ riskScore: { $type: "int" } }).forEach(function(doc) {
    db.security_findings.updateOne(
        { _id: doc._id },
        { $set: { riskScore: Number(doc.riskScore) } }
    );
    findingsUpdated++;
});

// Convert riskScore to Double in remediation_items collection
db.remediation_items.find({ riskScore: { $type: "int" } }).forEach(function(doc) {
    db.remediation_items.updateOne(
        { _id: doc._id },
        { $set: { riskScore: Number(doc.riskScore) } }
    );
    remediationUpdated++;
});

print('Migration completed: ' + findingsUpdated + ' security_findings updated, ' + remediationUpdated + ' remediation_items updated');
