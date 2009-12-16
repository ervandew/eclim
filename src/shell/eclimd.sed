# Copyright (C) 2005 - 2009  Eric Van Dewoestine
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

# Sed script which processes the file ~/.eclimrc, transforming it into a set of
# -D and other vm arguments to be passed to eclipse at startup.
# Usage: sed -n -f eclimd.sed ~/.eclimrc

# Note: using '[ ]' instead of '\s' since '\s' doesn't appear to work on osx
# (same with '\w').

# remove all leading or trailing spaces.
s/^[ ]*//g
s/[ ]*$//g

# delete blank and comment lines.
/^$/d
/^#/d

# block to process portions spanning across lines.
H
${
  g
  # remove line continuation chars.
  s/\\\n//g
  # remove all new line characters and add -D for each property
  s/\n[ ]*\([a-zA-Z]\)/ -D\1/g
  # remove any remaining new line characters
  s/\n/ /g
  p
}
