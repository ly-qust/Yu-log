param(
    [string]$StorageRoot = (Join-Path $PSScriptRoot '..\uploads'),
    [string]$DbHost = $(if ($env:DB_HOST) { $env:DB_HOST } else { 'localhost' }),
    [int]$DbPort = $(if ($env:DB_PORT) { [int]$env:DB_PORT } else { 3308 }),
    [string]$DbName = $(if ($env:DB_NAME) { $env:DB_NAME } else { 'yu_log' }),
    [string]$DbUser = $(if ($env:DB_USERNAME) { $env:DB_USERNAME } else { 'yu_log' }),
    [string]$DbPassword = $(if ($env:DB_PASSWORD) { $env:DB_PASSWORD } elseif ($env:YU_LOG_DB_PASSWORD) { $env:YU_LOG_DB_PASSWORD } else { '' }),
    [string]$PublicPrefix = '/uploads'
)

$ErrorActionPreference = 'Stop'
if ([string]::IsNullOrWhiteSpace($DbPassword)) {
    throw 'Set DB_PASSWORD or YU_LOG_DB_PASSWORD before running the media audit.'
}
$root = [IO.Path]::GetFullPath((Resolve-Path -LiteralPath $StorageRoot).Path)
$rootPrefix = if ($root.EndsWith([IO.Path]::DirectorySeparatorChar)) { $root } else { $root + [IO.Path]::DirectorySeparatorChar }

$query = @"
SELECT 'article', id, COALESCE(cover_image_url, '') FROM article WHERE deleted = 0 AND cover_image_url IS NOT NULL AND cover_image_url <> ''
UNION ALL
SELECT 'project', id, COALESCE(cover_image_url, '') FROM project WHERE deleted = 0 AND cover_image_url IS NOT NULL AND cover_image_url <> ''
"@

$previousPassword = $env:MYSQL_PWD
$env:MYSQL_PWD = $DbPassword
try {
    $rows = @(& mysql --protocol=TCP --host=$DbHost --port=$DbPort --user=$DbUser --database=$DbName --batch --raw --skip-column-names -e $query)
    if ($LASTEXITCODE -ne 0) {
        throw 'Media reference query failed.'
    }
} finally {
    $env:MYSQL_PWD = $previousPassword
}

$referenced = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
$missing = [Collections.Generic.List[string]]::new()
$external = [Collections.Generic.List[string]]::new()
$prefix = $PublicPrefix.TrimEnd('/') + '/'

foreach ($row in $rows) {
    if ([string]::IsNullOrWhiteSpace($row)) { continue }
    $parts = $row -split "`t", 3
    if ($parts.Count -lt 3) { continue }
    $source = "$($parts[0])#$($parts[1])"
    $url = $parts[2].Trim()
    if ($url -notlike "$prefix*") {
        $external.Add("$source -> $url")
        continue
    }
    $relative = $url.Substring($prefix.Length).Replace('/', [IO.Path]::DirectorySeparatorChar)
    $candidate = [IO.Path]::GetFullPath((Join-Path $root $relative))
    if (-not $candidate.StartsWith($rootPrefix, [StringComparison]::OrdinalIgnoreCase)) {
        $missing.Add("$source -> path outside storage root")
        continue
    }
    [void]$referenced.Add($candidate)
    if (-not (Test-Path -LiteralPath $candidate -PathType Leaf)) {
        $missing.Add("$source -> $url")
    }
}

$files = @(Get-ChildItem -LiteralPath $root -Recurse -File)
$orphanFiles = @($files | Where-Object { -not $referenced.Contains($_.FullName) })

Write-Output "Storage root: $root"
Write-Output "DB local references: $($referenced.Count)"
Write-Output "Missing DB references: $($missing.Count)"
foreach ($item in $missing) { Write-Output "MISSING $item" }
Write-Output "External/non-local references: $($external.Count)"
foreach ($item in $external) { Write-Output "EXTERNAL $item" }
Write-Output "Files on disk: $($files.Count)"
Write-Output "Files without DB cover reference: $($orphanFiles.Count)"
foreach ($file in $orphanFiles) { Write-Output "ORPHAN $($file.FullName)" }
Write-Output 'No files were deleted.'
