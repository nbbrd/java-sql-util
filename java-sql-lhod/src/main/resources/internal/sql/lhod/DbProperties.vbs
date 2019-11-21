Option Explicit
On Error Resume Next

Const en_US = 1033
Const adVarChar = 200
Const adModeRead = 1

SetLocale(en_US)

Dim connectionString : connectionString = Wscript.Arguments.Item(0)
Dim dynamicPropertyKeys : dynamicPropertyKeys = GetArgs(1, Wscript.Arguments.Count)

Dim csv : Set csv = new CsvWriter

Dim conn : Set conn = CreateObject("ADODB.Connection")
conn.Mode = adModeRead
conn.Open connectionString
Call CheckErr(csv)

Call PrintHead(csv)
Call PrintBody(csv, conn.Properties, dynamicPropertyKeys)
Call CheckErr(csv)

conn.Close : Set conn = Nothing

' --- specific code ---
  
Sub PrintHead(csv)
  csv.WriteField("Name")
  csv.WriteField("Value")
  csv.WriteEndOfLine()

  csv.WriteField(adVarChar)
  csv.WriteField(adVarChar)
  csv.WriteEndOfLine()
End Sub

Sub PrintBody(csv, properties, dynamicPropertyKeys)
  Dim prop
  If UBound(dynamicPropertyKeys) = 0 Then
    For Each prop In properties
      Call PrintProperty(csv, prop)
    Next
  Else
    For Each prop In properties
	  If IsRequiredProperty(prop, dynamicPropertyKeys) Then
        Call PrintProperty(csv, prop)
      End If
    Next
  End If
End Sub

Function IsRequiredProperty(prop, dynamicPropertyKeys)
  IsRequiredProperty = false
  Dim item : For Each item in dynamicPropertyKeys
    If StrComp(prop.Name, item) = 0 Then
	  IsRequiredProperty = true
	  Exit For
    End If
  Next
End Function

Sub PrintProperty(csv, prop)
  csv.WriteField(prop.Name)
  csv.WriteField(prop.Value)
  csv.WriteEndOfline()
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