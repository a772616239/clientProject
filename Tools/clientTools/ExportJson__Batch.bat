echo off
set rootPath=%~dp0../../
set toolPath=%rootPath%Tools/clientTools/ExcelImporter/ExcelToUnity.exe
set excelPath=%rootPath%Model/Excel/
set outputPath=%rootPath%clientProject/Assets/Bundles/ConfigData/
%toolPath% 2 %excelPath% %outputPath% || pause

set serverToolPath=%rootPath%Tools/serverTools/cfgTool/
cd %serverToolPath%
java -jar createAllJson.jar

pause