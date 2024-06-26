Option Explicit
On Error Resume Next

Const ForReading = 1

Dim file : file = Wscript.Arguments.Item(0)

Dim objFSO : Set objFSO = CreateObject("Scripting.FileSystemObject")
Dim objFile : Set objFile = objFSO.OpenTextFile(file, ForReading)

if Err then
    WScript.StdOut.Write Err.description
else
    WScript.StdOut.Write objFile.ReadAll
end if

objFile.close
