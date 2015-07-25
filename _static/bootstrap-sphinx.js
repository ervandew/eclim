var sphinx = window.sphinx || {};
sphinx['init'] = function($win, $nav, $subnav){
  /**
   * Get the absolute position of the supplied element within the document,
   * regardless of scroll offsets.
   */
  sphinx['absolutePosition'] = function (el) {
    var curleft = 0, curtop = 0;
    if (el.offsetParent) {
      do {
        curleft += el.offsetLeft;
        curtop += el.offsetTop;
      } while (el = el.offsetParent);
      return {left: curleft, top: curtop};
    }
    return {left: 0, top: 0};
  };
  /**
   * Initialize the subnav functionality, handling fixing it to the top when
   * scrolling, dealing with overflow, etc.
   */
  sphinx['initSubNav'] = function(){
    if (!$subnav.length){
      return;
    }

    // disable until it can be fixed to work w/ hoverscroll
    /*$subnav.find('.dropdown-toggle').show().click(function(e){
      e.stopPropagation();
      $(this).closest('li').toggleClass('open');
    });*/

    function afix(){
      $subnav.addClass('subnav-fixed');
      if ($subnav.css('position') == 'fixed'){
        if (!$subnav.siblings('.subnav-placeholder').length){
          var $placeholder = $('<div class="subnav-placeholder navbar"></div>');
          $placeholder.height($subnav.outerHeight());
          $subnav.before($placeholder);
        }
      }
    }

    function unafix(){
      $subnav.siblings('.subnav-placeholder').remove();
      $subnav.removeClass('subnav-fixed');
    }

    function hoverscroll(){
      var $subnav_inner = $subnav.find('.navbar-inner');
      var $list = $subnav_inner.find('ul').first();
      var width = ($subnav.css('position') == 'fixed') ?
        $('.body').width() : $subnav_inner.width();
      $list.hoverscroll({
        arrowOpacity: 0.3,
        width: width,
        height: $subnav_inner.height()
      });
    }

    var navTop = 0;
    $win.on('load resize', function(e){
      var offset = sphinx.absolutePosition($('.subnav-placeholder').get(0) || $subnav.get(0));
      navTop = offset.top - $nav.outerHeight();
      $win.scroll();
      if (e.type == 'load'){
        $win.hashchange();
      }
    });
    $win.on('hoverscroll scroll', function(){
      var scrollTop = $win.scrollTop();
      if (scrollTop > navTop) {
        if (!$subnav.hasClass('subnav-fixed')){
          // account for the page not being long enough to make the subnav fixed
          // without it immediately scrolling back and forcing back to static
          // positioning.
          var height = $(document).height() - navTop - $subnav.outerHeight(true);
          if (height > $win.height()){
            afix();
          }
        }
      } else if ($subnav.hasClass('subnav-fixed')){
        unafix();
      }
      hoverscroll();
    }).scroll();
  };

  /**
   * Initialize top nav flyout menus to position the submenus more
   * intelligently to reduce the chance that they overflow off screen.
   */
  sphinx['initTopNav'] = function(){
    $('#navbar .nav li.dropdown-submenu').hover(
      function(){
        function menuTop($el){
          if ($el.is('.dropdown-menu')){
            var top = $el.offset().top;
            var bot = top + $el.height();
            if (bot > $win.height()){
              var $parent = $el.parent();
              var parenttop = menuTop($parent.parent());
              var botalign = -($el.height() - $parent.height());
              if ((top + botalign + parenttop) > 0){
                return botalign;
              }
              // the true top offset of the parent menu accounting for negative
              // css offset
              var menutop = $parent.closest('.dropdown-menu').offset().top + parenttop;
              return menutop - top - parenttop;
            }
          }
          return 0;
        }

        var $submenu = $(this).find('> ul');
        var subtop = menuTop($submenu);
        $submenu.css({
          position: 'absolute',
          top: subtop + 'px'
        });
      },
      function(){
        $(this).find('> ul').css('top', 0);
      }
    );
  };

  /**
   * Initialize handling of hash links, and loading of a page linking to a
   * hash, to account for fixed position header elements.
   */
  sphinx['initHashLinks'] = function(){
    $win.hashchange(function(){
      if (location.hash){
        var $target = $(location.hash.replace(/^#/, '#_'));
        if (/^_id\d+$/.test($target.attr('id')) && !$target.is('.section')){
          $target = $target.parent('[id]');
        }
        var padding = parseInt($target.css('padding-top').replace('px', ''), 10)
        var spacing = isNaN(padding) ? 0 : padding;
        var navOffset = spacing > 10 ? -(spacing - 5) : 5;
        var firstSection = '.content > .section';
        if ($target.is(firstSection) ||
           ($target.is('span') && $target.parent().is(firstSection)))
        {
          $win.scrollTop(0);
          // for android browsers
          $win.load(function(){$win.scrollTop(0);});
          return;
        }

        if ($nav.css('position') == 'fixed'){
          navOffset += $nav.outerHeight();
        }
        var $placeholder = $subnav.siblings('.subnav-placeholder');
        if ($placeholder.length){
          navOffset += $placeholder.outerHeight();
        }else{
          $subnav.addClass('subnav-fixed');
          if ($subnav.css('position') == 'fixed'){
            navOffset += $subnav.outerHeight();
          }
          $subnav.removeClass('subnav-fixed');
        }

        $win.scrollTop(sphinx.absolutePosition($target.get(0)).top - navOffset);
        var $animate = $target;
        if ($animate.is('.section')){
          $animate = $animate.find('> h2');
        } else if ($animate.is('ul')){
          $animate = $animate.find('li:first-child :first-child');
        }
        $animate.animate({opacity: 0.4}, 1000, function(){
          $animate.animate({opacity: 1}, 1000);
        });
        if ($subnav.css('position') == 'fixed'){
          $win.scroll();
        }
      }
    }).hashchange();
  };

  sphinx['initScrollspy'] = function(){
    var navOffset = 10;
    navOffset += $nav.outerHeight();
    navOffset += $subnav.outerHeight();

    // wait on all other events
    setTimeout(function(){
      $('body').scrollspy({offset: navOffset, target: '.page-toc'});
      $win.on('load resize', function(){
        $('body').scrollspy('refresh');
      });
      $('body').bind('activate', function(e){
        var $target = $(e.target);
        var $list = $subnav.find('.listcontainer');
        var left = $target.position().left;
        var right = left + $target.width();
        var scroll = $list.scrollLeft();

        if (right > ($list.width() - scroll)){
          $list.scrollLeft(right);
          $win.trigger('hoverscroll');
        }else if (left < scroll){
          $list.scrollLeft(left);
          $win.trigger('hoverscroll');
        }
      });
      $win.on('scroll', function(){
        if($win.scrollTop() == 0){
          $('body').data('scrollspy').activeTarget = null;
          $subnav.find('li.active').removeClass('active');
          $subnav.find('.listcontainer').scrollLeft(0);
          $win.trigger('hoverscroll');
        }
      });
    }, 0);
  };

  // Enable dropdown.
  $('.dropdown-toggle').dropdown();

  // fix the navbar to the top w/ js so that if the js doesn't load, hash links
  // aren't screwed (must be before initHashLinks).
  $nav.addClass('navbar-fixed-top').removeClass('no-js');
  $('body').addClass('with-fixed-nav');

  // alter element ids so we can handle hash links ourselves accounting for
  // fixed positioned nav elements.
  $('.content *[id]').each(function(index, el){
    var $el = $(el);
    $el.attr('id', '_' + $el.attr('id'));
  });
  // update nav links accordingly for scrollspy
  $('.nav *[href^=#]').each(function(index, el){
    var $el = $(el);
    $el.attr('data-target', '#_' + $el.attr('href').replace('#', ''));
    $el.click(function(){
      // force a hashchange if the hash is already set to the href
      if (location.hash == $(this).attr('href')){
        $win.hashchange();
      }
    });
  });

  sphinx.initTopNav();
  sphinx.initSubNav();

  sphinx.initHashLinks();
  sphinx.initScrollspy();
};

$(document).ready(function() {
  sphinx.init($(window), $('#navbar'), $('.subnav'));
});