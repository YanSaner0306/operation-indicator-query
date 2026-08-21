[CmdletBinding()]
param(
    [string]$PlatformBaseUrl = 'http://127.0.0.1:8080',
    [string]$PlatformApiKey,
    [string]$ApiKeyEnv = 'BRRP_OPERATION_API_KEY'
)

$ErrorActionPreference = 'Stop'
$configDir = Join-Path $env:LOCALAPPDATA 'brrp-codex\operation-indicator-query'
New-Item -ItemType Directory -Force -Path $configDir | Out-Null

if ([string]::IsNullOrWhiteSpace($PlatformApiKey)) {
    $PlatformApiKey = [Environment]::GetEnvironmentVariable($ApiKeyEnv)
}
if ([string]::IsNullOrWhiteSpace($PlatformApiKey)) {
    $PlatformApiKey = Read-Host 'Platform API Key'
}
Set-Content -LiteralPath (Join-Path $configDir 'platform-api-key.txt') -Value $PlatformApiKey -Encoding utf8 -NoNewline

$config = [ordered]@{
    platform_base_url = $PlatformBaseUrl.TrimEnd('/')
    api_key_env = $ApiKeyEnv
    object_ontology_code = 'ORGANIZATION'
    indicator_ontology_code = 'INDICATOR_DEF'
    observation_ontology_code = 'INDICATOR_OBSERVATION'
    object_id_property = 'OBJECT_ID'
    object_code_property = 'OBJECT_CODE'
    object_name_property = 'OBJECT_NAME'
    object_short_name_property = 'SHORT_NAME'
    indicator_code_property = 'INDICATOR_CODE'
    indicator_name_property = 'INDICATOR_NAME'
    indicator_unit_property = 'UNIT'
    observation_object_id_property = 'OBJECT_ID'
    observation_indicator_code_property = 'INDICATOR_CODE'
    observation_period_property = 'PERIOD'
    observation_unit_property = 'UNIT'
    observation_key_template = '{period}|{object_id}|{indicator_code}'
    current_value_property = 'CURRENT_VALUE'
    previous_value_property = 'PREVIOUS_VALUE'
    yoy_amount_property = 'YOY_AMOUNT'
    yoy_rate_property = 'YOY_RATE'
    connect_timeout_seconds = 10
}
$config | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $configDir 'config.json') -Encoding utf8

Write-Output "Configured operation-indicator-query in $configDir"
