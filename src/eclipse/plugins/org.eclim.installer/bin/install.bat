@echo off

rem Copyright (C) 2005 - 2009  Eric Van Dewoestine
rem
rem This program is free software: you can redistribute it and/or modify
rem it under the terms of the GNU General Public License as published by
rem the Free Software Foundation, either version 3 of the License, or
rem (at your option) any later version.
rem
rem This program is distributed in the hope that it will be useful,
rem but WITHOUT ANY WARRANTY; without even the implied warranty of
rem MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
rem GNU General Public License for more details.
rem
rem You should have received a copy of the GNU General Public License
rem along with this program.  If not, see <http://www.gnu.org/licenses/>.

set ECLIPSE_HOME=%~dp0\..\..\..

if %2 == listFeatures (
  "%ECLIPSE_HOME%\eclipse" -nosplash -clean -refresh -application org.eclipse.update.core.standaloneUpdate %*
) else (
  "%ECLIPSE_HOME%\eclipse" -nosplash -clean -application org.eclim.installer.application %*
)
