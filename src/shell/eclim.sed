##
# Sed script which processes the file ~/.eclimrc, to grab the nailgun port
# number which the eclim client will use to connect to the eclimd server.
# Usage: sed -f eclim.sed ~/.eclimrc

# delete any line not containing nailgun.server.port
/^\s*nailgun.server.port=/!d

# grab the port number
s/^\s*nailgun.server.port=\s*\(.*\)\s*/\1/
