@echo off
%~d0
cd ..\..\
set outputPath=%cd%\serverProject\petServer\game-server\src\
set csPath=%cd%\serverProject\petServer\cross-server\src\protocol
set bsPath=%cd%\serverProject\petServer\battle-server\src\protocol
set robotPath=%cd%\serverProject\petServer\robot\src\petrobot\protocol
set protoFilePath=%~dp0\ProtoFile
set protocPath=%~dp0

:: del %outputPath%\*.* /f /s /q

for /f "delims=" %%i in ('dir/b "%protoFilePath%\*.proto"') do (
echo %%i
%~dp0protoc.exe --proto_path=%protoFilePath% --java_out=%outputPath% %%i
CALL:CHECK_FAIL
)

echo ===================================
echo gs path :%outputPath%
echo cs path :%csPath%
echo bs path :%bsPath%
echo rob path :%robotPath%
echo ===================================
xcopy %outputPath%\protocol %bsPath% /S /E /Y
xcopy %outputPath%\protocol %csPath% /S /E /Y
xcopy %outputPath%\protocol %robotPath% /S /E /Y
CALL:CHECK_FAIL
)

:CHECK_FAIL

if NOT ["%errorlevel%"]==["0"] (
    pause
    exit
)