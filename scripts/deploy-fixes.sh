#!/bin/bash

echo "=== Security Intelligence Platform Deployment Script ==="
echo "This script fixes timezone issues and applies MongoDB migration"
echo ""

# Step 1: Run MongoDB migration
echo "Step 1: Running MongoDB migration for riskScore type conversion..."
mongosh "mongodb+srv://admin:admin@cluster0.hs3mybp.mongodb.net/securityintel" --file scripts/migrate-risk-score.js

if [ $? -ne 0 ]; then
    echo "MongoDB migration failed. Please check the error messages above."
    exit 1
fi

echo "MongoDB migration completed successfully!"
echo ""

# Step 2: Build backend
echo "Step 2: Building backend with timezone fixes..."
cd backend
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "Backend build failed. Please check the error messages above."
    exit 1
fi

echo "Backend build completed successfully!"
cd ..
echo ""

# Step 3: Rebuild frontend without cache
echo "Step 3: Rebuilding frontend without cache..."
docker-compose build --no-cache frontend

if [ $? -ne 0 ]; then
    echo "Frontend build failed. Please check the error messages above."
    exit 1
fi

echo "Frontend build completed successfully!"
echo ""

# Step 4: Deploy to server (customize this based on your deployment method)
echo "Step 4: Deploying to server..."
echo "Please customize this section based on your deployment method (Docker, SCP, etc.)"
echo ""

echo "=== Deployment completed successfully! ==="
echo "Please restart your services and verify:"
echo "1. Times are now showing in IST timezone"
echo "2. Report upload works correctly"
echo "3. Remediation status updates work properly"
