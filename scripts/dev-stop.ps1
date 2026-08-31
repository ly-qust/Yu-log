$ErrorActionPreference = "SilentlyContinue"
$RootDir = Resolve-Path "$PSScriptRoot\.."

foreach ($port in 8080, 5173) {
    Get-NetTCPConnection -LocalPort $port | Select-Object -ExpandProperty OwningProcess -Unique | ForEach-Object {
        Stop-Process -Id $_ -Force
    }
}

docker compose -f (Join-Path $RootDir "docker-compose.local.yml") down
Write-Host "Dev services stopped."
