eclim = {
  init: function(){
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
  }
};

$(document).ready(eclim.init);
