[CmdletBinding()]
param(
    [string]$PlatformBaseUrl = 'http://127.0.0.1:8080',
    [string]$PlatformApiKey,
    [string]$DbHost = '127.0.0.1',
    [int]$DbPort = 3306,
    [string]$DbName = 'operation_management',
    [string]$DbUser,
    [string]$DbPassword
)

$ErrorActionPreference = 'Stop'
$configDir = Join-Path $env:LOCALAPPDATA 'brrp-codex\operation-indicator-query'
New-Item -ItemType Directory -Force -Path $configDir | Out-Null

if ([string]::IsNullOrWhiteSpace($PlatformApiKey)) {
    $platformSecret = Read-Host 'Platform API Key' -AsSecureString
} else {
    $platformSecret = ConvertTo-SecureString $PlatformApiKey -AsPlainText -Force
}
if ([string]::IsNullOrWhiteSpace($DbUser)) { $DbUser = Read-Host 'Business database user' }
if ([string]::IsNullOrWhiteSpace($DbPassword)) {
    $dbSecret = Read-Host 'Business database password' -AsSecureString
} else {
    $dbSecret = ConvertTo-SecureString $DbPassword -AsPlainText -Force
}

[pscredential]::new('api-key', $platformSecret) |
    Export-Clixml -LiteralPath (Join-Path $configDir 'platform-api-key.xml')
[pscredential]::new($DbUser, $dbSecret) |
    Export-Clixml -LiteralPath (Join-Path $configDir 'business-db.xml')

$config = [ordered]@{
    platform_base_url = $PlatformBaseUrl.TrimEnd('/')
    db_host = $DbHost
    db_port = $DbPort
    db_name = $DbName
    observation_binding = '运管-运营指标观测绑定'
    organization_binding = '运管-管控对象绑定'
    indicator_binding = '运管-指标定义绑定'
    connect_timeout_seconds = 5
    query_timeout_seconds = 10
}
$config | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $configDir 'config.json') -Encoding utf8

$PlatformApiKey = $null
$DbPassword = $null
Write-Output "Configured operation-indicator-query in $configDir"
