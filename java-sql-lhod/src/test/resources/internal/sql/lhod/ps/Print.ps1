
function Print-Error( $error ) {
    echo ""
    [PSCustomObject]@{ Number = 0; Description = $error.Message } | ConvertTo-Csv -NoTypeInformation -Delimiter "`t" | Select-Object -skip 1
}

$file = [Helper]::DecodeArg($args[0])

try {
    Get-Content $file -ErrorAction Stop
} catch {
    Print-Error($_.Exception)
    Exit(1)
}

class Helper {
    static [string] DecodeArg( [string] $argument ) {
        if ($argument -like "base64_*") {
            return [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($argument.Substring(7)))
        } else {
            return $argument
        }
    }
}
