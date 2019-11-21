Option Explicit
On Error Resume Next

Const en_US = 1033
Const adSchemaTables = 20
Const adModeRead = 1

SetLocale(en_US)

Dim connectionString : connectionString = Wscript.Arguments.Item(0)
Dim catalog : catalog = GetArgOrEmpty(1)
Dim schema : schema = GetArgOrEmpty(2)
Dim tableName : tableName = GetArgOrEmpty(3)
Dim types : types = GetArgs(4, Wscript.Arguments.Count)

Dim csv : Set csv = new CsvWriter

Dim conn : Set conn = CreateObject("ADODB.Connection")
conn.Mode = adModeRead
conn.Open connectionString
Call CheckErr(csv)

Dim rs : Set rs = conn.OpenSchema(adSchemaTables, Array(catalog, schema, tableName, Empty))
Call CheckErr(csv)

Call PrintHead(csv, rs)
Call PrintBody(csv, rs, types)
Call CheckErr(csv)

rs.Close : Set rs = Nothing
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

Sub PrintBody(csv, rs, types)
  If Not (rs.EOF) Then
    Dim field
    Do Until rs.EOF
      If IsValidType(rs.Fields, types) Then
        For Each field in rs.Fields
          csv.WriteField(field.Value)
        Next
		csv.WriteEndOfLine()
      End If
      rs.MoveNext
    Loop 
  End If
End Sub

Function IsValidType(fields, types)
  IsValidType = true
  If UBound(types) > 0 Then
    Dim i : For Each i in types
      If fields("TABLE_TYPE") = i Then
        Exit Function
      End If
    Next
    IsValidType = false
  End If
End Function

Function GetArgOrEmpty(position)
  If Wscript.Arguments.Count > position Then
    If Len(Wscript.Arguments.Item(position)) > 0 Then
      GetArgOrEmpty = Wscript.Arguments.Item(position)
    Else
      GetArgOrEmpty = Empty
    End If
  Else
    GetArgOrEmpty = Empty
  End If
End Function

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