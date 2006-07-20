<?php if (!defined('PmWiki')) exit();

##  $WikiTitle is the name that appears in the browser's title bar.
$WikiTitle = 'Eclim Wiki';

## Writable place for session data
session_save_path('/tmp/persistent/eclim/pmwiki.d/sessions');

## Store wiki pages outside the document tree.
$WorkDir = '/tmp/persistent/eclim/pmwiki.d/wiki.d';
$WikiDir = new PageStore('/tmp/persistent/eclim/pmwiki.d/wiki.d/$FullName');

$DefaultPasswords['admin']='$1$zlC1Ya1G$5paaylTMDCveyhwfQQuWc1';

## Config to help facilitate backups.
## Add a custom page storage location for wiki pages.
$PageStorePath = "wikilib2.d/\$FullName";
$where = count($WikiLibDirs);
if ($where>1) $where--;
array_splice($WikiLibDirs, $where, 0, array(new PageStore($PageStorePath)));
