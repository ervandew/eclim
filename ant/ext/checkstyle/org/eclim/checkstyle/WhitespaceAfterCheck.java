package org.eclim.checkstyle;

import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

/**
 * Extension to default WhitespaceAfterCheck which for COMMA, doesn't require a
 * space after the ',' in the type list of a generic
 * (eg. Map&lt;String,String&gt;).
 */
public class WhitespaceAfterCheck
  extends com.puppycrawl.tools.checkstyle.checks.whitespace.WhitespaceAfterCheck
{
  @Override
  public void visitToken(DetailAST aAST)
  {
    // Everything except the TYPE_ARGUMENT check towards the bottom is a
    // direct copy from the default WhitespaceAfterCheck.
    final Object[] message;
    final DetailAST targetAST;
    if (aAST.getType() == TokenTypes.TYPECAST) {
      targetAST = aAST.findFirstToken(TokenTypes.RPAREN);
      // TODO: i18n
      message = new Object[]{"cast"};
    }
    else {
      targetAST = aAST;
      message = new Object[]{aAST.getText()};
    }
    final String line = getLines()[targetAST.getLineNo() - 1];
    final int after =
      targetAST.getColumnNo() + targetAST.getText().length();

    if (after < line.length()) {

      final char charAfter = line.charAt(after);
      if ((targetAST.getType() == TokenTypes.SEMI)
          && ((charAfter == ';') || (charAfter == ')')))
      {
        return;
      }
      if (!Character.isWhitespace(charAfter)) {
        //empty FOR_ITERATOR?
        if (targetAST.getType() == TokenTypes.SEMI) {
          final DetailAST sibling =
            targetAST.getNextSibling();
          if ((sibling != null)
              && (sibling.getType() == TokenTypes.FOR_ITERATOR)
              && (sibling.getChildCount() == 0))
          {
            return;
          }
        }
        // type list for generics?
        if (targetAST.getType() == TokenTypes.COMMA) {
          final DetailAST sibling = targetAST.getNextSibling();
          if (sibling != null &&
              sibling.getType() == TokenTypes.TYPE_ARGUMENT)
          {
            return;
          }
        }

        log(targetAST.getLineNo(),
            targetAST.getColumnNo() + targetAST.getText().length(),
            "ws.notFollowed",
            message);
      }
    }
  }

  @Override
  protected String getMessageBundle()
  {
    String className = getClass().getSuperclass().getName();
    String packageName = className.substring(0, className.lastIndexOf('.'));
    return packageName + ".messages";
  }
}
