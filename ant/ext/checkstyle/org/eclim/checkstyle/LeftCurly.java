package org.eclim.checkstyle;

import java.util.Locale;

import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import com.puppycrawl.tools.checkstyle.checks.blocks.LeftCurlyCheck;
import com.puppycrawl.tools.checkstyle.checks.blocks.LeftCurlyOption;

import com.puppycrawl.tools.checkstyle.utils.CommonUtil;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

/**
 * Extension to default LeftCurlyCheck which allows not requiring curly on a new
 * line for anonymous classes.
 */
public class LeftCurly
  extends LeftCurlyCheck
{
  // copied from LeftCurlyCheck
  private static final String OPEN_CURLY_BRACE = "{";
  private LeftCurlyOption option = LeftCurlyOption.EOL;
  private boolean ignoreEnums = true;

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

  @Override
  protected String getMessageBundle()
  {
    String className = getClass().getSuperclass().getName();
    String packageName = className.substring(0, className.lastIndexOf('.'));
    return packageName + ".messages";
  }

  // straight copy from LeftCurlyCheck
  @Override
  public void visitToken(DetailAST ast)
  {
    final DetailAST startToken;
    DetailAST brace;

    switch (ast.getType()) {
      case TokenTypes.CTOR_DEF:
      case TokenTypes.METHOD_DEF:
      case TokenTypes.COMPACT_CTOR_DEF:
        startToken = skipModifierAnnotations(ast);
        brace = ast.findFirstToken(TokenTypes.SLIST);
        break;
      case TokenTypes.INTERFACE_DEF:
      case TokenTypes.CLASS_DEF:
      case TokenTypes.ANNOTATION_DEF:
      case TokenTypes.ENUM_DEF:
      case TokenTypes.ENUM_CONSTANT_DEF:
      case TokenTypes.RECORD_DEF:
        startToken = skipModifierAnnotations(ast);
        final DetailAST objBlock = ast.findFirstToken(TokenTypes.OBJBLOCK);
        brace = objBlock;

        if (objBlock != null) {
            brace = objBlock.getFirstChild();
        }
        break;
      case TokenTypes.LITERAL_WHILE:
      case TokenTypes.LITERAL_CATCH:
      case TokenTypes.LITERAL_SYNCHRONIZED:
      case TokenTypes.LITERAL_FOR:
      case TokenTypes.LITERAL_TRY:
      case TokenTypes.LITERAL_FINALLY:
      case TokenTypes.LITERAL_DO:
      case TokenTypes.LITERAL_IF:
      case TokenTypes.STATIC_INIT:
      case TokenTypes.LAMBDA:
        startToken = ast;
        brace = ast.findFirstToken(TokenTypes.SLIST);
        break;
      case TokenTypes.LITERAL_ELSE:
        startToken = ast;
        brace = getBraceAsFirstChild(ast);
        break;
      case TokenTypes.LITERAL_CASE:
      case TokenTypes.LITERAL_DEFAULT:
        startToken = ast;
        brace = getBraceFromSwitchMember(ast);
        break;
      default:
        // ATTENTION! We have default here, but we expect case TokenTypes.METHOD_DEF,
        // TokenTypes.LITERAL_FOR, TokenTypes.LITERAL_WHILE, TokenTypes.LITERAL_DO only.
        // It has been done to improve coverage to 100%. I couldn't replace it with
        // if-else-if block because code was ugly and didn't pass pmd check.

        startToken = ast;
        brace = ast.findFirstToken(TokenTypes.LCURLY);
        break;
    }

    if (brace != null) {
      verifyBrace(brace, startToken);
    }
  }

  // copied from LeftCurlyCheck, but updated to ignore anonymous class methods
  // if configured to do so.
  private void verifyBrace(final DetailAST brace, final DetailAST startToken) {
    final String braceLine = getLine(brace.getLineNo() - 1);

    // Check for being told to ignore, or have '{}' which is a special case
    if (braceLine.length() <= brace.getColumnNo() + 1
        || braceLine.charAt(brace.getColumnNo() + 1) != '}') {
      if (option == LeftCurlyOption.NL) {
        if (!CommonUtil.hasWhitespaceBefore(brace.getColumnNo(), braceLine)) {
          // eclim addition: ignore anonymous class methods if configured to
          if (ignoreAnonymous && startToken.getType() == TokenTypes.METHOD_DEF){
            DetailAST parent = startToken.getParent();
            if (parent != null){
              parent = parent.getParent();
            }
            if (parent != null && parent.getType() == TokenTypes.LITERAL_NEW){
              return;
            }
          }
          log(brace, MSG_KEY_LINE_NEW, OPEN_CURLY_BRACE, brace.getColumnNo() + 1);
        }
      }
      else if (option == LeftCurlyOption.EOL) {
        validateEol(brace, braceLine);
      }
      else if (!TokenUtil.areOnSameLine(startToken, brace)) {
        validateNewLinePosition(brace, startToken, braceLine);
      }
    }
  }

  // straight copy from LeftCurlyCheck
  private void validateEol(DetailAST brace, String braceLine) {
    if (CommonUtil.hasWhitespaceBefore(brace.getColumnNo(), braceLine)) {
      log(brace, MSG_KEY_LINE_PREVIOUS, OPEN_CURLY_BRACE, brace.getColumnNo() + 1);
    }
    if (!hasLineBreakAfter(brace)) {
      log(brace, MSG_KEY_LINE_BREAK_AFTER, OPEN_CURLY_BRACE, brace.getColumnNo() + 1);
    }
  }

  // straight copy from LeftCurlyCheck
  private void validateNewLinePosition(DetailAST brace, DetailAST startToken, String braceLine) {
    // not on the same line
    if (startToken.getLineNo() + 1 == brace.getLineNo()) {
      if (CommonUtil.hasWhitespaceBefore(brace.getColumnNo(), braceLine)) {
        log(brace, MSG_KEY_LINE_PREVIOUS, OPEN_CURLY_BRACE, brace.getColumnNo() + 1);
      }
      else {
        log(brace, MSG_KEY_LINE_NEW, OPEN_CURLY_BRACE, brace.getColumnNo() + 1);
      }
    }
    else if (!CommonUtil.hasWhitespaceBefore(brace.getColumnNo(), braceLine)) {
      log(brace, MSG_KEY_LINE_NEW, OPEN_CURLY_BRACE, brace.getColumnNo() + 1);
    }
  }

  // straight copy from LeftCurlyCheck
  private boolean hasLineBreakAfter(DetailAST leftCurly) {
    DetailAST nextToken = null;
    if (leftCurly.getType() == TokenTypes.SLIST) {
      nextToken = leftCurly.getFirstChild();
    }
    else {
      if (!ignoreEnums
          && leftCurly.getParent().getParent().getType() == TokenTypes.ENUM_DEF) {
        nextToken = leftCurly.getNextSibling();
      }
    }
    return nextToken == null
        || nextToken.getType() == TokenTypes.RCURLY
        || !TokenUtil.areOnSameLine(leftCurly, nextToken);
  }

  // straight copy from LeftCurlyCheck
  private static DetailAST getBraceFromSwitchMember(DetailAST ast) {
    final DetailAST brace;
    final DetailAST parent = ast.getParent();
    if (parent.getType() == TokenTypes.SWITCH_RULE) {
      brace = parent.findFirstToken(TokenTypes.SLIST);
    }
    else {
      brace = getBraceAsFirstChild(ast.getNextSibling());
    }
    return brace;
  }

  // straight copy from LeftCurlyCheck
  private static DetailAST getBraceAsFirstChild(DetailAST ast) {
    DetailAST brace = null;
    if (ast != null) {
      final DetailAST candidate = ast.getFirstChild();
      if (candidate != null && candidate.getType() == TokenTypes.SLIST) {
        brace = candidate;
      }
    }
    return brace;
  }

  // straight copy from LeftCurlyCheck
  private static DetailAST findLastAnnotation(DetailAST modifiers) {
    DetailAST annotation = modifiers.findFirstToken(TokenTypes.ANNOTATION);
    while (annotation != null && annotation.getNextSibling() != null
        && annotation.getNextSibling().getType() == TokenTypes.ANNOTATION) {
      annotation = annotation.getNextSibling();
    }
    return annotation;
  }

  // straight copy from LeftCurlyCheck
  private static DetailAST skipModifierAnnotations(DetailAST ast) {
    DetailAST resultNode = ast;
    final DetailAST modifiers = ast.findFirstToken(TokenTypes.MODIFIERS);

    if (modifiers != null) {
      final DetailAST lastAnnotation = findLastAnnotation(modifiers);

      if (lastAnnotation != null) {
        if (lastAnnotation.getNextSibling() == null) {
          resultNode = modifiers.getNextSibling();
        }
        else {
          resultNode = lastAnnotation.getNextSibling();
        }
      }
    }
    return resultNode;
  }

  // straight copy from LeftCurlyCheck
  public void setIgnoreEnums(boolean ignoreEnums) {
    this.ignoreEnums = ignoreEnums;
  }

  // straight copy from LeftCurlyCheck
  public void setOption(String optionStr) {
    option = LeftCurlyOption.valueOf(optionStr.trim().toUpperCase(Locale.ENGLISH));
  }
}
