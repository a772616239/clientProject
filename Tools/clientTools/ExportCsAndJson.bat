echo off
set isOnlyExportHotfix=%1

set rootPath=%~dp0../../

set toolPath=%rootPath%Tools/clientTools/ExcelImporter/ExcelToUnity.exe
set excelPath=%rootPath%Model/Excel/
set outputPath=%rootPath%clientProject/Assets/Bundles/ConfigData/
set codeoutputPath=%rootPath%clientProject/Assets/Scripts/Model/GameScripts/ExcelDefine/
set hotfixCodeOutputPath=%rootPath%clientProject/Assets/Scripts/Hotfix/GameDocument/Excel/ExcelDefine/
set logicConfigManagerPath=%rootPath%clientProject/Assets/Scripts/Model/Fight/FightHotfix/FightLogic_Hotfix/Core/Config/LogicConfigManager.g.cs
%toolPath% 1 %excelPath% %outputPath% %codeoutputPath% %hotfixCodeOutputPath% %logicConfigManagerPath% %isOnlyExportHotfix% || pause

set serverToolPath=%rootPath%Tools/serverTools/cfgTool/
cd %serverToolPath%
java -jar createAllJson.jar

pause