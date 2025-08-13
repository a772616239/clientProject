echo off
:redo
set /p file=拖入要导出的配置:
set rootPath=%~dp0../../
set toolPath=%rootPath%Tools/clientTools/ExcelImporter/ExcelToUnity.exe
set outputPath=%rootPath%clientProject/Assets/Bundles/ConfigData/
%toolPath% 3 %outputPath% %file% || pause
echo.
goto redo
pause