<?php if (!defined('PmWiki')) exit();
/*  Copyright 2004 Patrick R. Michaud (pmichaud@pobox.com)
    This file is blocklist.php for PmWiki, you can redistribute it 
    and/or modify it under the terms of the GNU General Public License 
    as published by the Free Software Foundation; either version 2 of 
    the License, or (at your option) any later version.  
    
*/

/**
    This script adds blocklisting capabilities to PmWiki.  By default,
    it searches through Main.Blocklist (or whatever pages are specified
    by the $BlocklistPages variable) looking for strings of the form
    "block:something" to be excluded from any posting to the site.
    For example, if Main.Blocklist contains "block:spam.com", then
    any posting to "spam.com" will be disallowed.
    
    Crisses Modifications:
    Looks for patterns that begin with "block:" and
    match to the end of the line or the next "block:", which allows for phrases.  If you add
    block:no spam
    Then posts that contain the phrase "no spam" will be blocked.
    Works with punctuation, also.  Phrases are trimmed, so "block: no spam" works also.

    In addition, the script scans the Main.Blocklist page for 
    IP addresses of the form "a.b.c.d", "a.b.c.*", or "a.b.*.*" and blocks 
    any postings coming from a host matching the IP address or
    address range.

    To use the script, simply place it in the cookbook/ directory
    and add the following line to local/config.php:

        include_once('cookbook/blocklist.php');

    The script also sets the variables $Blocklisted and $WhyBlockedFmt
    so that the administrator can take actions other than simply blocking
    the post.  $Blocklisted counts the number of "offenses" in the attemped post.
    $WhyBlockedFmt is an array containing string messages for
    the reasons the post was blocked.  This makes for easy emails to an admin
    or data transfer to a security script.


USAGE -- this is how I tested it in config.php:
    
    
// ------- blocklist settings -------

// Only bother with this info if this is an edit action
if ($action=='edit') {

    // get the IP address of the poster
    $editip = $_SERVER["REMOTE_ADDR"];
    
    //Uncomment for poster to see a message on the edit page about why they have been blocked.
    # $EnableWhyBlocked = "1";

    // run the script
    include_once('cookbook/blocklist.php');

    // if the script found blocked content
    if ($Blocklisted) {
        
        // compose the contents portion of the email to be sent:

       	// give the page URL posted to
        $contents = "The poster was calling: $ScriptUrl/$pagename\n"; // page URL

        // give the IP address
        $contents .= "Blocked poster is at $editip.\n";
        
        // Let the admin know why the post is blocked
        $contents .= "$WhyBlockedFmt\n\n";
        
        // Give the original post contents, in case it's legit, has
        // additional content that ought to be blocked, or needs
        // to be reported to someone.
        $contents .= "The body of the attempted post was:\n\n" . @$_POST['text'];
        
        // If you want to have the end of the webserver logfile attached, use these lines
        // (If you don't have permissions for reading the mail log, comment out these lines and
        //	report website abuse to your system's administrator.)
        $weblog = '/var/log/httpd/access_log';
        $contents .= "\n\n------Access Log Tail------\n" . `tail -n 20 $weblog`;
        
        
        // compose the email with the following attributes:
        
        // comma-separated addresses to send the mail to:
        $emailto = "crisses"; 
        
        // the subject line to send it out with (includes a "score" for how bad it is):
        $subjectline = "Blocked $WikiTitle Post (Score $Blocklisted)";
        
        // email address the mail should be from:
        $mailfrom = "mantra@eclectictech.net";
            
        // This line sends the mail.  Do not alter (unless you know what you
        //  are doing).  Comment out if you do not want emails regarding blocked posts.
        mail("$emailto","$subjectline","$contents","$mailfrom");

    }
}

// ------- End blocklist settings -------


Other variables available:

$BlocklistPages is an array of the pages that are used for the blocklist.  Default = "Site.Blocklist", and if it exists it includes "Main.Blocklist".  User defining this overrides either file as default.

$BlocklistMessageFmt is the format for the "you've been blocked" statement on the edit screen.

$BlockIPMessageFmt is the message format preceeding the blocked IP address in mails and on the edit screen.

$BlockTextMessageFmt is the message format preceeding the blocked text content in mails and on the edit screen.

$EnableWhyBlocked is a switch for whether or not to display reasons a post is blocked to the user on the edit screen.  For security purposes, the default is off (0).

*/

//define the blocklist version number
define(BLOCKLIST_VERSION, '2.3.0');

$page_name = ResolvePageName($pagename);


// Set default for $BlocklistPages to Site/Blocklist &/OR Main/Blocklist.  
// If customized, remove values that don't exist.
if (!isset($BlocklistPages)) {
	if (PageExists('Site.Blocklist')) $BlocklistPages[] = 'Site.Blocklist';
	if (PageExists('Main.Blocklist')) $BlocklistPages[] = 'Main.Blocklist';
} else {
	foreach ($BlocklistPages as $key->$value) {
		if (! PageExists($value)) unset($BlocklistPages[$key]);
	}
}
//print_r($BlocklistPages);

//print_r($_POST['text']);

# This script only applies to editing (or the commentbox cookbook script), so 
# if we're not editing or commenting, just return.  Partly borrowed from cmsb-blocklist.php.
# Not sure why he added "diff" to the actions that this runs on.  
if (! ($action == 'edit' || $action == 'comment')
	|| (count($BlocklistPages) < 1)
	|| in_array ($page_name, $BlocklistPages) ) { return; }

//Now that we know we're supposed to be here, set remaining variables
SDV($BlocklistMessageFmt, "<h3 class='wikimessage'>$[This post has been blocked by the administrator]</h3>");
SDV($BlockIPMessageFmt, "Remote server matches blocked server IP: ");
SDV($BlockTextMessageFmt, "Content matches blocklist content: ");
SDV($EnableWhyBlocked, "0"); // default is to not let them know
SDV($EnableBlocklistRegex, "0"); // enables Perl-compatible regular expression search

// Set the block counter
$Blocklisted = 0;

// check each blocklist page
foreach((array)$BlocklistPages as $oneblockpage) {
	$pn = FmtPageName($oneblockpage,$pagename);
	$page = ReadPage($pn);
	
	// Prep for IP checking
	list($ipa,$ipb,$ipc,$ipd) = explode('.', $_SERVER['REMOTE_ADDR']);
	
	// Check ip address against page text
	if (preg_match("/(\\D$ipa\\.$ipb\\.(\\D|$ipc)\\.($ipd)?\\D)/", $page['text'], $matchedip)) {
		$Blocklisted++; // increment counter
		$WhyBlockedFmt .= $BlockIPMessageFmt . $matchedip[1] . "\n"; // add a reason why the page is blocked
	}
  
  // When not tallying reasons, only run through the terms if the IP is not blocked
  if (!(($EnableWhyBlocked == 0) && ($Blocklisted > 0))) {
		
		//catch and fix multiple block:'s on a single line and leading whitespace 
		//(allows legacy format)
		$blocktext = preg_replace('/\\s*block:/', "\nblock:", $page['text']);
		// Make an array of text that follows "block:" incl spaces & punct
		preg_match_all('/block:\s*(.+)/', $blocktext, $blockterms);
	
		//compares entries in the blocklist to the post content
		// Check posted page content against blocklist
		// Note:  matches only on first, not repeated, offenses-per-criteria
		// If score/reasons are not being tallied, break out.
		foreach ($blockterms[1] as $blockitem){
			if (stristr(@$_POST['text'], $blockitem)) {
				$Blocklisted++;
				if ($EnableWhyBlocked == 0) break;
				$WhyBlockedFmt .= $BlockTextMessageFmt . $blockitem . "\n";
			}
		}
	}

	
  // Check Regex only if necessary
  if (($EnableBlocklistRegex == 1) && !(($EnableWhyBlocked == 0) && ($Blocklisted > 0))) {
		
		//catch and fix multiple block:'s on a single line and leading whitespace 
		//(allows legacy format)
		$blocktext = preg_replace('/\\s*regex:/', "\nregex:", $blocktext);

		// Make an array of text that follows "regex:" incl spaces & punct
		preg_match_all('/regex:(.+)/', $blocktext, $regexterms);
	
		//compares entries in the regex list to the post content
		// Note:  matches only on first, not repeated, offenses-per-criteria
		// If score/reasons are not being tallied, break out.
		foreach ($regexterms[1] as $oneregex){
			if (preg_match($oneregex, @$_POST['text'], $regexmatch)) {
				$Blocklisted++;
				if ($EnableWhyBlocked == 0) break;
				$WhyBlockedFmt .= $BlockTextMessageFmt . $regexmatch[0] . "\n";
			}
		}
	}

} //foreach ((array)$BlocklistPages)

// if the posting is blocked, then disallow the post 
if ($Blocklisted) { 
	$EnablePost = 0; 
	unset($_POST['post']); 
	unset($_POST['postattr']);
	unset($_POST['postedit']);
	// let the poster know they've been blocked
	$MessagesFmt[] .= $BlocklistMessageFmt;
	// if required, provide reasons
	if ($EnableWhyBlocked == 1) $MessagesFmt[] = '<pre class="blocklistmessage">' . $WhyBlockedFmt . "</pre>";
}

?>
