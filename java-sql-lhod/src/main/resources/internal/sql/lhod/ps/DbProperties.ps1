Set-StrictMode -Version 2.0

Set-Variable adVarChar -Option Constant -Value 200
Set-Variable adModeRead -Option Constant -Value 1

function Print-Head( [CsvWriter] $csv ) {
    $csv.WriteField("Name")
    $csv.WriteField("Value")
    $csv.WriteEndOfLine()

    $csv.WriteField($adVarChar)
    $csv.WriteField($adVarChar)
    $csv.WriteEndOfLine()
}

function Print-Body( [CsvWriter] $csv, $properties, $dynamicPropertyKeys ) {
    if ($dynamicPropertyKeys.Length -eq 0) {
        foreach($prop in $properties) {
            Print-Property $csv $prop
        }
    } else {
        foreach($prop in $properties) {
            if ($prop.Name -in $dynamicPropertyKeys) {
                Print-Property $csv $prop
            }
        }
    }
}

function Print-Property( [CsvWriter] $csv, $prop ) {
    $csv.WriteField($prop.Name)
    $csv.WriteField($prop.Value)
    $csv.WriteEndOfLine()
}

[System.Threading.Thread]::CurrentThread.CurrentCulture = "en_US"

$connectionString = $args[0]
$dynamicPropertyKeys = $args[1..$args.Length]

$csv = New-Object CsvWriter
$conn = New-Object -ComObject ADODB.Connection
try {
    $conn.Mode = $adModeRead
    $conn.Open($connectionString)

    Print-Head $csv
    Print-Body $csv $conn.Properties $dynamicPropertyKeys
} catch {
    [Helper]::PrintError($csv, $_.Exception)
    Exit(1)
} finally {
    [Helper]::CloseResource($conn)
}

# --- generic code ---

class Helper {
    static [int] $adStateOpen = 1

    static [void] PrintError( [CsvWriter] $csv, $error ) {
        $csv.WriteEndOfLine()
        $csv.WriteField($error.ErrorCode)
        $csv.WriteField($error.Message)
        $csv.WriteEndOfLine()
    }

    static [void] CloseResource( $resource ) {
        if ($resource.State -eq [Helper]::adStateOpen) { $resource.Close() }
    }
}

enum STATE { NO_FIELD; SINGLE_EMPTY_FIELD; MULTI_FIELD }

class CsvWriter {
    [string] $quote
    [string] $delimiter
    [string] $endOfLine

    [STATE] $state
    [string] $buffer

    CsvWriter() {
        # Csv format
        $this.quote = '"'
        $this.delimiter = "`t"
        $this.endOfLine = "`r`n"
        # Initial state
        $this.state = [STATE]::NO_FIELD
        $this.buffer = ""
    }

    [void] WriteField( [string] $field ) {
        switch($this.state) {
            ([STATE]::NO_FIELD) {
                if ($this.IsNonEmptyField($field)) {
                    $this.state = [STATE]::MULTI_FIELD
                    $this.WriteNonEmptyField($field)
                } else {
                    $this.state = [STATE]::SINGLE_EMPTY_FIELD
                }
            }
            ([STATE]::SINGLE_EMPTY_FIELD) {
                $this.state = [STATE]::MULTI_FIELD
                $this.buffer += $this.delimiter
                if ($this.IsNonEmptyField($field)) {
                    $this.WriteNonEmptyField($field)
                }
            }
            ([STATE]::MULTI_FIELD) {
                $this.buffer += $this.delimiter
                if ($this.IsNonEmptyField($field)) {
                    $this.WriteNonEmptyField($field)
                }
            }
        }
    }

    [void] WriteEndOfLine() {
        $this.FlushField()
        $this.buffer += $this.endOfLine
        Write-Host ($this.buffer) -NoNewline
        $this.buffer = ""
    }

    [boolean] IsNonEmptyField( [string] $field ) {
        return ($field.length -ne "")
    }

    [void] WriteNonEmptyField( [string] $field ) {
        $this.buffer += $this.quote
        foreach ($char in $field.ToCharArray()) {
            if ($char -eq $this.quote) {
                $this.buffer += $this.quote
            }
            $this.buffer += $char
        }
        $this.buffer += $this.quote
    }

    [void] FlushField() {
        if ($this.state -eq [STATE]::SINGLE_EMPTY_FIELD) {
            $this.buffer += $this.quote
            $this.buffer += $this.quote
        }
        $this.state = [STATE]::NO_FIELD
    }
}
