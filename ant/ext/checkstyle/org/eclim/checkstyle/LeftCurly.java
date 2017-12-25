package org.eclim.checkstyle;

import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.Utils;

import com.puppycrawl.tools.checkstyle.checks.blocks.LeftCurlyCheck;
import com.puppycrawl.tools.checkstyle.checks.blocks.LeftCurlyOption;

/**
 * Extension to default LeftCurlyCheck which allows not requiring curly on a new
 * line for anonymous classes.
 */
public class LeftCurly
  extends LeftCurlyCheck
{
  // copied from LeftCurlyCheck
  private static final int DEFAULT_MAX_LINE_LENGTH = 80;
  private int mMaxLineLength = DEFAULT_MAX_LINE_LENGTH;

  private boolean ignoreAnonymous;

  /**
   * Sets whether or not this instance should ignore anonymous class methods.
   *
   * @param ignoreAnonymous true to ignore anonymouse class methods, false
   * otherwise.
   */
  public void setIgnoreAnonymousClassMethods(boolean ignoreAnonymous)
  {
    this.ignoreAnonymous = ignoreAnonymous;
  }

  // straight copy from LeftCurlyCheck
  @Override
  public void visitToken(DetailAST aAST)
  {
    final DetailAST startToken;
    final DetailAST brace;

    switch (aAST.getType()) {
      case TokenTypes.CTOR_DEF :
      case TokenTypes.METHOD_DEF :
        startToken = skipAnnotationOnlyLines(aAST);
        brace = aAST.findFirstToken(TokenTypes.SLIST);
        break;

      case TokenTypes.INTERFACE_DEF :
      case TokenTypes.CLASS_DEF :
      case TokenTypes.ANNOTATION_DEF :
      case TokenTypes.ENUM_DEF :
      case TokenTypes.ENUM_CONSTANT_DEF :
        startToken = skipAnnotationOnlyLines(aAST);
        final DetailAST objBlock = aAST.findFirstToken(TokenTypes.OBJBLOCK);
        brace = (objBlock == null)
          ? null
          : (DetailAST) objBlock.getFirstChild();
        break;

      case TokenTypes.LITERAL_WHILE:
      case TokenTypes.LITERAL_CATCH:
      case TokenTypes.LITERAL_SYNCHRONIZED:
      case TokenTypes.LITERAL_FOR:
      case TokenTypes.LITERAL_TRY:
      case TokenTypes.LITERAL_FINALLY:
      case TokenTypes.LITERAL_DO:
      case TokenTypes.LITERAL_IF :
        startToken = aAST;
        brace = aAST.findFirstToken(TokenTypes.SLIST);
        break;

      case TokenTypes.LITERAL_ELSE :
        startToken = aAST;
        final DetailAST candidate = aAST.getFirstChild();
        brace =
          (candidate.getType() == TokenTypes.SLIST)
          ? candidate
          : null; // silently ignore
        break;

      case TokenTypes.LITERAL_SWITCH :
        startToken = aAST;
        brace = aAST.findFirstToken(TokenTypes.LCURLY);
        break;

      default :
        startToken = null;
        brace = null;
    }

    if ((brace != null) && (startToken != null)) {
      verifyBrace(brace, startToken);
    }
  }

  // straight copy from LeftCurlyCheck
  private DetailAST skipAnnotationOnlyLines(DetailAST aAST)
  {
    final DetailAST modifiers = aAST.findFirstToken(TokenTypes.MODIFIERS);
    if (modifiers == null) {
      return aAST;
    }
    DetailAST lastAnnot = findLastAnnotation(modifiers);
    if (lastAnnot == null) {
      // There are no annotations.
      return aAST;
    }
    final DetailAST tokenAfterLast = lastAnnot.getNextSibling() != null
      ? lastAnnot.getNextSibling()
      : modifiers.getNextSibling();
    if (tokenAfterLast.getLineNo() > lastAnnot.getLineNo()) {
      return tokenAfterLast;
    }
    final int lastAnnotLineNumber = lastAnnot.getLineNo();
    while (lastAnnot.getPreviousSibling() != null
        && (lastAnnot.getPreviousSibling().getLineNo()
          == lastAnnotLineNumber))
    {
      lastAnnot = lastAnnot.getPreviousSibling();
    }
    return lastAnnot;
  }

  // straight copy from LeftCurlyCheck
  private DetailAST findLastAnnotation(DetailAST aModifiers)
  {
    DetailAST aAnnot = aModifiers.findFirstToken(TokenTypes.ANNOTATION);
    while (aAnnot != null && aAnnot.getNextSibling() != null
        && aAnnot.getNextSibling().getType() == TokenTypes.ANNOTATION)
    {
      aAnnot = aAnnot.getNextSibling();
    }
    return aAnnot;
  }

  // copied from LeftCurlyCheck, but updated to ignore anonymous class methods
  // if configured to do so.
  private void verifyBrace(final DetailAST aBrace,
      final DetailAST aStartToken)
  {
    final String braceLine = getLines()[aBrace.getLineNo() - 1];

    // calculate the previous line length without trailing whitespace. Need
    // to handle the case where there is no previous line, cause the line
    // being check is the first line in the file.
    final int prevLineLen = (aBrace.getLineNo() == 1)
      ? mMaxLineLength
      : Utils.lengthMinusTrailingWhitespace(
          getLines()[aBrace.getLineNo() - 2]);

    // Check for being told to ignore, or have '{}' which is a special case
    if ((braceLine.length() > (aBrace.getColumnNo() + 1))
        && (braceLine.charAt(aBrace.getColumnNo() + 1) == '}'))
    {
      ; // ignore
    }
    else if (getAbstractOption() == LeftCurlyOption.NL) {
      if (!Utils.whitespaceBefore(aBrace.getColumnNo(), braceLine)) {
        if (ignoreAnonymous && aStartToken.getType() == TokenTypes.METHOD_DEF){
          DetailAST parent = aStartToken.getParent();
          if (parent != null){
            parent = parent.getParent();
          }
          if (parent != null && parent.getType() == TokenTypes.LITERAL_NEW){
            return;
          }
        }
        log(aBrace.getLineNo(), aBrace.getColumnNo(),
            "line.new", "{");
      }
    }
    else if (getAbstractOption() == LeftCurlyOption.EOL) {
      if (Utils.whitespaceBefore(aBrace.getColumnNo(), braceLine)
          && ((prevLineLen + 2) <= mMaxLineLength))
      {
        log(aBrace.getLineNo(), aBrace.getColumnNo(),
            "line.previous", "{");
      }
    }
    else if (getAbstractOption() == LeftCurlyOption.NLOW) {
      if (aStartToken.getLineNo() == aBrace.getLineNo()) {
        ; // all ok as on the same line
      }
      else if ((aStartToken.getLineNo() + 1) == aBrace.getLineNo()) {
        if (!Utils.whitespaceBefore(aBrace.getColumnNo(), braceLine)) {
          log(aBrace.getLineNo(), aBrace.getColumnNo(),
              "line.new", "{");
        }
        else if ((prevLineLen + 2) <= mMaxLineLength) {
          log(aBrace.getLineNo(), aBrace.getColumnNo(),
              "line.previous", "{");
        }
      }
      else if (!Utils.whitespaceBefore(aBrace.getColumnNo(), braceLine)) {
        log(aBrace.getLineNo(), aBrace.getColumnNo(),
            "line.new", "{");
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
