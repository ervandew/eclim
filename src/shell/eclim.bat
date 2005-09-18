@echo off

rem Copyright (c) 2004 - 2005
rem
rem Licensed under the Apache License, Version 2.0 (the "License");
rem you may not use this file except in compliance with the License.
rem You may obtain a copy of the License at
rem
rem      http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.
rem
rem Author: Eric Van Dewoestine

if "%ECLIPSE_HOME%" == "" goto no_eclipse_home

setlocal EnableDelayedExpansion
set ECLIM_HOME=%ECLIPSE_HOME%\plugins\org.eclim_${eclim.version}

set JAVA_CP=.
for %%i in ("%ECLIM_HOME%\*.jar") do set JAVA_CP=!JAVA_CP!;%%i
endlocal & set JAVA_CP=%JAVA_CP%

if "%JAVA_CMD%" == "" set JAVA_CMD=java -cp "%JAVA_CP%"

%JAVA_CMD% org.eclim.client.Main %*
set JAVA_CMD=
goto exit

:no_eclipse_home
  echo ECLIPSE_HOME not set
  goto exit

:exit
