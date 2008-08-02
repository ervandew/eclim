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

.. _vim/java/regex:

Regex Testing
=============

.. _\:JavaRegex:

Eclim provides a means to test java regular expressions.  Simply execute
**:JavaRegex** and a Vim window will open allowing you to test your regular
expression.

By default the content of the regex window will be\:

::

  te(st)
  Some test content used to test
  language specific regex against.

The first line of this window is the regular expression to test, and the
remaining lines consist of the content to test against.  When you write the file
(:w), eclim will evaluate the regular expression and highlight all matched
occurrences as well as any groups found within those occurrences.

When applying the regex against the text, by default eclim will run the regex
against the whole text at once, where '^' and '$' would match the start and end
of the whole sample text, not each individual line.  If you would like to
instead have the regex matched against each line individually, then you can set
the buffer local variable **b:eclim_regex_type** to 'line'.  To switch back to
the default mode simply set the same variable to 'file' instead.

By default eclim uses four different highlighting groups, two of which are
alternated between for the full matches, and two that are alternated between for
the group matches.  Eclim alternates the highlighting so that you can
distinguish one match from the next even if they occur one right after the
other.  Before utilizing the regex testing, you should first modify the four
variables defined below which define what highlighting groups will be used.
Since color schemes can vary greatly, eclim defaults to using some common groups
already defined by Vim.

The regex window also provides two commands to jump to the next or previous
match much like 'n' or 'N' for Vim searching\:

.. _\:NextMatch:

- **:NextMatch** - Jump to the next match.

.. _\:PreMatch:

- **:PrevMatch** - Jump to the previous match.


Configuration
-------------

Vim Variables

.. _g\:EclimRegexHi_0:

- **g:EclimRegexHi{0}** (Default: 'Constant') -
  The first highlighting group used for full regex matches.

.. _g\:EclimRegexHi_1:

- **g:EclimRegexHi{1}** (Default: 'MoreMsg') -
  The second highlighting group used for full regex matches.

.. _g\:EclimRegexGroupHi_0:

- **g:EclimRegexGroupHi{0}** (Default: 'Statement') -
  The first highlighting group used for group matches.

.. _g\:EclimRegexGroupHi_1:

- **g:EclimRegexGroupHi{1}** (Default: 'Todo') -
  The second highlighting group used for group matches.

.. _b\:eclim_regex_type:

- **b:eclim_regex_type** (Default: 'file') -
  Possible values: 'file' or 'line'

  Determines if the supplied regex should be applied against the sample
  text all at once ('file') or on each line individually ('line').
