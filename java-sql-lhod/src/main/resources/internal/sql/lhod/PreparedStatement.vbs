Option Explicit
On Error Resume Next

Const en_US = 1033
Const adModeRead = 1
Const adVarChar = 200
Const adParamInput = 1

SetLocale(en_US)

Dim connectionString : connectionString = Wscript.Arguments.Item(0)
Dim sql : sql = Wscript.Arguments.Item(1)
Dim params : params = GetArgs(2, Wscript.Arguments.Count)

Dim csv : Set csv = new CsvWriter

Dim conn : Set conn = CreateObject("ADODB.Connection")
conn.Mode = adModeRead
conn.Open connectionString
Call CheckErr(csv)

Dim cmd : Set cmd = CreateObject("ADODB.Command")
cmd.ActiveConnection = conn
cmd.CommandText = sql
Call CheckErr(csv)

Dim i : For i = 0 To UBound(params) - 1
  cmd.Parameters.Append cmd.CreateParameter("p" & i, adVarChar, adParamInput, Len(params(i)), params(i))
Next
Call CheckErr(csv)

Dim rs : Set rs = cmd.Execute
Call CheckErr(csv)

Call PrintHead(csv, rs)
Call PrintBody(csv, rs)
Call CheckErr(csv)

rs.Close : Set rs = Nothing
cmd.Close : Set cmd = Nothing
conn.Close : Set conn = Nothing

' --- specific code ---

Sub PrintHead(csv, rs)
  Dim field
  
  For Each field in rs.Fields
    csv.WriteField(field.Name)
  Next
  csv.WriteEndOfLine()

  For Each field in rs.Fields
    csv.WriteField(field.Type)
  Next
  csv.WriteEndOfLine()
End Sub

Sub PrintBody(csv, rs)
  If Not (rs.EOF) Then
    Do Until rs.EOF
      Dim field
      For Each field in rs.Fields
        csv.WriteField(field.Value)
      Next
      csv.WriteEndOfLine()
      rs.MoveNext
    Loop 
  End If
End Sub

' --- generic code ---

Sub CheckErr(csv)
  If Err.Number <> 0 Then
    csv.WriteEndOfLine()
    csv.WriteField(Err.Number)
    csv.WriteField(Err.Description)
    Wscript.quit(1)
  End If
End Sub

Class CsvWriter
  Private STATE_NO_FIELD, STATE_SINGLE_EMPTY_FIELD, STATE_MULTI_FIELD
  Private QUOTING_NONE, QUOTING_PARTIAL, QUOTING_FULL

  Private quote
  Private delimiter
  Private endOfLine

  Private state
  
  Private Sub Class_Initialize()
    ' State enum
    STATE_NO_FIELD = 0
    STATE_SINGLE_EMPTY_FIELD = 1
    STATE_MULTI_FIELD = 2
    ' Quoting enum
    QUOTING_NONE = 0
    QUOTING_PARTIAL = 1
    QUOTING_FULL = 2
    ' Csv format
    quote = Chr(34) ' double quotes
    delimiter = vbTab
    endOfLine = vbCrLf
    ' Initiol state
    state = STATE_NO_FIELD
  End Sub
  
  Public Sub WriteField(field)
    Select Case state
      Case STATE_NO_FIELD
        If (IsNonEmptyField(field)) Then
          state = STATE_MULTI_FIELD
          WriteNonEmptyField(field)
        Else
          state = STATE_SINGLE_EMPTY_FIELD
        End If
      Case STATE_SINGLE_EMPTY_FIELD
        state = STATE_MULTI_FIELD
        WScript.StdOut.Write delimiter
        If (IsNonEmptyField(field)) Then
          WriteNonEmptyField(field)
        End If
      Case STATE_MULTI_FIELD
        WScript.StdOut.Write delimiter
        If (IsNonEmptyField(field)) Then
          WriteNonEmptyField(field)
        End If
    End Select
  End Sub
  
  Public Sub WriteEndOfLine()
    FlushField()
    WScript.StdOut.Write endOfLine
  End Sub

  Private Function IsNonEmptyField(field)
    IsNonEmptyField = Len(field & "") > 0
  End Function

  Private Sub WriteNonEmptyField(field)
    Select Case GetQuoting(field)
      Case QUOTING_NONE
        WScript.StdOut.Write field
      Case QUOTING_PARTIAL
        WScript.StdOut.Write quote
        WScript.StdOut.Write field
        WScript.StdOut.Write quote
      Case QUOTING_FULL
        WScript.StdOut.Write quote
        Dim c
        Dim i : For i = 1 To Len(field)
          c = Mid(field, i, 1)
          If (c = quote) Then
            WScript.StdOut.Write quote
          End If
          WScript.StdOut.Write c
        Next
        WScript.StdOut.Write quote
    End Select
  End Sub

  Private Sub FlushField()
    If (state = STATE_SINGLE_EMPTY_FIELD) Then
      WScript.StdOut.Write quote
      WScript.StdOut.Write quote
    End If
    state = STATE_NO_FIELD
  End Sub

  Private Function GetQuoting(field)
    GetQuoting = QUOTING_NONE
    Dim c
    Dim i : For i = 1 To Len(field)
      c = Mid(field, i, 1)
      If (c = quote) Then
        GetQuoting = QUOTING_FULL
        Exit Function
      End If
      If ((c = delimiter) Or IsNewLine(c)) Then
        GetQuoting = QUOTING_PARTIAL
      End If
    Next
  End Function

  Private Function IsNewLine(c)
    IsNewLine = (c = vbCr) Or (c = vbLf)
  End Function
End Class

Function GetArgs(starting, ending)
  Dim out_array
  out_array = Array()

  If ending >= starting Then
    ReDim Preserve out_array(ending - starting)
    Dim i : For i = starting To ending - 1
      out_array(i - starting) = Wscript.Arguments.Item(i)
    Next
  End If

  GetArgs = out_array
End Function