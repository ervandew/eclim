eclim = {
  init: function(){
    // give tree menu structure open close capabilities.
    $("#left div.sidebar ul li")
      .filter(function(){
        return $("ul", this).hide().length !== 0;
      })
      .addClass("branch")
      .children("a")
      .removeAttr("href")
      .addClass("closed")
      .click(function(e){
        var node = $(this);
        if (node.is(".closed")){
          node.removeClass("closed").addClass("open").siblings("ul").show();
        }else{
          node.removeClass("open").addClass("closed").siblings("ul").hide();
        }
      });

    // open the tree and hightlight the node for the currently selected page.
    var test = $("#left div.sidebar ul li a")
      .filter(function(){
        var anchor = $(this);
        var href = anchor.attr("href");
        if (!href){
          return !anchor.is(".closed");
        }
        return false;
      })
      .parent()
      .addClass("selected")
      .parents("li")
      .children("a")
      .removeClass("closed")
      .addClass("open")
      .siblings("ul")
      .show();
  }
};

$(document).ready(eclim.init);
