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
package org.eclim.plugin.jdt.command.doc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import org.apache.commons.lang.StringUtils;

import org.apache.log4j.Logger;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.preference.Preferences;

import org.eclim.plugin.jdt.util.ASTUtils;
import org.eclim.plugin.jdt.util.JavaUtils;
import org.eclim.plugin.jdt.util.TypeUtils;

import org.eclim.util.file.FileUtils;

import org.eclipse.core.resources.IProject;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Handles requests to add javadoc comments to an element.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class CommentCommand
  extends AbstractCommand
{
  private static final Logger logger = Logger.getLogger(CommentCommand.class);

  private static final Pattern THROWS_PATTERN =
    Pattern.compile("\\s*[a-zA-Z0-9._]*\\.(\\w*)($|\\s.*)");

  private static final String INHERIT_DOC =
    "{" + TagElement.TAG_INHERITDOC + "}";

  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
    throws IOException
  {
    try{
      String project = _commandLine.getValue(Options.PROJECT_OPTION);
      String file = _commandLine.getValue(Options.FILE_OPTION);
      int offset = _commandLine.getIntValue(Options.OFFSET_OPTION);

      ICompilationUnit src = JavaUtils.getCompilationUnit(project, file);
      IJavaElement element = src.getElementAt(offset);
      // don't comment import declarations.
      if(element.getElementType() == IJavaElement.IMPORT_DECLARATION){
        return "";
      }

      CompilationUnit cu = ASTUtils.getCompilationUnit(src, true);
      ASTNode node = ASTUtils.findNode(cu, offset, element);

      if(node != null){
        comment(node, element);
      }

      ASTUtils.commitCompilationUnit(src, cu);

      return "";
    }catch(Exception e){
      return e;
    }
  }

  /**
   * Comment the supplied node.
   *
   * @param _node The node to comment.
   * @param _element The IJavaElement this node corresponds to.
   */
  private void comment (ASTNode _node, IJavaElement _element)
    throws Exception
  {
    Javadoc javadoc = null;
    boolean isNew = false;
    if (_node instanceof PackageDeclaration){
      javadoc = ((PackageDeclaration)_node).getJavadoc();
      if(javadoc == null){
        isNew = true;
        javadoc = _node.getAST().newJavadoc();
        ((PackageDeclaration)_node).setJavadoc(javadoc);
      }
    }else{
      javadoc = ((BodyDeclaration)_node).getJavadoc();
      if(javadoc == null){
        isNew = true;
        javadoc = _node.getAST().newJavadoc();
        ((BodyDeclaration)_node).setJavadoc(javadoc);
      }
    }

    switch(_node.getNodeType()){
      case ASTNode.PACKAGE_DECLARATION:
        commentPackage(javadoc, _element, isNew);
        break;
      case ASTNode.ENUM_DECLARATION:
      case ASTNode.TYPE_DECLARATION:
        commentType(javadoc, _element, isNew);
        break;
      case ASTNode.METHOD_DECLARATION:
        commentMethod(javadoc, _element, isNew);
        break;
      default:
        commentOther(javadoc, _element, isNew);
    }
  }

  /**
   * Comment a package declaration.
   *
   * @param _javadoc The Javadoc.
   * @param _element The IJavaElement.
   * @param _isNew true if there was no previous javadoc for this element.
   */
  private void commentPackage (
      Javadoc _javadoc, IJavaElement _element, boolean _isNew)
    throws Exception
  {
    IProject project = _element.getJavaProject().getProject();
    String copyright = getPreferences().getPreference(
      project, Preferences.PROJECT_COPYRIGHT_PREFERENCE);
    if(copyright != null && copyright.trim().length() > 0){
      File file = FileUtils.getProjectRelativeFile(project, copyright);
      if(!file.exists()){
        throw new IllegalArgumentException(
            Services.getMessage("project.copyright.not.found", file));
      }

      List tags = _javadoc.tags();
      tags.clear();

      BufferedReader reader = null;
      try{
        reader = new BufferedReader(new FileReader(file));
        String line = null;
        while ((line = reader.readLine()) != null){
          addTag(_javadoc, tags.size(), null, line);
        }
      }finally{
        IOUtils.closeQuietly(reader);
      }
    }else{
      commentOther(_javadoc, _element, _isNew);
    }
  }

  /**
   * Comment a type declaration.
   *
   * @param _javadoc The Javadoc.
   * @param _element The IJavaElement.
   * @param _isNew true if there was no previous javadoc for this element.
   */
  private void commentType (
      Javadoc _javadoc, IJavaElement _element, boolean _isNew)
    throws Exception
  {
    if(_element.getParent().getElementType() == IJavaElement.COMPILATION_UNIT){
      List tags = _javadoc.tags();
      IProject project = _element.getJavaProject().getProject();
      if(_isNew){
        addTag(_javadoc, tags.size(), null, "");
        addTag(_javadoc, tags.size(), null, "");
        addTag(_javadoc, tags.size(), TagElement.TAG_AUTHOR, getAuthor(project));
        String version = getPreferences().getPreference(
            project, "org.eclim.java.doc.version");
        version = StringUtils.replace(version, "\\$", "$");
        addTag(_javadoc, tags.size(), TagElement.TAG_VERSION, version);
      }else{
        // check if author tag exists.
        int index = -1;
        String author = getAuthor(project);
        for (int ii = 0; ii < tags.size(); ii++){
          TagElement tag = (TagElement)tags.get(ii);
          if(TagElement.TAG_AUTHOR.equals(tag.getTagName())){
            String authorText = tag.fragments().size() > 0 ?
              ((TextElement)tag.fragments().get(0)).getText() : null;
            // check if author tag is the same.
            if(authorText != null && author.trim().equals(authorText.trim())){
              index = -1;
              break;
            }
            index = ii + 1;
          }else if(tag.getTagName() != null){
            if(index == -1){
              index = ii;
            }
          }
        }

        // insert author tag if it doesn't exist.
        if(index > -1){
          TagElement authorTag = _javadoc.getAST().newTagElement();
          TextElement authorText = _javadoc.getAST().newTextElement();
          authorText.setText(author);
          authorTag.setTagName(TagElement.TAG_AUTHOR);
          authorTag.fragments().add(authorText);
          tags.add(index, authorTag);
        }

        // add the version tag if it doesn't exist.
        boolean versionExists = false;
        for (int ii = 0; ii < tags.size(); ii++){
          TagElement tag = (TagElement)tags.get(ii);
          if(TagElement.TAG_VERSION.equals(tag.getTagName())){
            versionExists = true;
            break;
          }
        }
        if (!versionExists){
          String version = getPreferences().getPreference(
              project, "org.eclim.java.doc.version");
          version = StringUtils.replace(version, "\\$", "$");
          addTag(_javadoc, tags.size(), TagElement.TAG_VERSION, version);
        }
      }
    }else{
      commentOther(_javadoc, _element, _isNew);
    }
  }

  /**
   * Comment a method declaration.
   *
   * @param _javadoc The Javadoc.
   * @param _element The IJavaElement.
   * @param _isNew true if there was no previous javadoc for this element.
   */
  private void commentMethod (
      Javadoc _javadoc, IJavaElement _element, boolean _isNew)
    throws Exception
  {
    IMethod method = (IMethod)_element;
    List tags = _javadoc.tags();

    if(_isNew){
      // see if method is overriding / implementing method from superclass
      IType parentType = null;
      IType[] types = TypeUtils.getSuperTypes(method.getDeclaringType());
      for (int ii = 0; ii < types.length; ii++){
        if(TypeUtils.containsMethod(types[ii], method)){
          parentType = types[ii];
          break;
        }
      }

      // if an inherited method, add inheritDoc and @see
      if(parentType != null){
        addTag(_javadoc, tags.size(), null, INHERIT_DOC);

        String signature = JavaUtils.getFullyQualifiedName(
            TypeUtils.getMethod(parentType, method));
        addTag(_javadoc, tags.size(), TagElement.TAG_SEE, signature);
        return;
      }else{
        addTag(_javadoc, tags.size(), null, "");
        addTag(_javadoc, tags.size(), null, "");
      }
    }

    // only add/update tags if javadoc doesn't contain inheritDoc.
    boolean update = true;
    for (Iterator ii = tags.iterator(); ii.hasNext();){
      TagElement tag = (TagElement)ii.next();
      if(tag.getTagName() == null && tag.fragments().size() > 0){
        String text = ((TextElement)tag.fragments().get(0)).getText();
        if(INHERIT_DOC.equals(text)){
          update = false;
          break;
        }
      }
    }

    if(update){
      addUpdateParamTags(_javadoc, method, _isNew);
      addUpdateReturnTag(_javadoc, method, _isNew);
      addUpdateThrowsTags(_javadoc, method, _isNew);
    }
  }

  /**
   * Comment everything else.
   *
   * @param _javadoc The Javadoc.
   * @param _element The IJavaElement.
   * @param _isNew true if there was no previous javadoc for this element.
   */
  private void commentOther (
      Javadoc _javadoc, IJavaElement _element, boolean _isNew)
    throws Exception
  {
    if(_isNew){
      addTag(_javadoc, 0, null, "");
    }
  }

  /**
   * Add or update the param tags for the given method.
   *
   * @param _javadoc The Javadoc instance.
   * @param _method The method.
   * @param _isNew true if we're adding to brand new javadocs.
   */
  private void addUpdateParamTags (
      Javadoc _javadoc, IMethod _method, boolean _isNew)
    throws Exception
  {
    List tags = _javadoc.tags();
    String[] params = _method.getParameterNames();
    if(_isNew){
      for (int ii = 0; ii < params.length; ii++){
        addTag(_javadoc, tags.size(), TagElement.TAG_PARAM, params[ii]);
      }
    }else{
      // find current params.
      int index = 0;
      Map current = new HashMap();
      for (int ii = 0; ii < tags.size(); ii++){
        TagElement tag = (TagElement)tags.get(ii);
        if(TagElement.TAG_PARAM.equals(tag.getTagName())){
          if(current.size() == 0){
            index = ii;
          }
          Object element = tag.fragments().size() > 0 ?
            tag.fragments().get(0) : null;
          if(element != null && element instanceof Name){
            String name = ((Name)element).getFullyQualifiedName();
            current.put(name, tag);
          }else{
            current.put(String.valueOf(ii), tag);
          }
        }else{
          if(current.size() > 0){
            break;
          }
          if(tag.getTagName() == null){
            index = ii + 1;
          }
        }
      }

      if(current.size() > 0){
        for (int ii = 0; ii < params.length; ii++){
          if(current.containsKey(params[ii])){
            TagElement tag = (TagElement)current.get(params[ii]);
            int currentIndex = tags.indexOf(tag);
            if(currentIndex != ii){
              tags.remove(tag);
              tags.add(index + ii, tag);
            }
            current.remove(params[ii]);
          }else{
            addTag(_javadoc, index + ii, TagElement.TAG_PARAM, params[ii]);
          }
        }

        // remove any other param tags.
        for (Iterator ii = current.keySet().iterator(); ii.hasNext();){
          tags.remove(current.get(ii.next()));
        }
      }else{
        for (int ii = 0; ii < params.length; ii++){
          addTag(_javadoc, index + ii, TagElement.TAG_PARAM, params[ii]);
        }
      }
    }
  }

  /**
   * Add or update the return tag for the given method.
   *
   * @param _javadoc The Javadoc instance.
   * @param _method The method.
   * @param _isNew true if we're adding to brand new javadocs.
   */
  private void addUpdateReturnTag (
      Javadoc _javadoc, IMethod _method, boolean _isNew)
    throws Exception
  {
    List tags = _javadoc.tags();
    // get return type from element.
    if(!_method.isConstructor()){
      String returnType =
        Signature.getSignatureSimpleName(_method.getReturnType());
      if (!"void".equals(returnType)){
        if(_isNew){
          addTag(_javadoc, tags.size(), TagElement.TAG_RETURN, "");
        }else{
          // search starting from the bottom since @return should be near the
          // end.
          int index = tags.size();
          for (int ii = tags.size() - 1; ii >= 0; ii--){
            TagElement tag = (TagElement)tags.get(ii);
            // return tag already exists?
            if(TagElement.TAG_RETURN.equals(tag.getTagName())){
              index = -1;
              break;
            }
            // if we hit the param tags, or the main text, insert below them.
            if (TagElement.TAG_PARAM.equals(tag.getTagName()) ||
                tag.getTagName() == null)
            {
              index = ii + 1;
              break;
            }
            index = ii;
          }
          if(index > -1){
            addTag(_javadoc, index, TagElement.TAG_RETURN, "");
          }
        }
      }else{
        // remove any return tag that may exist.
        for (int ii = tags.size() - 1; ii >= 0; ii--){
          TagElement tag = (TagElement)tags.get(ii);
          // return tag already exists?
          if(TagElement.TAG_RETURN.equals(tag.getTagName())){
            tags.remove(tag);
          }
          // if we hit the param tags, or the main text we can stop.
          if (TagElement.TAG_PARAM.equals(tag.getTagName()) ||
              tag.getTagName() == null)
          {
            break;
          }
        }
      }
    }
  }

  /**
   * Add or update the throws tags for the given method.
   *
   * @param _javadoc The Javadoc instance.
   * @param _method The method.
   * @param _isNew true if we're adding to brand new javadocs.
   */
  private void addUpdateThrowsTags (
      Javadoc _javadoc, IMethod _method, boolean _isNew)
    throws Exception
  {
    List tags = _javadoc.tags();

    // get thrown exceptions from element.
    String[] exceptions = _method.getExceptionTypes();
    if(_isNew && exceptions.length > 0){
      addTag(_javadoc, tags.size(), null, "");
      for (int ii = 0; ii < exceptions.length; ii++){
        addTag(_javadoc, tags.size(), TagElement.TAG_THROWS,
            Signature.getSignatureSimpleName(exceptions[ii]));
      }
    }else{
      // get current throws tags
      Map current = new HashMap();
      int index = tags.size();
      for (int ii = tags.size() - 1; ii >= 0; ii--){
        TagElement tag = (TagElement)tags.get(ii);
        if(TagElement.TAG_THROWS.equals(tag.getTagName())){
          index = index == tags.size() ? ii + 1 : index;
          Name name = tag.fragments().size() > 0 ?
            (Name)tag.fragments().get(0) : null;
          if(name != null){
            String text = name.getFullyQualifiedName();
            String key = THROWS_PATTERN.matcher(text).replaceFirst("$1");
            current.put(key, tag);
          }else{
            current.put(String.valueOf(ii), tag);
          }
        }
        // if we hit the return tag, a param tag, or the main text we can stop.
        if (TagElement.TAG_PARAM.equals(tag.getTagName()) ||
            TagElement.TAG_RETURN.equals(tag.getTagName()) ||
            tag.getTagName() == null)
        {
          break;
        }
      }

      // see what needs to be added / removed.
      for (int ii = 0; ii < exceptions.length; ii++){
        String name = Signature.getSignatureSimpleName(exceptions[ii]);
        if(!current.containsKey(name)){
          addTag(_javadoc, index, TagElement.TAG_THROWS, name);
        }else{
          current.remove(name);
        }
      }

      // remove any left over thows clauses.
      for (Iterator ii = current.keySet().iterator(); ii.hasNext();){
        tags.remove(current.get(ii.next()));
      }
    }
  }

  /**
   * Gets the author string.
   *
   * @return The author.
   */
  private String getAuthor (IProject _project)
    throws Exception
  {
    String username = getPreferences().getPreference(
        _project.getProject(), Preferences.USERNAME_PREFERENCE);
    String email = getPreferences().getPreference(
        _project.getProject(), Preferences.USEREMAIL_PREFERENCE);

    // build the author string.
    StringBuffer author = new StringBuffer();
    if (username != null && username.trim().length() > 0){
      author.append(username);
      if (email != null && email.trim().length() > 0){
        author.append(" (")
          .append(email)
          .append(")");
      }
    }else if (email != null && email.trim().length() > 0){
      author.append(email);
    }
    return author.toString();
  }

  /**
   * Adds a tag to the supplied list of tags.
   *
   * @param _javadoc The Javadoc instance.
   * @param _index The index to insert the new tag at.
   * @param _name The tag name.
   * @param _text The tag text.
   */
  private void addTag (
      Javadoc _javadoc, int _index, String _name, String _text)
    throws Exception
  {
    TagElement tag = _javadoc.getAST().newTagElement();
    TextElement text = _javadoc.getAST().newTextElement();
    text.setText(_text);
    tag.setTagName(_name);
    tag.fragments().add(text);
    _javadoc.tags().add(_index, tag);
  }
}
