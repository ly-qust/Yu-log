param(
    [string]$BackupDir = "$PSScriptRoot\..\backups"
)

$ErrorActionPreference = "Stop"
$RootDir = Resolve-Path "$PSScriptRoot\.."
$EnvFile = Join-Path $RootDir ".env"

if (Test-Path $EnvFile) {
    Get-Content $EnvFile | ForEach-Object {
        if ($_ -match "^\s*#" -or $_ -notmatch "=") { return }
        $parts = $_ -split "=", 2
        [Environment]::SetEnvironmentVariable($parts[0].Trim(), $parts[1].Trim(), "Process")
    }
}

$Database = if ($env:MYSQL_DATABASE) { $env:MYSQL_DATABASE } else { "yu_log" }
$User = if ($env:MYSQL_USER) { $env:MYSQL_USER } else { "yu_log" }
$Password = if ($env:MYSQL_PASSWORD) { $env:MYSQL_PASSWORD } else { "change_me_mysql_password" }
$Timestamp = Get-Date -Format "yyyyMMdd-HHmmss"

New-Item -ItemType Directory -Force -Path $BackupDir | Out-Null
$Output = Join-Path $BackupDir "$Database-$Timestamp.sql"

docker compose -f (Join-Path $RootDir "docker-compose.yml") exec -T mysql mysqldump --single-transaction --quick --no-tablespaces "-u$User" "-p$Password" $Database | Out-File -FilePath $Output -Encoding utf8
if ($LASTEXITCODE -ne 0) {
    Remove-Item -Path $Output -Force -ErrorAction SilentlyContinue
    throw "Database backup failed. Check Docker Compose and MySQL credentials."
}

Write-Host "Database backup written to $Output"
