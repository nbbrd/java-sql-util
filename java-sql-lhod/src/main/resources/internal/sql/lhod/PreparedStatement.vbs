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
  private quote
  private requiresDelimiter
  
  Private Sub Class_Initialize()
    quote = Chr(34)
    requiresDelimiter = false
  End Sub
  
  Public Sub WriteField(field)
    If requiresDelimiter Then
	  WScript.StdOut.Write vbTab
	End If
	requiresDelimiter = true
	WScript.StdOut.Write field & ""
  End Sub
  
  Public Sub WriteEndOfLine()
    WScript.StdOut.Write vbCrLf
	requiresDelimiter = false
  End Sub
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