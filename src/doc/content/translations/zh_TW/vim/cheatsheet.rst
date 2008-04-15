.. Copyright (C) 2005 - 2008  Eric Van Dewoestine

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.

.. _translations/zh_TW/vim/cheatsheet:

Eclim Vim 指令參考
==================

以下包含所有在 Eclim 中可以使用的指令，並提供簡要參考用法.

全區域指令集
--------------

- <a href="index.html#PingEclim">**:PingEclim**</a> -
  連接 eclimd 伺服器.
- <a href="index.html#ShutdownEclim">**:ShutdownEclim**</a> -
  關閉 eclimd 伺服器.
- <a href="index.html#EclimSettings">**:EclimSettings**</a> -
  瀏覽/編輯全區域設定選項.


Project 專案指令集
------------------

- <a href="common/project.html#ProjectCreate">**:ProjectCreate**</a>
  <資料夾> [-p <專案名稱>] -n <nature> ... [-d <依存的專案> ...] -
  建立新專案.
- <a href="common/project.html#ProjectList">**:ProjectList**</a> -
  顯示目前專案清單.
- <a href="common/project.html#ProjectSettings">**:ProjectSettings**</a>
  [<專案名稱>] - 瀏覽/編輯專案設定選項.
- <a href="common/project.html#ProjectDelete">**:ProjectDelete**</a>
  <專案名稱> - 刪除指定的專案.
- <a href="common/project.html#ProjectRefresh">**:ProjectRefresh**</a>
  [<專案名稱> <專案名稱> ...] -
  更新列表中或所有的專案，這將會更新設定值至實際磁碟檔案中.
- <a href="common/project.html#ProjectRefreshAll">**:ProjectRefreshAll**</a> -
  同 :ProjectRefreshAll 指令，但更新所有的專案.
- <a href="common/project.html#ProjectOpen">**:ProjectOpen**</a>
  <專案名稱> - 開啟舊專案.
- <a href="common/project.html#ProjectClose">**:ProjectClose**</a>
  <專案名稱> - 關閉專案.
- <a href="common/project.html#ProjectCD">**:ProjectCD**</a>
  - 改變全區域的工作目錄至目前檔案所在的專案目錄(即執行 :cd).
- <a href="common/project.html#ProjectLCD">**:ProjectLCD**</a>
  - 改變目前工作目錄至目前檔案所在的專案目錄(即執行 :lcd).
- <a href="common/project.html#ProjectTree">**:ProjectTree**</a>
  [<專案名稱> <專案名稱> ...] - 針對一個或多個專案開啟可導覽的樹狀結構表.
- <a href="common/project.html#ProjectsTree">**:ProjectsTree**</a> -
  對於所有的專案開啟一份可導覽的樹狀結構表.
- <a href="common/project.html#ProjectGrep">**:ProjectGrep**</a>
  /<pattern>/ file_pattern [file_pattern ...] -
  在專案根目錄使用 :vim[grep] 指令功能.
- <a href="common/project.html#ProjectGrepAdd">**:ProjectGrepAdd**</a>
  /<pattern>/ file_pattern [file_pattern ...] -
  在專案根目錄使用 :vimgrepa[dd] 指令功能.
- <a href="common/project.html#ProjectLGrep">**:ProjectLGrep**</a>
  /<pattern>/ file_pattern [file_pattern ...] -
  在專案根目錄使用 :lv[imgrep] 指令功能.
- <a href="common/project.html#ProjectLGrepAdd">**:ProjectLGrepAdd**</a>
  /<pattern>/ file_pattern [file_pattern ...] -
  在專案根目錄使用 :lvimgrepa[dd] 指令功能.


Eclipse .classpath 維護指令集
-----------------------------

- <a href="java/classpath.html#NewSrcEntry">**:NewSrcEntry**</a>
  <目錄> [<目錄> ...] -
  新增一個原始碼目前節點.
- <a href="java/classpath.html#NewProjectEntry">**:NewProjectEntry**</a>
  <專案名稱> [<專案名稱> ...] -
  新增一個專案節點.
- <a href="java/classpath.html#NewJarEntry">**:NewJarEntry**</a>
  <檔案> [<檔案> ...] -
  新增一個 .jar 檔案節點.
- <a href="java/classpath.html#NewVarEntry">**:NewVarEntry**</a>
  <參數/檔案> [<參數/檔案> ...] -
  新增一個參數節點.
- <a href="java/classpath.html#VariableList">**:VariableList**</a>
  列出可使用的 classpath 參數及相對應的值.
- <a href="java/classpath.html#VariableCreate">**:VariableCreate**</a>
  <名稱> <路徑> - 建立或修改一個名稱的變數.
- <a href="java/classpath.html#VariableDelete">**:VariableDelete**</a>
  <名稱> - 刪除指定名稱的變數.


Ant 指令集
--------------

- <a href="java/ant/run.html#Ant">**:Ant**</a>
  [<目標> ...] -
  在目前專案設定下，執行 ant.
- <a href="java/ant/doc.html#AntDoc">**:AntDoc**</a>
  [<元素>] -
  以目前游標位置的元素或指定元素尋找並開啟文件檔案.
- <a href="java/ant/validate.html#Validate">**:Validate**</a> -
  驗證目前的 ant 檔案.


DTD 指令集
--------------

- <a href="dtd/validate.html#Validate">**:Validate**</a> -
  驗證目前的 DTD 檔案.


HTML 指令集
--------------

- <a href="dtd/validate.html#Validate">**:Validate**</a> -
  驗證目前的 HTML 檔案.


Ivy Commands
--------------

- <a href="../guides/java/ivy/ivy_classpath.html#IvyRepo">**:IvyRepo**</a>
  <路徑> -
  設定必須的 IVY_REPO classpath 參數予會自動更新 .classpath 檔案的 ``ivy.xml`` 設定檔.
- <a href="../guides/java/ivy/ivy_classpath.html#IvyDependencySearch">**:IvyDependencySearch**</a>
  <artifact> -
  尋找線上的資料庫，並將結果列示在一個視窗內，使用者可經由按下<Enter>鍵將結果加入目前專案內。在編輯 ``ivy.xml`` 檔案時可以使用本指令.


Java 指令集
--------------

- <a href="java/bean.html#JavaGet">**:JavaGet**</a> -
  建立 java bean getter 方法.
- <a href="java/bean.html#JavaSet">**:JavaSet**</a> -
  建立 java bean setter 方法.
- <a href="java/bean.html#JavaGetSet">**:JavaGetSet**</a> -
  建立 java bean getter 及 setter 方法.
- <a href="java/constructor.html#JavaConstructor">**:JavaConstructor**</a> -
  建立類別的建構子，內容為空或以選擇的欄位建立預設值.
- <a href="java/impl.html#JavaImpl">**:JavaImpl**</a> -
  自 super class 及實作的 interface 列示可實作/可重載的方法.
- <a href="java/delegate.html#JavaDelegate">**:JavaDelegate**</a> -
  列示操作目前游標欄位的方法.
- <a href="java/junit.html#JUnitImpl">**:JUnitImpl**</a> -
  類似 **:JavaImpl** 的動作, 但建立的方法為測試用途.
- <a href="java/junit.html#JUnitExecute">**:JUnitExecute**</a> - [測試例子]
  以常用的建置工具執行測試例子.
- <a href="java/junit.html#JUnitResult">**:JUnitResult**</a> - [測試例子]
  檢視測試例子的執行結果.
- <a href="java/import.html#JavaImport">**:JavaImport**</a> -
  Import 目前游標位置的 class.
- <a href="java/search.html#JavaSearch">**:JavaSearch**</a>
  [-p <pattern>] [-t <type>] [-x <內文內容>] -
  尋找類別, 方法, 欄位等(使用 pattern 支持，尋找目前游標位置的元素).
- <a href="java/search.html#JavaSearchContext">**:JavaSearchContext**</a> -
  執行目前游標位置元素的內文搜尋.
- <a href="java/correct.html#JavaCorrect">**:JavaCorrect**</a> -
  建議應修正的程式碼.
- <a href="java/regex.html#JavaRegex">**:JavaRegex**</a> -
  開啟一個視窗，進行 regular ecpression 測試.
- <a href="java/doc.html#JavaDocSearch">**:JavaDocSearch**</a> -
  尋找 javadoc，如同 **:JavaSearch** 指令.
- <a href="java/doc.html#JavaDocComment">**:JavaDocComment**</a> -
  新增或修改目前游標位置元素的註解.
- <a href="java/tools.html#Jps">**:Jps**</a> -
  開啟目前 java 運作資訊的視窗.


Log4j 指令集
--------------

- <a href="java/log4j/validate.html#Validate">**:Validate**</a> -
  驗證目前的 log4j 組態設定檔案.


Maven 指令集
--------------

- <a href="java/maven/run.html#Maven">**:Maven**</a>
  [<目標> ...] -
  在目前的專案設定下執行 maven 1.x.
- <a href="java/maven/run.html#Mvn">**:Mvn**</a>
  [<目標> ...] -
  在目前的專案設定下執行 maven 2.x.
- <a href="../guides/java/maven/maven_classpath.html#MavenRepo">**:MavenRepo**</a>
  - 設定必須的 MAVEN_REPO classpath 參數予帶有 eclipse 支持的 maven's (1.x).
- <a href="../guides/java/maven/mvn_classpath.html#MvnRepo">**:MvnRepo**</a>
  - 設定必須的 M2_REPO classpath 參數予帶有 eclipse 支持的 maven's (2.x).
- <a href="java/maven/dependency.html#MavenDependencySearch">**:MavenDependencySearch**</a>
  <artifact> -
  尋找線上的資料庫，並將結果列示在一個視窗內，使用者可經由按下<Enter>鍵將結果加入目前專案內。在編輯 maven 1.x ``project.xml`` 檔案時可以使用本指令.
- <a href="java/maven/dependency.html#MvnDependencySearch">**:MvnDependencySearch**</a>
  <artifact> -
  尋找線上的資料庫，並將結果列示在一個視窗內，使用者可經由按下<Enter>鍵將結果加入目前專案內。在編輯 maven 2.x ``pom.xml`` 檔案時可以使用本指令.


Python 指令集
--------------

- <a href="python/regex.html#PythonRegex">**:PythonRegex**</a> -
  開啟一個視窗來測試 python regular expressions.
- <a href="python/django.html#DjangoManage">**:DjangoManage**</a> -
  存在 manage.py 的同一個目錄下或子目錄下，自任何檔案中呼叫 django's ``manage.py``.


Vim 指令集
--------------

- <a href="vim/find.html#FindCommandDef">**:FindCommandDef**</a>
  [<command>] -
  尋找 command 的定義.
- <a href="vim/find.html#FindCommandRef">**:FindCommandRef**</a>
  [<command>] -
  尋找 command 的參考.
- <a href="vim/find.html#FindFunctionDef">**:FindFunctionDef**</a>
  [<function>] -
  尋找函數的定義.
- <a href="vim/find.html#FindFunctionRef">**:FindFunctionRef**</a>
  [<function>] -
  尋找函數的參考.
- <a href="vim/find.html#FindVariableDef">**:FindVariableDef**</a>
  [<變數名稱>] -
  尋找全區域變數的定義
- <a href="vim/find.html#FindVariableRef">**:FindVariableRef**</a>
  [<變數名稱>] -
  尋找全區域變數的參考.
- <a href="vim/find.html#FindByContext">**:FindByContext**</a> -
  以游標位置元素在內文中尋找 command, 函數或變數.
- <a href="vim/doc.html#VimDoc">**:VimDoc**</a>
  [<關鍵字>] -
  以關鍵字開啟相關的 vim 說明文件.


WebXml 指令集
--------------

- <a href="java/webxml/validate.html#Validate">**:Validate**</a> -
  驗證目前的 ``web.xml`` 檔案.


Wsdl 指令集
--------------

- <a href="dtd/validate.html#Validate">**:Validate**</a> -
  驗證目前的檔案.


Xml 指令集
--------------

- <a href="xml/definition.html#DtdDefinition">**:DtdDefinition**</a>
  [<元素>] -
  開啟目前 XML 檔案的 dtd 檔案，並移置指定元素的定義.
- <a href="xml/definition.html#XsdDefinition">**:XsdDefinition**</a>
  [<元素>] -
  開啟目前 XML 檔案的 xsd 檔案，並移置指定元素的定義.
- <a href="xml/validate.html#Validate">**:Validate**</a>
  [<檔案>] -
  驗證指定的 XML 檔案或目前的 XML 檔案.
- <a href="xml/format.html#XmlFormat">**:XmlFormat**</a>
  重新編排目前的 XML 檔案.


Xsd 指令集
--------------

- <a href="dtd/validate.html#Validate">**:Validate**</a> -
  驗證目前的檔案.


版本控制指令集
--------------

.. note::

  目前這項指令集只支援 CVS 及 subversion 兩種系統.

- <a href="common/vcs.html#VcsAnnotate">**:VcsAnnotate**</a> -
  使用 vim 簽名對目前版本檔案作注釋.
- <a href="common/vcs.html#Viewvc">**:Viewvc**</a> [檔案] -
  開啟指定檔案、目錄或目前緩衝內容(未指定)的 <a href="site:viewvc">ViewVc</a> 超連結.


網際網路搜尋指令集
------------------

- <a href="common/web.html#OpenUrl">**:OpenUrl**</a> [超連結] -
  以定義好的瀏覽器開啟超連結.
- <a href="common/web.html#Google">**:Google**</a> [關鍵字 ...] -
  在 Google 搜尋關鍵字.
- <a href="common/web.html#Clusty">**:Clusty**</a> [關鍵字 ...] -
  在 Clusty 搜尋關鍵字.
- <a href="common/web.html#Wikipedia">**:Wikipedia**</a> [關鍵字 ...] -
  在 wikipedia 搜尋關鍵字.
- <a href="common/web.html#Dictionary">**:Dictionary**</a> [word] -
  在 dictionary.reference.com 尋找字典內容.
- <a href="common/web.html#Thesaurus">**:Thesaurus**</a> [word] -
  在 thesaurus.reference.com 尋找內容.


其他指令集
--------------

- <a href="common/util.html#LocateFileEdit">**:LocateFileEdit**</a> [檔案] -
  尋找檔案並以 :edit 開啟.
- <a href="common/util.html#LocateFileSplit">**:LocateFileSplit**</a> [檔案] -
  尋找檔案並以 :split 開啟.
- <a href="common/util.html#LocateFileTab">**:LocateFileTab**</a> [檔案] -
  尋找檔案並以 :tabnew 開啟.
- <a href="common/util.html#Split">**:Split**</a>
  檔案 [檔案 ...] -
  類似 :split 指令, 但允許同時開啟多個檔案.
- <a href="common/util.html#SplitRelative">**:SplitRelative**</a>
  檔案 [檔案 ...] -
  類似 **:Split** 指令，但會個別將檔案分割至目前 buffer 內.
- <a href="common/util.html#Tabnew">**:Tabnew**</a>
  檔案 [檔案 ...] -
  類似 **:Split** 指令, 但會以 :tabnew 開啟個別的檔案.
- <a href="common/util.html#TabnewRelative">**:TabnewRelative**</a> -
  檔案 [檔案...] -
  類似 **:SplitRelative** 指令, 但會以 :tabnew 開啟個別的檔案.
- <a href="common/util.html#EditRelative">**:EditRelative**</a>
  檔案 -
  類似 :SplitRelative 指令, 但會以 edit 開啟檔案，並且一次只能開啟一個檔案.
- <a href="common/util.html#ReadRelative">**:ReadRelative**</a>
  檔案 -
  類似 **:SplitRelative** 指令, 但會以 :read 指令開啟, 並同一次只能開啟一個檔案.
- <a href="common/util.html#ArgsRelative">**:ArgsRelative**</a>
  file_pattern [ file_pattern ...] -
  類似 **:SplitRelative** 指令, 但會以 :args 指令執行動作.
- <a href="common/util.html#ArgAddRelative">**:ArgAddRelative**</a>
  file_pattern [ file_pattern ...] -
  類似 **:SplitRelative** 指令, 但會以 :argadd 指令執行動作.
- <a href="common/util.html#DiffLastSaved">**:DiffLastSaved**</a> -
  執行 diffsplit 比較目前修改的檔案內容與前一次存檔的內容.
- <a href="common/util.html#SwapWords">**:SwapWords**</a> -
  交換游標目前位置的字與下一個字。這個指令可以處理非英文單字間的交換，例如: commas, periods 等.
- <a href="common/util.html#Sign">**:Sign**</a> -
  切換新增或移除 vim 簽名在目前列.
- <a href="common/util.html#Signs">**:Signs**</a> -
  開啟一個新視窗, 包含所有在緩衝內容的簽名清單.
- <a href="common/util.html#SignClearUser">**:SignClearUser**</a> -
  移除所有經由 :Sign 指令寫入的 vim 簽名.
- <a href="common/util.html#SignClearAll">**:SignClearAll**</a> -
  移除所有 vim 簽名
- <a href="common/util.html#QuickFixClear">**:QuickFixClear**</a> -
  移除所有 QuickFix 視窗的連接點.
- <a href="common/util.html#LocationListClear">**:LocationListClear**</a> -
  移除所有在位置視窗的連接點.

.. vim:fileencoding=utf-8:encoding=utf-8
