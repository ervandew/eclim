#!/bin/sh

# Copyright (c) 2005 - 2006
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FILES="
  ./after/plugin/eclim_taglist.vim
  ./autoload/eclim.vim
  ./autoload/eclim/ant/complete.vim
  ./autoload/eclim/ant/doc.vim
  ./autoload/eclim/ant/run.vim
  ./autoload/eclim/ant/util.vim
  ./autoload/eclim/ant/validate.vim
  ./autoload/eclim/common.vim
  ./autoload/eclim/eclipse.vim
  ./autoload/eclim/html/util.vim
  ./autoload/eclim/ivy.vim
  ./autoload/eclim/java/bean.vim
  ./autoload/eclim/java/complete.vim
  ./autoload/eclim/java/constructor.vim
  ./autoload/eclim/java/correct.vim
  ./autoload/eclim/java/delegate.vim
  ./autoload/eclim/java/doc.vim
  ./autoload/eclim/java/impl.vim
  ./autoload/eclim/java/import.vim
  ./autoload/eclim/java/junit.vim
  ./autoload/eclim/java/logging.vim
  ./autoload/eclim/java/regex.vim
  ./autoload/eclim/java/search.vim
  ./autoload/eclim/java/util.vim
  ./autoload/eclim/maven.vim
  ./autoload/eclim/project.vim
  ./autoload/eclim/signs.vim
  ./autoload/eclim/util.vim
  ./autoload/eclim/vim/doc.vim
  ./autoload/eclim/vim/find.vim
  ./autoload/eclim/vim/src.vim
  ./autoload/eclim/web.vim
  ./autoload/eclim/xml.vim
  ./autoload/eclim/xml/util.vim
  ./ftdetect/eclim.vim
  ./ftplugin/ant/eclim_complete.vim
  ./ftplugin/ant/eclim_doc.vim
  ./ftplugin/ant/eclim_validate.vim
  ./ftplugin/commonsvalidator/eclim.vim
  ./ftplugin/eclipse_classpath/eclim.vim
  ./ftplugin/forrestdocument/eclim.vim
  ./ftplugin/forreststatus/eclim.vim
  ./ftplugin/hibernate/eclim.vim
  ./ftplugin/ivy/eclim.vim
  ./ftplugin/java/eclim.vim
  ./ftplugin/java/eclim_bean.vim
  ./ftplugin/java/eclim_complete.vim
  ./ftplugin/java/eclim_constructor.vim
  ./ftplugin/java/eclim_correct.vim
  ./ftplugin/java/eclim_delegate.vim
  ./ftplugin/java/eclim_doc.vim
  ./ftplugin/java/eclim_impl.vim
  ./ftplugin/java/eclim_import.vim
  ./ftplugin/java/eclim_junit.vim
  ./ftplugin/java/eclim_logging.vim
  ./ftplugin/java/eclim_regex.vim
  ./ftplugin/java/eclim_search.vim
  ./ftplugin/junitresult/eclim.vim
  ./ftplugin/maven_project/eclim.vim
  ./ftplugin/spring/eclim.vim
  ./ftplugin/strutsconfig/eclim.vim
  ./ftplugin/vim/eclim_doc.vim
  ./ftplugin/vim/eclim_find.vim
  ./ftplugin/vim/eclim_src.vim
  ./ftplugin/webxml/eclim.vim
  ./ftplugin/xml/eclim.vim
  ./plugin/eclim.vim
  ./plugin/eclim_ant.vim
  ./plugin/eclim_archive.vim
  ./plugin/eclim_common.vim
  ./plugin/eclim_project.vim
  ./plugin/eclim_qf.vim
  ./plugin/eclim_web.vim
  ./syntax/commonsvalidator.vim
  ./syntax/eclipse_classpath.vim
  ./syntax/forrestdocument.vim
  ./syntax/forreststatus.vim
  ./syntax/hibernate.vim
  ./syntax/ivy.vim
  ./syntax/junitresult.vim
  ./syntax/maven_project.vim
  ./syntax/spring.vim
  ./syntax/strutsconfig.vim
  ./syntax/webxml.vim"

for i in $FILES; do
  if [ -f $i ]; then
    echo "Removing file: $i"
    rm $i
  fi
done

echo "Done."
