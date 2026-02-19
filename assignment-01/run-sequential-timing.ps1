# Run 10 sequential client connections to single server
# Each client connects, sends "3 + 7", and disconnects
# Measures total elapsed time

$root = "C:\Users\there\OneDrive\Documents\GitHub\idata2305-operating-systems-group-1\assignment-01"
$java = "C:\Program Files\Java\jdk-22\bin\java.exe"

Set-Location $root

# Start timing
$startTime = Get-Date

# Run 10 sequential clients
for ($i = 1; $i -le 10; $i++) {
    Write-Host "Client $i connecting..."
    "3 + 7`nQuit" | & $java Client 127.0.0.1 5000
    # "3 + 7" | & $java Client 127.0.0.1 5000
}

# End timing
$endTime = Get-Date
$elapsed = $endTime - $startTime

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "Total time for 10 sequential clients: $($elapsed.TotalMilliseconds) ms" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
