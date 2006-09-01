##
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
