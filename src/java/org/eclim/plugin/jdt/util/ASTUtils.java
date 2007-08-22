/**
 * Copyright (c) 2005 - 2006
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

import org.eclim.logging.Logger;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;

import org.eclipse.jdt.internal.corext.dom.NodeFinder;

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
  private static final Logger logger = Logger.getLogger(ASTUtils.class);

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

  /**
   * Finds the node at the specified offset.
   *
   * @param _cu The CompilationUnit.
   * @param _offset The node offset in the compilation unit.
   * @return The node at the specified offset.
   */
  public static ASTNode findNode (CompilationUnit _cu, int _offset)
    throws Exception
  {
    NodeFinder finder= new NodeFinder(_offset, 1);
    _cu.accept(finder);
    //return finder.getCoveredNode();
    return finder.getCoveringNode();
  }

  /**
   * Finds the node at the specified offset that matches up with the supplied
   * IJavaElement.
   *
   * @param _cu The CompilationUnit.
   * @param _offset The node offset in the compilation unit.
   * @param _element The IJavaElement to match.
   * @return The node at the specified offset.
   */
  public static ASTNode findNode (
      CompilationUnit _cu, int _offset, IJavaElement _element)
    throws Exception
  {
    ASTNode node = findNode(_cu, _offset);
    if(node == null){
      return null;
    }

    if(_element.getElementType() == IJavaElement.TYPE_PARAMETER){
      _element = _element.getParent();
    }

    switch(_element.getElementType()){
      case IJavaElement.PACKAGE_DECLARATION:
        node = resolveNode(node, PackageDeclaration.class);
        break;
      case IJavaElement.IMPORT_DECLARATION:
        node = resolveNode(node, ImportDeclaration.class);
        break;
      case IJavaElement.TYPE:
        node = resolveNode(node, AbstractTypeDeclaration.class);
        break;
      case IJavaElement.INITIALIZER:
        node = resolveNode(node, Initializer.class);
        break;
      case IJavaElement.FIELD:
        node = resolveNode(node, FieldDeclaration.class);
        break;
      case IJavaElement.METHOD:
        node = resolveNode(node, MethodDeclaration.class);
        break;
      default:
        logger.info("findNode(CompilationUnit,int,IJavaElement) - " +
            "unrecognized element type " + _element.getElementType());
    }
    return node;
  }

  /**
   * Walk up the node tree until a node of the specified type is reached.
   *
   * @param _node The starting node.
   * @param _type The type to resolve.
   * @return The resulting node.
   */
  private static ASTNode resolveNode (ASTNode _node, Class _type)
    throws Exception
  {
    if(_node == null){
      return null;
    }

    if(_type.isAssignableFrom(_node.getClass())){
      return _node;
    }

    return resolveNode(_node.getParent(), _type);
  }
}
