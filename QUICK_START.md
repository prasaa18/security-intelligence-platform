# Quick Start Guide - Get Everything Running in 5 Minutes

## 🚀 IMMEDIATE ACTION NEEDED: MongoDB Setup

The application requires MongoDB to run. Here are your options:

### Option 1: Install MongoDB (Recommended for Development)
**Download & Install:**
1. Go to: https://www.mongodb.com/try/download/community
2. Download MongoDB Community Server for Windows
3. Run the installer
4. Start MongoDB: `net start MongoDB`

### Option 2: Use MongoDB Atlas (Easiest - Free Cloud)
**Setup:**
1. Go to: https://www.mongodb.com/cloud/atlas/register
2. Create free account
3. Create free cluster (takes 2-3 minutes)
4. Click "Connect" → "Connect your application"
5. Copy the connection string
6. Update `backend/src/main/resources/application.yml` line 6:
   ```yaml
   uri: mongodb+srv://YOUR_USERNAME:YOUR_PASSWORD@YOUR_CLUSTER.mongodb.net/securityintel
   ```

## 📋 QUICK SETUP STEPS

### Step 1: Configure the Docker deployment
From the project root, copy `.env.example` to `.env` and replace every `replace-with-...` value with a strong secret. Docker Compose keeps MongoDB and the backend private; only the frontend port is published.

Generate tokens instead of typing predictable values:
```bash
# Linux/macOS
openssl rand -hex 32

# PowerShell
[Convert]::ToHexString((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

### Step 2: Create Environment File for local development
Create `backend/.env` when running the backend outside Docker:
```bash
MONGODB_URI=mongodb://localhost:27017/securityintel
SCAN_INGESTION_TOKEN=replace-with-a-long-random-token
GEMINI_API_KEY=your-gemini-key-optional
```

### Step 3: Start Applications
```bash
# Terminal 1 - Backend
cd security-intelligence-platform/backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Terminal 2 - Frontend  
cd security-intelligence-platform/frontend
npm start
```

For a protected deployment, use the `secure` profile and set `APP_ADMIN_USERNAME`, `APP_ADMIN_PASSWORD`, `APP_VIEWER_USERNAME`, and `APP_VIEWER_PASSWORD`. The secure profile protects application APIs with stateless HTTP Basic authentication; GitHub Actions ingestion continues to require `SCAN_INGESTION_TOKEN`.

### Step 4: Test the Application
- Open browser: http://localhost:4200
- You should see the Security Remediation Intelligence Platform

## 🧪 QUICK TESTING

### Test with Sample Data
```bash
# Seed sample services (available only with the dev profile)
curl -X POST http://localhost:8080/api/dev/seed

### Docker deployment
```bash
docker compose up -d --build
docker compose ps
```

Open `http://YOUR_SERVER_IP:4200`. The public API base for all CI integrations is `http://YOUR_SERVER_IP:4200/api` and the upload endpoint is `POST /api/integrations/scans/github-actions`.

Use the same `SCAN_INGESTION_TOKEN` from `.env` as the `Authorization: Bearer` token in GitHub Actions, GitLab CI, Jenkins, or direct uploads. Do not expose MongoDB or port 8080 publicly.

# Upload sample report
curl -X POST http://localhost:8080/api/reports/upload \
  -F "file=@sample-data/sample-trivy-report.json" \
  -F "serviceName=test-service" \
  -F "environment=DEVELOPMENT"
```

### Navigate Through UI
1. **Action Center** - Check dashboard metrics
2. **Remediation Plan** - View remediation items
3. **Scans** - Check scan history
4. **AI Assistant** - Test AI features (requires GEMINI_API_KEY)

## 🔗 GITHUB SETUP (After Local Testing Works)

### Push to GitHub
```bash
cd security-intelligence-platform
git remote add origin https://github.com/YOUR_USERNAME/security-intelligence-platform.git
git branch -M main
git push -u origin main
```

### Setup Demo Services
For each demo service (payment-service, order-service, auth-service):
1. Create separate GitHub repository
2. Push demo service code
3. Add GitHub Secrets:
   - `SECURITY_INTEL_API_URL`: Your backend URL
   - `SCAN_INGESTION_TOKEN`: Same as in backend/.env

## 💡 GEMINI API KEY (Optional but Recommended)

### Get Gemini API Key
1. Go to: https://aistudio.google.com/app/apikey
2. Create new API key
3. Add to `backend/.env`:
   ```bash
   GEMINI_API_KEY=your-actual-api-key
   ```
4. Restart backend
5. Test AI features in the UI

## ⚡ WHAT'S WORKING NOW

✅ Frontend: Running on http://localhost:4200  
✅ Backend: Ready to run (needs MongoDB)  
✅ All features implemented  
✅ Complete documentation  
✅ Demo services ready

## 🎯 CORE QUESTIONS THE PLATFORM ANSWERS

Once running, the platform will answer:
1. **"What should we fix first?"** → Action Center P0 items
2. **"Why fix it first?"** → Priority reasons & risk scores  
3. **"Who owns it?"** → Team assignments
4. **"What should they do?"** → Recommended actions
5. **"Is data current?"** → Scan freshness indicators
6. **"What changed?"** → Scan comparison results

## 📞 NEXT STEPS

1. **Get MongoDB running** (Option 1 or 2 above)
2. **Start backend** with MongoDB connection
3. **Test the UI** at http://localhost:4200
4. **Configure Gemini** (optional)
5. **Push to GitHub** for CI/CD testing

The complete transformation is done - we just need MongoDB to make it fully functional!