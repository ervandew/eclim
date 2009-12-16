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

# Sed script which processes the file ~/.eclimrc, to grab the nailgun port
# number which the eclim client will use to connect to the eclimd server.
# Usage: sed -n -f eclim.sed ~/.eclimrc

# Note: using '[ ]' instead of '\s' since '\s' doesn't appear to work on osx
# (same with '\w').

# remove all leading or trailing spaces.
s/^[ ]*//g
s/[ ]*$//g

# delete blank and comment lines.
/^$/d
/^#/d

# delete any line not containing nailgun.server.*
# this stopped working at some point, maybe when i switched to arch?
#/^nailgun\.server\..*=.*/!d

# block to process portions spanning across lines.
# since the !d operation stopped working, instead only copy nailgun properties
# to the hold space (downside is that their values must be on the same line).
#H
/^\(-D\)*nailgun\.server\..*=/H
${
  g
  # remove line continuation chars.
  s/\\\n//g
  # convert properties to nailgun arguments.
  s/\(-D\)*nailgun\.server\.\([a-zA-Z]*\)[ ]*=[ ]*\([a-zA-Z0-9]*\)/ --nailgun-\2 \3/g
  # remove all new line characters
  s/\n/ /g
  p
}
