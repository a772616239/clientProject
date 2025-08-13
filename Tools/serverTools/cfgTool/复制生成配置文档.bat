@echo off
%~d0
cd ..\..\..\
set gsPath=%cd%\serverProject\petServer\game-server\cfg\
set csPath=%cd%\serverProject\petServer\cross-server\cfg\
set bsPath=%cd%\serverProject\petServer\battle-server\cfg\
set sourceFilePath=%~dp0\exportPack

echo ===================================
echo sourceFilePath : %sourceFilePath%
echo gs path :%gsPath%
echo cs path :%csPath%
echo bs path :%bsPath%
echo ===================================

xcopy %sourceFilePath%\*.json %gsPath% /S /E /Y
CALL:CHECK_FAIL
xcopy %sourceFilePath%\*.json %csPath% /S /E /Y
CALL:CHECK_FAIL
xcopy %sourceFilePath%\*.json %bsPath% /S /E /Y
CALL:CHECK_FAIL
del %sourceFilePath%\*.json /f /s /q
CALL:CHECK_FAIL

:CHECK_FAIL

if NOT ["%errorlevel%"]==["0"] (
    pause
    exit
)