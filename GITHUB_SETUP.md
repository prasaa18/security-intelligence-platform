# Complete Setup Guide for GitHub Integration

## 🎯 Quick Setup - 5 Minutes to Testing

### Step 1: Configure GitHub Repository

**Add GitHub Secrets to your repository:**
1. Go to your repository: https://github.com/prasaa18/security-intelligence-platform
2. Navigate to **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret** and add:

**Secret 1: SCAN_INGESTION_TOKEN**
- Name: `SCAN_INGESTION_TOKEN`
- Value: `test-token-123` (or your secure token)

**Secret 2: SECURITY_INTEL_API_URL**
- Name: `SECURITY_INTEL_API_URL`
- Value: For local testing: `http://localhost:8080/api`
- For AWS deployment: `http://your-aws-instance-ip:8080/api`

### Step 2: Update Application Configuration

The current configuration is set up with:
- **MongoDB Atlas**: `mongodb+srv://admin:admin@cluster0.hs3mybp.mongodb.net/securityintel`
- **Gemini API Key**: Your provided key
- **Scan Ingestion Token**: `test-token-123`

### Step 3: Fix MongoDB Connection Issue

**Option A: Use MongoDB Atlas (Recommended)**
The configuration is already set to use MongoDB Atlas. Make sure:
1. Your MongoDB Atlas cluster is running
2. Network access allows connections from your IP
3. Credentials are correct

**Option B: Use Local MongoDB**
If you prefer local MongoDB:
1. Install MongoDB locally
2. Start MongoDB service: `net start MongoDB`
3. Update `backend/src/main/resources/application.yml`:
   ```yaml
   spring:
     data:
       mongodb:
         uri: mongodb://localhost:27017/securityintel
   ```

### Step 4: Start the Application

**Start Backend:**
```bash
cd security-intelligence-platform/backend
mvn spring-boot:run
```

**Start Frontend (in another terminal):**
```bash
cd security-intelligence-platform/frontend
npm start
```

### Step 5: Test the Integration

**Test 1: Check if Backend is Running**
```bash
curl http://localhost:8080/api/dashboard/summary
```

**Test 2: Test AI Assistant with Gemini**
```bash
curl http://localhost:8080/api/ai-assistant/configured
```

**Test 3: Upload Sample Report**
```bash
cd security-intelligence-platform
curl -X POST http://localhost:8080/api/reports/upload \
  -F "file=@sample-data/sample-trivy-report.json" \
  -F "serviceName=test-service" \
  -F "environment=DEVELOPMENT"
```

**Test 4: Test GitHub Actions Integration**
1. Push code to GitHub: `git push origin main`
2. Go to **Actions** tab in your GitHub repository
3. Manually trigger the **Security Scan with Trivy** workflow
4. Check the workflow runs and sends data to your platform

## 🚀 GitHub Actions Integration Setup

### What the Workflow Does:

The `.github/workflows/security-scan.yml` file automatically:
1. **Runs Trivy security scan** on your code
2. **Generates service metadata** (service name, environment, commit info)
3. **Sends results to your Security Intelligence Platform**
4. **Uploads reports as artifacts** for review

### How to Use Your devsecops-portfolio Repository:

Since your `devsecops-portfolio` repository is a portfolio website, you can:

**Option 1: Add Security Scanning to devsecops-portfolio**
1. Clone your devsecops-portfolio repository
2. Copy the `.github/workflows/security-scan.yml` file to it
3. Add the same GitHub secrets to devsecops-portfolio
4. Push changes to trigger security scans

**Option 2: Create a dedicated test repository**
1. Create a new repository for testing (e.g., `test-security-scan`)
2. Add the security-scan.yml workflow
3. Configure GitHub secrets
4. Push code to test the integration

## 🌐 AWS Deployment Setup

### For AWS Deployment:

**Step 1: Prepare AWS Instance**
1. Launch an EC2 instance (Ubuntu/Amazon Linux)
2. Install Java 20, Maven, Node.js, MongoDB
3. Open port 8080 in security group

**Step 2: Deploy the Application**
```bash
# On AWS instance
git clone https://github.com/prasaa18/security-intelligence-platform.git
cd security-intelligence-platform/backend
mvn clean package
nohup java -jar target/security-intelligence-platform-0.0.1-SNAPSHOT.jar &

cd ../frontend
npm install
npm run build
# Use nginx or serve the dist folder
```

**Step 3: Update GitHub Secrets**
- Change `SECURITY_INTEL_API_URL` to your AWS instance public IP
- Example: `http://your-aws-ip:8080/api`

## 🧪 Complete Testing Workflow

### Test 1: MongoDB Connection
```bash
# Test MongoDB connection
mongosh "mongodb+srv://admin:admin@cluster0.hs3mybp.mongodb.net/securityintel"
```

### Test 2: Backend Health
```bash
curl http://localhost:8080/api/actuator/health
```

### Test 3: Gemini AI Integration
```bash
# Test if Gemini is configured
curl http://localhost:8080/api/ai-assistant/configured

# Test AI generation
curl -X POST http://localhost:8080/api/ai-assistant/generate \
  -H "Content-Type: application/json" \
  -d '{"context": "test", "query": "What are the top security priorities?"}'
```

### Test 4: GitHub Actions Integration
1. Make a small change to any file
2. Commit and push: `git add . && git commit -m "test" && git push origin main`
3. Monitor the GitHub Actions workflow
4. Check your Security Intelligence Platform for the scan results

### Test 5: UI Testing
1. Open browser: http://localhost:4200
2. Navigate through all pages
3. Test the AI Assistant features
4. Check if scan results appear in the dashboard

## 🔧 Troubleshooting

### MongoDB Connection Issues
- Check MongoDB Atlas cluster status
- Verify network access allows your IP
- Test connection: `mongosh "mongodb+srv://admin:admin@cluster0.hs3mybp.mongodb.net/securityintel"`

### Backend Startup Issues
- Check if port 8080 is available: `netstat -ano | findstr ":8080"`
- Kill process if needed: `taskkill /F /PID <PID>`
- Check application logs for specific errors

### GitHub Actions Issues
- Verify GitHub secrets are set correctly
- Check workflow logs for authentication errors
- Ensure SECURITY_INTEL_API_URL is accessible from GitHub

### Gemini API Issues
- Verify API key is correct
- Check Gemini API quota and limits
- Test API key directly with Google's API tester

## 📝 Configuration Summary

**Current Configuration:**
- **MongoDB**: MongoDB Atlas cluster
- **Gemini API Key**: `AQ.Ab8RN6IWwkgoeWu8kZbavbdGdjhj5MtrrCPBDeDLSiIPCoHBow`
- **Scan Token**: `test-token-123`
- **GitHub Repository**: `https://github.com/prasaa18/security-intelligence-platform`
- **Test Repository**: `https://github.com/prasaa18/devsecops-portfolio`

**Next Steps:**
1. ✅ Gemini API key configured
2. ✅ GitHub Actions workflow created
3. ✅ GitHub remote added
4. ⏳ Fix MongoDB connection (choose Atlas or local)
5. ⏳ Test backend startup
6. ⏳ Test GitHub Actions integration
7. ⏳ Deploy to AWS (optional)

## 🎯 Testing Checklist

- [ ] MongoDB connection successful
- [ ] Backend starts without errors
- [ ] Frontend loads at http://localhost:4200
- [ ] Dashboard displays correctly
- [ ] AI Assistant responds to queries
- [ ] GitHub Actions workflow runs successfully
- [ ] Scan results appear in the platform
- [ ] Remediation items are created from scans
- [ ] Security state calculations work correctly

The complete transformation is ready - we just need to resolve the MongoDB connection to start end-to-end testing!