<?php if (!defined('PmWiki')) exit();

######### Main Config ##########
#
# The name that appears in the browser's title bar.
$WikiTitle = 'Eclim Wiki';
$PageLogoUrl = '../images/project.png';
$Skin = 'forrest_pelt';

# Writable place for session data
session_save_path('/tmp/persistent/eclim/pmwiki.d/sessions');

# Store wiki pages outside the document tree.
$WorkDir = '/tmp/persistent/eclim/pmwiki.d/wiki.d';
$WikiDir = new PageStore('/tmp/persistent/eclim/pmwiki.d/wiki.d/$FullName');

# Admin password.
$DefaultPasswords['admin'] = '$1$ck6xfmeT$O4PVGZHQHkBRu2dSlqytU0';

# Require that the user supply an author when editing pages.
$EnablePostAuthorRequired = 1;

# Config to help facilitate backups.
# Add a custom page storage location for wiki pages.
$where = count($WikiLibDirs);
if ($where>1) $where--;
array_splice($WikiLibDirs, $where, 0, array(new PageStore($PageStorePath)));

# Require admin password to edit RecentChanges (etc.) pages.
if ($action=='edit' && preg_match('/\\.(All)?RecentChanges$/', $pagename))
  { $DefaultPasswords['edit'] = '*'; }

######### Cookbook Aditions ##########

# Black List support
# Not working.
#if ($action == 'edit') include_once('cookbook/mt-blacklist.php');

# Block List support
if ($action == 'edit') include_once('cookbook/blocklist2.php');
