$ErrorActionPreference = "Stop"
$RootDir = Resolve-Path "$PSScriptRoot\.."

docker compose -f (Join-Path $RootDir "docker-compose.local.yml") up -d

$BackendLog = Join-Path $RootDir "backend\dev-server.log"
$BackendErr = Join-Path $RootDir "backend\dev-server.err.log"
$FrontendLog = Join-Path $RootDir "frontend\dev-server.log"
$FrontendErr = Join-Path $RootDir "frontend\dev-server.err.log"

Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run" -WorkingDirectory (Join-Path $RootDir "backend") -RedirectStandardOutput $BackendLog -RedirectStandardError $BackendErr -WindowStyle Hidden
Start-Process -FilePath "npm.cmd" -ArgumentList "run dev" -WorkingDirectory (Join-Path $RootDir "frontend") -RedirectStandardOutput $FrontendLog -RedirectStandardError $FrontendErr -WindowStyle Hidden

Write-Host "Dev services starting."
Write-Host "Backend:  http://localhost:8080/api/health"
Write-Host "Frontend: http://localhost:5173"
