echo off
:redo
set /p file=����Ҫ����������:
set rootPath=%~dp0../../
set toolPath=%rootPath%Tools/clientTools/ExcelImporter/ExcelToUnity.exe
set outputPath=%rootPath%clientProject/Assets/Bundles/ConfigData/
%toolPath% 3 %outputPath% %file% || pause
echo.
goto redo
pause