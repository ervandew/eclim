/**
 * Copyright (c) 2004 - 2006
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclim.plugin.jdt.util;

import org.eclipse.jdt.core.ICompilationUnit;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import org.eclipse.jface.text.Document;

import org.eclipse.text.edits.TextEdit;

/**
 * Utility class for working with the eclipse java dom model.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ASTUtils
{
  private ASTUtils ()
  {
  }

  /**
   * Gets the AST CompilationUnit for the supplied ICompilationUnit.
   * <p/>
   * Equivalent of getCompilationUnit(_src, false).
   *
   * @param _src The ICompilationUnit.
   * @return The CompilationUnit.
   */
  public static CompilationUnit getCompilationUnit (ICompilationUnit _src)
    throws Exception
  {
    return getCompilationUnit(_src, false);
  }

  /**
   * Gets the AST CompilationUnit for the supplied ICompilationUnit.
   *
   * @param _src The ICompilationUnit.
   * @param _recordModifications true to record any modifications, false
   * otherwise.
   * @return The CompilationUnit.
   */
  public static CompilationUnit getCompilationUnit (
      ICompilationUnit _src, boolean _recordModifications)
    throws Exception
  {
    ASTParser parser = ASTParser.newParser(AST.JLS3);
    parser.setSource(_src);
    CompilationUnit src = (CompilationUnit)parser.createAST(null);
    if(_recordModifications){
      src.recordModifications();
    }

    return src;
  }

  /**
   * Commits any changes made to the supplied CompilationUnit.
   * <p/>
   * Note: The method expects that the CompilationUnit is recording the
   * modifications (getCompilationUnit(_src, true) was used).
   *
   * @param _src The original ICompilationUnit.
   * @param _node The CompilationUnit ast node.
   */
  public static void commitCompilationUnit (
      ICompilationUnit _src, CompilationUnit _node)
    throws Exception
  {
    Document document = new Document(_src.getBuffer().getContents());
    TextEdit edits = _node.rewrite(
        document, _src.getJavaProject().getOptions(true));
    edits.apply(document);
    _src.getBuffer().setContents(document.get());
    _src.save(null, false);
  }
}
