# Copyright (c) 2005 - 2008
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Author: Eric Van Dewoestine

# Sed script which processes the file ~/.eclimrc, transforming it into a set of
# -D arguments to be passed to eclipse at startup.
# Usage: sed -n -f eclimd.sed ~/.eclimrc

# delete blank and comment lines.
/^\(\s*#\|$\)/d

# remove all leading spaces.
s/^\s\+//g

# block to process portions spanning across lines.
H
${
  g
  # remove line continuation chars.
  s/\\\s*//g
  # remove all new line characters and add -D for each property
  s/\n/ -D/g
  p
}
