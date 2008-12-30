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

- :ref:`:PingEclim` -
  連接 eclimd 伺服器.
- :ref:`:ShutdownEclim` -
  關閉 eclimd 伺服器.
- :ref:`:EclimSettings` -
  瀏覽/編輯全區域設定選項.


Project 專案指令集
------------------

- :ref:`:ProjectCreate`
  <資料夾> [-p <專案名稱>] -n <nature> ... [-d <依存的專案> ...] -
  建立新專案.
- :ref:`:ProjectList` - 顯示目前專案清單.
- :ref:`:ProjectSettings` [<專案名稱>] -
  瀏覽/編輯專案設定選項.
- :ref:`:ProjectDelete` <專案名稱> - 刪除指定的專案.
- :ref:`:ProjectRefresh` [<專案名稱> <專案名稱> ...] -
  更新列表中或所有的專案，這將會更新設定值至實際磁碟檔案中.
- :ref:`:ProjectRefreshAll` -
  同 :ProjectRefreshAll 指令，但更新所有的專案.
- :ref:`:ProjectOpen` <專案名稱> - 開啟舊專案.
- :ref:`:ProjectClose` <專案名稱> - 關閉專案.
- :ref:`:ProjectCD` -
  改變全區域的工作目錄至目前檔案所在的專案目錄(即執行 :cd).
- :ref:`:ProjectLCD` -
  改變目前工作目錄至目前檔案所在的專案目錄(即執行 :lcd).
- :ref:`:ProjectTree`
  [<專案名稱> <專案名稱> ...] - 針對一個或多個專案開啟可導覽的樹狀結構表.
- :ref:`:ProjectsTree` -
  對於所有的專案開啟一份可導覽的樹狀結構表.
- :ref:`:ProjectGrep`
  /<pattern>/ file_pattern [file_pattern ...] -
  在專案根目錄使用 :vim[grep] 指令功能.
- :ref:`:ProjectGrepAdd`
  /<pattern>/ file_pattern [file_pattern ...] -
  在專案根目錄使用 :vimgrepa[dd] 指令功能.
- :ref:`:ProjectLGrep`
  /<pattern>/ file_pattern [file_pattern ...] -
  在專案根目錄使用 :lv[imgrep] 指令功能.
- :ref:`:ProjectLGrepAdd`
  /<pattern>/ file_pattern [file_pattern ...] -
  在專案根目錄使用 :lvimgrepa[dd] 指令功能.


Eclipse .classpath 維護指令集
-----------------------------

- :ref:`:NewSrcEntry` <目錄> [<目錄> ...] - 新增一個原始碼目前節點.
- :ref:`:NewProjectEntry` <專案名稱> [<專案名稱> ...] - 新增一個專案節點.
- :ref:`:NewJarEntry` <檔案> [<檔案> ...] - 新增一個 .jar 檔案節點.
- :ref:`:NewVarEntry` <參數/檔案> [<參數/檔案> ...] - 新增一個參數節點.
- :ref:`:VariableList` 列出可使用的 classpath 參數及相對應的值.
- :ref:`:VariableCreate` <名稱> <路徑> - 建立或修改一個名稱的變數.
- :ref:`:VariableDelete` <名稱> - 刪除指定名稱的變數.


Ant 指令集
--------------

- :ref:`:Ant` [<目標> ...] - 在目前專案設定下，執行 ant.
- :ref:`:AntDoc` [<元素>] -
  以目前游標位置的元素或指定元素尋找並開啟文件檔案.
- :ref:`:Validate <:Validate_ant>` - 驗證目前的 ant 檔案.


DTD 指令集
--------------

- :ref:`:Validate <:Validate_dtd>` - 驗證目前的 DTD 檔案.


HTML 指令集
--------------

- :ref:`:Validate <:Validate_html>` - 驗證目前的 HTML 檔案.


Ivy Commands
--------------

- :ref:`:IvyRepo` <路徑> -
  設定必須的 IVY_REPO classpath 參數予會自動更新 .classpath 檔案的 ``ivy.xml`` 設定檔.
- :ref:`:IvyDependencySearch` <artifact> -
  尋找線上的資料庫，並將結果列示在一個視窗內，使用者可經由按下<Enter>鍵將結果加入目前專案內。在編輯 ``ivy.xml`` 檔案時可以使用本指令.


Java 指令集
--------------

- :ref:`:JavaGet` - 建立 java bean getter 方法.
- :ref:`:JavaSet` - 建立 java bean setter 方法.
- :ref:`:JavaGetSet` - 建立 java bean getter 及 setter 方法.
- :ref:`:JavaConstructor` -
  建立類別的建構子，內容為空或以選擇的欄位建立預設值.
- :ref:`:JavaImpl` -
  自 super class 及實作的 interface 列示可實作/可重載的方法.
- :ref:`:JavaDelegate` - 列示操作目前游標欄位的方法.
- :ref:`:JUnitImpl` -
  類似 **:JavaImpl** 的動作, 但建立的方法為測試用途.
- :ref:`:JUnitExecute` - [測試例子]
  以常用的建置工具執行測試例子.
- :ref:`:JUnitResult` - [測試例子]
  檢視測試例子的執行結果.
- :ref:`:JavaImport` - Import 目前游標位置的 class.
- :ref:`:JavaSearch`
  [-p <pattern>] [-t <type>] [-x <內文內容>] -
  尋找類別, 方法, 欄位等(使用 pattern 支持，尋找目前游標位置的元素).
- :ref:`:JavaSearchContext` -
  執行目前游標位置元素的內文搜尋.
- :ref:`:JavaCorrect` - 建議應修正的程式碼.
- :ref:`:JavaRegex` - 開啟一個視窗，進行 regular ecpression 測試.
- :ref:`:JavaDocSearch` -
  尋找 javadoc，如同 **:JavaSearch** 指令.
- :ref:`:JavaDocComment` - 新增或修改目前游標位置元素的註解.
- :ref:`:Jps` - 開啟目前 java 運作資訊的視窗.


Log4j 指令集
--------------

- :ref:`:Validate <:Validate_log4j>` - 驗證目前的 log4j 組態設定檔案.


Maven 指令集
--------------

- :ref:`:Maven` [<目標> ...] - 在目前的專案設定下執行 maven 1.x.
- :ref:`:Mvn` [<目標> ...] - 在目前的專案設定下執行 maven 2.x.
- :ref:`:MavenRepo` -
  設定必須的 MAVEN_REPO classpath 參數予帶有 eclipse 支持的 maven's (1.x).
- :ref:`:MvnRepo` -
  設定必須的 M2_REPO classpath 參數予帶有 eclipse 支持的 maven's (2.x).
- :ref:`:MavenDependencySearch` <artifact> -
  尋找線上的資料庫，並將結果列示在一個視窗內，使用者可經由按下<Enter>鍵將結果加入目前專案內。在編輯 maven 1.x ``project.xml`` 檔案時可以使用本指令.
- :ref:`:MvnDependencySearch` <artifact> -
  尋找線上的資料庫，並將結果列示在一個視窗內，使用者可經由按下<Enter>鍵將結果加入目前專案內。在編輯 maven 2.x ``pom.xml`` 檔案時可以使用本指令.


Python 指令集
--------------

- :ref:`:PythonRegex` -
  開啟一個視窗來測試 python regular expressions.
- :ref:`:DjangoManage` -
  存在 manage.py 的同一個目錄下或子目錄下，自任何檔案中呼叫 django's ``manage.py``.


Vim 指令集
--------------

- :ref:`:FindCommandDef` [<command>] -
  尋找 command 的定義.
- :ref:`:FindCommandRef` [<command>] -
  尋找 command 的參考.
- :ref:`:FindFunctionDef` [<function>] -
  尋找函數的定義.
- :ref:`:FindFunctionRef` [<function>] -
  尋找函數的參考.
- :ref:`:FindVariableDef` [<變數名稱>] -
  尋找全區域變數的定義
- :ref:`:FindVariableRef` [<變數名稱>] -
  尋找全區域變數的參考.
- :ref:`:FindByContext` -
  以游標位置元素在內文中尋找 command, 函數或變數.
- :ref:`:VimDoc` [<關鍵字>] -
  以關鍵字開啟相關的 vim 說明文件.


WebXml 指令集
--------------

- :ref:`:Validate <:Validate_webxml>` - 驗證目前的 ``web.xml`` 檔案.


Wsdl 指令集
--------------

- :ref:`:Validate <:Validate_wsdl>` - 驗證目前的檔案.


Xml 指令集
--------------

- :ref:`:DtdDefinition` [<元素>] -
  開啟目前 XML 檔案的 dtd 檔案，並移置指定元素的定義.
- :ref:`:XsdDefinition` [<元素>] -
  開啟目前 XML 檔案的 xsd 檔案，並移置指定元素的定義.
- :ref:`:Validate <:Validate_xml>` [<檔案>] -
  驗證指定的 XML 檔案或目前的 XML 檔案.
- :ref:`:XmlFormat` 重新編排目前的 XML 檔案.


Xsd 指令集
--------------

- :ref:`:Validate <:Validate_xsd>` - 驗證目前的檔案.


版本控制指令集
--------------

.. note::

  目前這項指令集只支援 CVS 及 subversion 兩種系統.

- :ref:`:VcsAnnotate` - 使用 vim 簽名對目前版本檔案作注釋.


網際網路搜尋指令集
------------------

- :ref:`:OpenUrl` [超連結] - 以定義好的瀏覽器開啟超連結.
- :ref:`:Google` [關鍵字 ...] - 在 Google 搜尋關鍵字.
- :ref:`:Clusty` [關鍵字 ...] - 在 Clusty 搜尋關鍵字.
- :ref:`:Wikipedia` [關鍵字 ...] - 在 wikipedia 搜尋關鍵字.
- :ref:`:Dictionary` [word] -
  在 dictionary.reference.com 尋找字典內容.
- :ref:`:Thesaurus` [word] - 在 thesaurus.reference.com 尋找內容.


其他指令集
--------------

- :ref:`:Split` 檔案 [檔案 ...] -
  類似 :split 指令, 但允許同時開啟多個檔案.
- :ref:`:SplitRelative` 檔案 [檔案 ...] -
  類似 **:Split** 指令，但會個別將檔案分割至目前 buffer 內.
- :ref:`:Tabnew` 檔案 [檔案 ...] -
  類似 **:Split** 指令, 但會以 :tabnew 開啟個別的檔案.
- :ref:`:TabnewRelative` - 檔案 [檔案...] -
  類似 **:SplitRelative** 指令, 但會以 :tabnew 開啟個別的檔案.
- :ref:`:EditRelative` 檔案 -
  類似 :SplitRelative 指令, 但會以 edit 開啟檔案，並且一次只能開啟一個檔案.
- :ref:`:ReadRelative` 檔案 -
  類似 **:SplitRelative** 指令, 但會以 :read 指令開啟, 並同一次只能開啟一個檔案.
- :ref:`:ArgsRelative` file_pattern [ file_pattern ...] -
  類似 **:SplitRelative** 指令, 但會以 :args 指令執行動作.
- :ref:`:ArgAddRelative` file_pattern [ file_pattern ...] -
  類似 **:SplitRelative** 指令, 但會以 :argadd 指令執行動作.
- :ref:`:DiffLastSaved` -
  執行 diffsplit 比較目前修改的檔案內容與前一次存檔的內容.
- :ref:`:SwapWords` -
  交換游標目前位置的字與下一個字。這個指令可以處理非英文單字間的交換，例如: commas, periods 等.
- :ref:`:Sign` - 切換新增或移除 vim 簽名在目前列.
- :ref:`:Signs` - 開啟一個新視窗, 包含所有在緩衝內容的簽名清單.
- :ref:`:SignClearUser` -
  移除所有經由 :Sign 指令寫入的 vim 簽名.
- :ref:`:SignClearAll` - 移除所有 vim 簽名
- :ref:`:QuickFixClear` - 移除所有 QuickFix 視窗的連接點.
- :ref:`:LocationListClear` - 移除所有在位置視窗的連接點.

.. vim:fileencoding=utf-8:encoding=utf-8
