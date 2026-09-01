param(
    [string]$DbHost = $(if ($env:DB_HOST) { $env:DB_HOST } else { 'localhost' }),
    [int]$DbPort = $(if ($env:DB_PORT) { [int]$env:DB_PORT } else { 3308 }),
    [string]$DbName = $(if ($env:DB_NAME) { $env:DB_NAME } else { 'yu_log' }),
    [string]$DbUser = $(if ($env:DB_USERNAME) { $env:DB_USERNAME } else { 'yu_log' }),
    [string]$DbPassword = $(if ($env:DB_PASSWORD) { $env:DB_PASSWORD } elseif ($env:YU_LOG_DB_PASSWORD) { $env:YU_LOG_DB_PASSWORD } else { '' })
)

$ErrorActionPreference = 'Stop'
if ([string]::IsNullOrWhiteSpace($DbPassword)) {
    throw 'Set DB_PASSWORD or YU_LOG_DB_PASSWORD before running the data audit.'
}

$queries = [ordered]@{
    'orphan_article_tag_article' = "SELECT COUNT(*) FROM article_tag at LEFT JOIN article a ON a.id = at.article_id AND a.deleted = 0 WHERE a.id IS NULL"
    'orphan_article_tag_tag' = "SELECT COUNT(*) FROM article_tag at LEFT JOIN tag t ON t.id = at.tag_id AND t.deleted = 0 WHERE t.id IS NULL"
    'article_missing_category' = "SELECT COUNT(*) FROM article a LEFT JOIN category c ON c.id = a.category_id AND c.deleted = 0 WHERE a.deleted = 0 AND a.category_id IS NOT NULL AND c.id IS NULL"
    'note_missing_category' = "SELECT COUNT(*) FROM note n LEFT JOIN category c ON c.id = n.category_id AND c.deleted = 0 WHERE n.deleted = 0 AND n.category_id IS NOT NULL AND c.id IS NULL"
    'comment_missing_article' = "SELECT COUNT(*) FROM comment cm LEFT JOIN article a ON a.id = cm.article_id AND a.deleted = 0 WHERE cm.deleted = 0 AND a.id IS NULL"
    'timeline_missing_article' = "SELECT COUNT(*) FROM timeline_event t LEFT JOIN article a ON a.id = t.related_article_id AND a.deleted = 0 WHERE t.deleted = 0 AND t.related_article_id IS NOT NULL AND a.id IS NULL"
    'timeline_missing_project' = "SELECT COUNT(*) FROM timeline_event t LEFT JOIN project p ON p.id = t.related_project_id AND p.deleted = 0 WHERE t.deleted = 0 AND t.related_project_id IS NOT NULL AND p.id IS NULL"
    'invalid_article_status' = "SELECT COUNT(*) FROM article WHERE deleted = 0 AND status NOT IN ('DRAFT', 'PUBLISHED', 'HIDDEN')"
    'invalid_project_status' = "SELECT COUNT(*) FROM project WHERE deleted = 0 AND status NOT IN ('DRAFT', 'PLANNING', 'DEVELOPING', 'COMPLETED', 'PUBLISHED')"
}

$previousPassword = $env:MYSQL_PWD
$env:MYSQL_PWD = $DbPassword
try {
    foreach ($entry in $queries.GetEnumerator()) {
        $value = (& mysql --protocol=TCP --host=$DbHost --port=$DbPort --user=$DbUser --database=$DbName --batch --skip-column-names -e $entry.Value).Trim()
        if ($LASTEXITCODE -ne 0) {
            throw "Data audit query failed: $($entry.Key)"
        }
        [PSCustomObject]@{ Check = $entry.Key; Count = [int64]$value }
    }
} finally {
    $env:MYSQL_PWD = $previousPassword
}
