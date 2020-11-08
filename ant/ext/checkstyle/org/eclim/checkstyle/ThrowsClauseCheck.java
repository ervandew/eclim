package org.eclim.checkstyle;

import com.puppycrawl.tools.checkstyle.StatelessCheck;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Check the location of the 'throws' clause.
 */
@StatelessCheck
public class ThrowsClauseCheck
  extends AbstractCheck
{
  @Override
  public int[] getDefaultTokens()
  {
    return new int[] {
      TokenTypes.LITERAL_THROWS,
    };
  }

  @Override
  public int[] getAcceptableTokens()
  {
    return getDefaultTokens();
  }

  @Override
  public int[] getRequiredTokens()
  {
    return getDefaultTokens();
  }

  @Override
  public void visitToken(DetailAST aAST)
  {
    switch (aAST.getType()) {
      case TokenTypes.LITERAL_THROWS:
        visitLiteralThrows(aAST);
        break;
      default:
        throw new IllegalStateException(aAST.toString());
    }
  }

  private void visitLiteralThrows(DetailAST aAST)
  {
    final String throwsLine = getLines()[aAST.getLineNo() - 1];
    if (!CommonUtil.hasWhitespaceBefore(aAST.getColumnNo(), throwsLine)) {
        log(aAST.getLineNo(), aAST.getColumnNo(), "line.new", "throws");
    }
  }

  @Override
  protected String getMessageBundle()
  {
    return "com.puppycrawl.tools.checkstyle.checks.blocks.messages";
  }
}
