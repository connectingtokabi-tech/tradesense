$ErrorActionPreference = 'Stop'

$dbPasswordFile = Join-Path $PSScriptRoot 'tradesense_database_password.txt'
$authPasswordFile = Join-Path $PSScriptRoot 'tradesense_auth_password.txt'

foreach ($secret in @(
    @{ Name = 'TRADESENSE_DATABASE_PASSWORD'; File = $dbPasswordFile },
    @{ Name = 'TRADESENSE_AUTH_PASSWORD'; File = $authPasswordFile }
)) {
    if (-not (Test-Path $secret.File)) {
        throw "Missing secret file: $($secret.File)"
    }

    $value = (Get-Content $secret.File -Raw).Trim()
    if ([string]::IsNullOrWhiteSpace($value)) {
        throw "Secret file is empty: $($secret.File)"
    }

    Set-Item -Path "Env:$($secret.Name)" -Value $value
}

Write-Host 'Loaded TradeSense secret environment variables for this PowerShell session.'
