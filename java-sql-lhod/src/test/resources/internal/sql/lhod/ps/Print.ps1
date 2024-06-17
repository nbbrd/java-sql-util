
function Print-Error( $error ) {
    echo ""
    [PSCustomObject]@{ Number = 0; Description = $error.Message } | ConvertTo-Csv -NoTypeInformation -Delimiter "`t" | Select-Object -skip 1
}

$file = $args[0]

try {
    Get-Content $file -ErrorAction Stop
} catch {
    Print-Error($_.Exception)
    Exit(1)
}
