# Complete End-to-End Setup Guide

## Step 1: Install MongoDB (Required for Backend)

Since MongoDB is not installed on your system, you need to install it:

### Option A: Install MongoDB locally
1. Download MongoDB Community Server from: https://www.mongodb.com/try/download/community
2. Install MongoDB Windows version
3. Start MongoDB service:
   ```bash
   net start MongoDB
   ```
4. Verify MongoDB is running:
   ```bash
   mongosh --eval "db.adminCommand('ping')"
   ```

### Option B: Use MongoDB Atlas (Cloud - Free)
1. Go to https://www.mongodb.com/cloud/atlas
2. Create a free account
3. Create a new cluster (free tier)
4. Get your connection string
5. Update `backend/src/main/resources/application.yml`:
   ```yaml
   spring:
     data:
       mongodb:
         uri: mongodb+srv://YOUR_USERNAME:YOUR_PASSWORD@YOUR_CLUSTER.mongodb.net/securityintel
   ```

## Step 2: Configure Environment Variables

Create a file `backend/.env`:
```bash
# MongoDB Connection (use your actual connection)
MONGODB_URI=mongodb://localhost:27017/securityintel

# GitHub Actions Integration (choose a secure token)
SCAN_INGESTION_TOKEN=your-secret-token-here

# Gemini AI (optional - get from https://aistudio.google.com/app/apikey)
GEMINI_API_KEY=your-gemini-api-key-here
```

## Step 3: Start the Complete Application

### Start MongoDB (if using local installation)
```bash
# Start MongoDB service
net start MongoDB

# Or run mongod directly
mongod
```

### Start Backend
```bash
cd security-intelligence-platform/backend
mvn spring-boot:run
```

### Start Frontend (in another terminal)
```bash
cd security-intelligence-platform/frontend
npm start
```

## Step 4: Configure Gemini API (Optional but Recommended)

1. Go to https://aistudio.google.com/app/apikey
2. Create a new API key
3. Add it to your `backend/.env` file:
   ```bash
   GEMINI_API_KEY=your-actual-api-key
   ```
4. Restart the backend

## Step 5: GitHub Setup and Push

### Create GitHub Repository
1. Go to https://github.com/new
2. Create a new repository (e.g., `security-intelligence-platform`)
3. Don't initialize with README (we have one)

### Push Your Code
```bash
cd security-intelligence-platform
git remote add origin https://github.com/YOUR_USERNAME/security-intelligence-platform.git
git branch -M main
git push -u origin main
```

## Step 6: Setup Demo Services with GitHub Actions

### For Each Demo Service (payment-service, order-service, auth-service):

1. **Create separate GitHub repositories** for each service
2. **Push the demo service code** to each repository
3. **Configure GitHub Secrets** in each repository:
   - Go to Settings → Secrets and variables → Actions
   - Add these secrets:
     - `SECURITY_INTEL_API_URL`: Your backend URL (or use ngrok for local testing)
     - `SCAN_INGESTION_TOKEN`: Same token you set in backend/.env

### Local Testing with ngrok (Optional)
If you want to test GitHub Actions locally:
1. Install ngrok: https://ngrok.com/download
2. Run ngrok:
   ```bash
   ngrok http 8080
   ```
3. Use the ngrok URL as `SECURITY_INTEL_API_URL`

## Step 7: Complete End-to-End Testing Workflow

### 1. Access the Application
- Open browser: http://localhost:4200
- You should see the "Security Remediation Intelligence Platform"

### 2. Test Dashboard
- Navigate to "Action Center"
- Verify metrics cards are displayed
- Check Top Remediation Plan section

### 3. Create Test Services
- Go to "Services" page
- Create a test service:
  - Service Name: "test-service"
  - Environment: "DEVELOPMENT"
  - Team: "platform-team"
  - Business Criticality: "MEDIUM"

### 4. Upload Sample Security Reports
- Go to "Reports" page
- Upload sample Trivy report:
  ```bash
  cd security-intelligence-platform
  curl -X POST http://localhost:8080/api/reports/upload \
    -F "file=@sample-data/sample-trivy-report.json" \
    -F "serviceName=test-service" \
    -F "environment=DEVELOPMENT"
  ```

### 5. Test Remediation Plan
- Navigate to "Remediation Plan"
- Verify remediation items are created
- Test filtering by priority/team/service
- Update remediation status

### 6. Test Scan History
- Navigate to "Scans"
- Verify scan executions are recorded
- Check comparison results (NEW/UNCHANGED/NOT_DETECTED)

### 7. Test AI Assistant (if Gemini configured)
- Navigate to "AI Assistant"
- Test quick actions
- Generate daily security brief
- Ask for priority explanations

### 8. Test GitHub Actions Integration
- Push code to one of the demo service repositories
- Trigger the GitHub Actions workflow manually
- Verify the scan report reaches your platform
- Check that remediation items are created

## Step 8: Verify the Core Questions

The platform should now answer:
1. **"What should we fix first?"** → Check Action Center P0 items
2. **"Why should we fix it first?"** → Click on remediation item to see priority reasons
3. **"Who owns it?"** → Team assignments in remediation items
4. **"What should they do?"** → Recommended actions and AI guidance
5. **"Is the security data current?"** → Scan freshness indicators
6. **"What changed since the last scan?"** → Scan comparison results

## Troubleshooting

### MongoDB Connection Issues
- Ensure MongoDB is running: `net start MongoDB`
- Check connection string in application.yml
- Verify MongoDB credentials

### Backend Startup Issues
- Check backend logs for specific errors
- Verify all dependencies are installed
- Ensure MongoDB is accessible

### Frontend Build Issues
- Run `npm install` to ensure all dependencies
- Check for TypeScript errors
- Verify API service configuration

### GitHub Actions Issues
- Verify GitHub Secrets are set correctly
- Check workflow logs for errors
- Ensure backend is accessible from GitHub (use ngrok for local testing)

## Next Steps After Testing

Once everything is working:
1. Deploy to a proper hosting environment
2. Configure production MongoDB
3. Set up proper API authentication
4. Configure CI/CD pipeline
5. Set up monitoring and alerting