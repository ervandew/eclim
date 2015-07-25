/**
 * HoverScroll jQuery Plugin
 *
 * Make an unordered list scrollable by hovering the mouse over it
 *
 * @author RasCarlito <carl.ogren@gmail.com>
 * @version 0.2.4
 * @revision 21
 *
 * FREE BEER LICENSE VERSION 1.02
 *
 * The free beer license is a license to give free software to you and free
 * beer (in)to the author(s).
 * 
 *
 * Released: 09-12-2010 11:31pm
 */
 
(function($) {

/**
 * @method hoverscroll
 * @param params {Object}  Parameter list
 *  params = {
 *    width {Integer},  // Width of list container
 *    height {Integer}, // Height of list container
 *    arrows {Boolean}, // Show direction indicators or not
 *    arrowsOpacity {Float},  // Arrows maximum opacity
 *    fixedArrows {Boolean},  // Display arrows permenantly, this overrides arrowsOpacity option
 *    debug {Boolean}   // Debug output in firebug console
 *  };
 */
$.fn.hoverscroll = function(params) {
  if (!params) { params = {}; }

  // Extend default parameters
  // note: empty object to prevent params object from overriding default params object
  params = $.extend({}, $.fn.hoverscroll.params, params);

  // Loop through all the elements
  this.each(function() {
    var $this = $(this);

    if (params.debug) {
      $.log('[HoverScroll] Trying to create hoverscroll on element ' + this.tagName + '#' + this.id);
    }

    // wrap ul list with a div.listcontainer
    if (params.fixedArrows) {
      if (!$this.parent().is('.fixed-listcontainer')){
        $this.wrap('<div class="fixed-listcontainer"></div>');
      }
    } else {
      if (!$this.parent().is('.listcontainer')){
        $this.wrap('<div class="listcontainer"></div>');
      }
    }

    $this.addClass('list');
    //.addClass('ui-helper-clearfix');

    // store handle to listcontainer
    var listctnr = $this.parent();

    // wrap listcontainer with a div.hoverscroll
    if (!listctnr.parent().is('.hoverscroll')){
      listctnr.wrap('<div class="ui-widget-content hoverscroll' +
        (params.rtl ? " rtl" : "") + '"></div>');
      //listctnr.wrap('<div class="hoverscroll"></div>');
    }

    // store hoverscroll container
    var ctnr = listctnr.parent();

        var leftArrow, rightArrow, topArrow, bottomArrow;

    // Add arrow containers
    if (params.arrows) {
      if (params.fixedArrows) {
        if (!listctnr.prevous().is('.left')){
          leftArrow = '<div class="fixed-arrow left"></div>';
          rightArrow = '<div class="fixed-arrow right"></div>';
          listctnr.before(leftArrow).after(rightArrow);
        }
      }
      else {
        if (!listctnr.find('> .left').length){
          leftArrow = '<div class="arrow left"></div>';
          rightArrow = '<div class="arrow right"></div>';
          listctnr.append(leftArrow).append(rightArrow);
        }
      }
    }

    // Apply parameters width and height
    ctnr.width(params.width).height(params.height);

    if (params.arrows && params.fixedArrows) {
      leftArrow = listctnr.prev();
      rightArrow = listctnr.next();

      listctnr.height(params.height)
        .width(params.width - (leftArrow.width() + rightArrow.width()));
    } else {
      listctnr.width(params.width).height(params.height);
    }

    var size = 0;

    ctnr.addClass('horizontal');

    // Determine content width
    $this.children().each(function() {
      $(this).addClass('item');

      if ($(this).outerWidth) {
        size += $(this).outerWidth(true);
      } else {
        // jQuery < 1.2.x backward compatibility patch
        size += $(this).width() + parseInt($(this).css('padding-left')) + parseInt($(this).css('padding-right'))
          + parseInt($(this).css('margin-left')) + parseInt($(this).css('margin-right'));
      }
    });
    // Apply computed width to listcontainer
    $this.width(size);

    if (params.debug) {
      $.log('[HoverScroll] Computed content width : ' + size + 'px');
    }

    // Retrieve container width instead of using the given params.width to include padding
    if (ctnr.outerWidth) {
      size = ctnr.outerWidth();
    } else {
      // jQuery < 1.2.x backward compatibility patch
      size = ctnr.width() + parseInt(ctnr.css('padding-left')) + parseInt(ctnr.css('padding-right'))
        + parseInt(ctnr.css('margin-left')) + parseInt(ctnr.css('margin-right'));
    }

    if (params.debug) {
      $.log('[HoverScroll] Computed container width : ' + size + 'px');
    }

    // Define hover zones on container
    var zoneLeft = ctnr.find('.left').outerWidth();
    var zoneRight = ctnr.find('.right').outerWidth();
    var zone = {
      1: {action: 'move', from: 0, to: zoneLeft, direction: -1 , speed: 16},
      2: {action: 'move', from: size - zoneRight, to: size, direction: 1 , speed: 16}
    }

    // Store default state values in container
    ctnr[0].isChanging = false;
    ctnr[0].direction  = 0;
    ctnr[0].speed      = 1;

    /**
     * Check mouse position relative to hoverscroll container
     * and trigger actions according to the zone table
     *
     * @param x {Integer} Mouse X event position
     * @param y {Integer} Mouse Y event position
     */
    function checkMouse(x, y) {
      var pos = x - ctnr.offset().left;

      for (i in zone) {
        if (pos >= zone[i].from && pos < zone[i].to) {
          if (zone[i].action == 'move') {startMoving(zone[i].direction, zone[i].speed);}
          else {stopMoving();}
        }
      }
    }

    /**
     * Sets the opacity of the left|top and right|bottom
     * arrows according to the scroll position.
     */
    function setArrowOpacity() {
      if (!params.arrows || params.fixedArrows) {return;}

      var maxScroll = listctnr[0].scrollWidth - listctnr.width();
      var scroll = listctnr[0].scrollLeft;
      var limit = params.arrowsOpacity;

            // Optimization of opacity control by Josef Körner
            // Initialize opacity; keep it between its extremas (0 and limit) we don't need to check limits after init
      var opacity = (scroll / maxScroll) * limit;

          if (opacity > limit) { opacity = limit; }
      if (isNaN(opacity)) { opacity = 0; }

      // Check if the arrows are needed
      // Thanks to <admin at unix dot am> for fixing the bug that displayed the right arrow when it was not needed
      var done = false;
      if (opacity <= 0) {
                $('div.arrow.left, div.arrow.top', ctnr).hide();
                if(maxScroll > 0) {
                    $('div.arrow.right, div.arrow.bottom', ctnr).show().css('opacity', limit);
                }
                done = true;
            }
      if (opacity >= limit || maxScroll <= 0) {
                $('div.arrow.right, div.arrow.bottom', ctnr).hide();
                done = true;
            }

      if (!done) {
        $('div.arrow.left, div.arrow.top', ctnr).show().css('opacity', opacity);
        $('div.arrow.right, div.arrow.bottom', ctnr).show().css('opacity', (limit - opacity));
      }
            // End of optimization
    }

    /**
     * Start scrolling the list with a given speed and direction
     *
     * @param direction {Integer} Direction of the displacement, either -1|1
     * @param speed {Integer}   Speed of the displacement (20 being very fast)
     */
    function startMoving(direction, speed) {
      if (ctnr[0].direction != direction) {
        if (params.debug) {
          $.log('[HoverScroll] Starting to move. direction: ' + direction + ', speed: ' + speed);
        }

        stopMoving();
        ctnr[0].direction  = direction;
        ctnr[0].isChanging = true;
        move();
      }
      if (ctnr[0].speed != speed) {
        if (params.debug) {
          $.log('[HoverScroll] Changed speed: ' + speed);
        }

        ctnr[0].speed = speed;
      }
    }

    /**
     * Stop scrolling the list
     */
    function stopMoving() {
      if (ctnr[0].isChanging) {
        if (params.debug) {
          $.log('[HoverScroll] Stoped moving');
        }

        ctnr[0].isChanging = false;
        ctnr[0].direction  = 0;
        ctnr[0].speed      = 1;
        clearTimeout(ctnr[0].timer);
      }
    }

    /**
     * Move the list one step in the given direction and speed
     */
    function move() {
      if (ctnr[0].isChanging == false) {return;}

      setArrowOpacity();

      var scrollSide = 'scrollLeft';

      listctnr[0][scrollSide] += ctnr[0].direction * ctnr[0].speed;
      ctnr[0].timer = setTimeout(function() {move();}, 50);
    }

    // Initialize "right to left" option if specified
    if (params.rtl) {
      listctnr[0].scrollLeft = listctnr[0].scrollWidth - listctnr.width();
    }

    // Bind actions to the hoverscroll container
    ctnr.unbind('mousemove').unbind('mouseleave');
    ctnr
    // Bind checkMouse to the mousemove
    .mousemove(function(e) {checkMouse(e.pageX, e.pageY);})
    // Bind stopMoving to the mouseleave
    // jQuery 1.2.x backward compatibility, thanks to Andy Mull!
    // replaced .mouseleave(...) with .bind('mouseleave', ...)
    .bind('mouseleave', function() {stopMoving();});

        // Bind the startMoving and stopMoving functions
        // to the HTML object for external access
        this.startMoving = startMoving;
        this.stopMoving = stopMoving;

    if (params.arrows && !params.fixedArrows) {
      // Initialise arrow opacity
      setArrowOpacity();
    }
    else {
      // Hide arrows
      $('.arrowleft, .arrowright, .arrowtop, .arrowbottom', ctnr).hide();
    }
  });

  return this;
};


// Backward compatibility with jQuery 1.1.x
if (!$.fn.offset) {
  $.fn.offset = function() {
    this.left = this.top = 0;

    if (this[0] && this[0].offsetParent) {
      var obj = this[0];
      do {
        this.left += obj.offsetLeft;
        this.top += obj.offsetTop;
      } while (obj = obj.offsetParent);
    }

    return this;
  }
}



/**
 * HoverScroll default parameters
 */
$.fn.hoverscroll.params = {
  width:    400,        // Width of the list
  height:   50,         // Height of the list
  arrows:   true,       // Display arrows to the left and top or the top and bottom
  arrowsOpacity:  0.7,    // Maximum opacity of the arrows if fixedArrows
    fixedArrows: false,     // Fix the displayed arrows to the side of the list
  rtl:    false,    // Set display mode to "Right to Left"
  debug:    false       // Display some debugging information in firebug console
};



/**
 * Log errors to consoles (firebug, opera) if exist, else uses alert()
 */
$.log = function() {
  try {console.log.apply(console, arguments);}
  catch (e) {
    try {opera.postError.apply(opera, arguments);}
    catch (e) {
//            alert(Array.prototype.join.call(arguments, " "));
        }
  }
};


})(jQuery);
