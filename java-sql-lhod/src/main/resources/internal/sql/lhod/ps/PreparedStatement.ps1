Set-StrictMode -Version 2.0

Set-Variable adModeRead -Option Constant -Value 1
Set-Variable adVarChar -Option Constant -Value 200
Set-Variable adParamInput -Option Constant -Value 1

function Print-Head( [CsvWriter] $csv, $rs ) {
    foreach ($field in $rs.Fields) {
        $csv.WriteField($field.Name)
    }
    $csv.WriteEndOfLine()
    foreach ($field in $rs.Fields) {
        $csv.WriteField($field.Type)
    }
    $csv.WriteEndOfLine()
}

function Print-Body( [CsvWriter] $csv, $rs ) {
    while ($rs.EOF -eq $false) {
        foreach ($field in $rs.Fields) {
            $csv.WriteField($field.Value)
        }
        $csv.WriteEndOfLine()
        $rs.MoveNext()
    }
}

[System.Threading.Thread]::CurrentThread.CurrentCulture = "en_US"

$connectionString = $args[0]
$sql = $args[1]
$params = $args[2..$args.Length]

$csv = New-Object CsvWriter
$conn = New-Object -ComObject ADODB.Connection
try {
    $conn.Mode = $adModeRead
    $conn.Open($connectionString)

    $cmd = New-Object -ComObject ADODB.Command
    try {
        $cmd.ActiveConnection = $conn
        $cmd.CommandText = "$sql"

        for ($i = 0; $i -lt $params.Length; $i++) {
            $cmd.Parameters.Append($cmd.CreateParameter("p$i", $adVarChar, $adParamInput, $params[$i].Length, $params[$i]))
        }

        $rs = $cmd.Execute()
        try {
            Print-Head $csv $rs
            Print-Body $csv $rs
        } catch {
            [Helper]::PrintError($csv, $_.Exception)
            Exit(3)
        } finally {
            [Helper]::CloseResource($rs)
        }
    } catch {
        [Helper]::PrintError($csv, $_.Exception)
        Exit(2)
    } finally {
        [Helper]::CloseResource($cmd)
    }
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
