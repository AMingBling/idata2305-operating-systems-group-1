param(
    [int]$Count = 10,
    [string]$ServerHost = "127.0.0.1",
    [int]$Port = 5000,
    [string]$Request
)

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Push-Location $scriptDir

try {
    if (-not (Test-Path "Client.class") -or ((Get-Item "Client.java").LastWriteTime -gt (Get-Item "Client.class").LastWriteTime)) {
        Write-Host "Compiling Client.java..."
        & javac "Client.java"
        if ($LASTEXITCODE -ne 0) {
            throw "Compilation failed."
        }
    }

    for ($i = 1; $i -le $Count; $i++) {
        $command = if ([string]::IsNullOrWhiteSpace($Request)) {
            "Set-Location '$scriptDir'; Write-Host 'Client #$i'; java Client $ServerHost $Port"
        } else {
            "Set-Location '$scriptDir'; Write-Host 'Client #$i'; java Client $ServerHost $Port '$Request'"
        }

        Start-Process powershell -ArgumentList @(
            "-NoExit",
            "-Command",
            $command
        )
    }

    if ([string]::IsNullOrWhiteSpace($Request)) {
        Write-Host "Started $Count interactive client instances against $ServerHost`:$Port"
    } else {
        Write-Host "Started $Count client instances with auto request '$Request' against $ServerHost`:$Port"
    }
}
finally {
    Pop-Location
}
