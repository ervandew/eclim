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

# Sed script which processes the file ~/.eclimrc, to grab the nailgun port
# number which the eclim client will use to connect to the eclimd server.
# Usage: sed -n -f eclim.sed ~/.eclimrc

# delete any line not containing nailgun.server.*
/^\s*nailgun.server.\(.*\)=/!d

# remove all leading spaces.
s/^\s\+//g

# block to process portions spanning across lines.
H
${
  g
  # remove line continuation chars.
  s/\\\s*//g
  # remove all new line characters
  s/\n/ /g
  # convert properties to nailgun arguments.
  s/nailgun.server.\(\w*\)\s*=\s*\(\w*\)\s*/ --nailgun-\1 \2/g
  p
}
