param (
    [Parameter(Mandatory=$true)]
    [string]$ReportFile,

    [Parameter(Mandatory=$true)]
    [string]$ServiceName,

    [Parameter(Mandatory=$false)]
    [string]$Environment = "PRODUCTION",

    [Parameter(Mandatory=$false)]
    [string]$PlatformUrl = $env:SECURITY_PLATFORM_URL
)

if (-not $PlatformUrl) {
    $PlatformUrl = "http://localhost:8080"
}

if (-not (Test-Path $ReportFile)) {
    Write-Error "File not found: $ReportFile"
    exit 1
}

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "🛡 Security Intelligence Platform — Ingesting Scan" -ForegroundColor Cyan
Write-Host "  File:         $ReportFile"
Write-Host "  Service:      $ServiceName"
Write-Host "  Environment:  $Environment"
Write-Host "  Endpoint:     $PlatformUrl/api/reports/upload"
Write-Host "========================================================" -ForegroundColor Cyan

$uri = "$PlatformUrl/api/reports/upload"

$form = @{
    file = Get-Item -Path $ReportFile
    serviceName = $ServiceName
    environment = $Environment
}

try {
    $response = Invoke-RestMethod -Uri $uri -Method Post -Form $form
    Write-Host "✓ Ingestion successful!" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Error "✗ Ingestion failed: $_"
    exit 1
}

