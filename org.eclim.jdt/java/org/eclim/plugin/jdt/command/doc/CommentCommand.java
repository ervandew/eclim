/**
 * Copyright (C) 2005 - 2015  Eric Van Dewoestine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.eclim.plugin.jdt.command.doc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.util.HashMap;
import java.util.List;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.preference.Preferences;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.plugin.jdt.util.ASTUtils;
import org.eclim.plugin.jdt.util.JavaUtils;
import org.eclim.plugin.jdt.util.MethodUtils;
import org.eclim.plugin.jdt.util.TypeInfo;
import org.eclim.plugin.jdt.util.TypeUtils;

import org.eclim.util.IOUtils;

import org.eclipse.core.resources.IProject;

import org.eclipse.jdt.core.IAnnotation;
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

import org.eclipse.jdt.core.formatter.CodeFormatter;

/**
 * Handles requests to add javadoc comments to an element.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "javadoc_comment",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED o offset ARG," +
    "OPTIONAL e encoding ARG"
)
public class CommentCommand
  extends AbstractCommand
{
  private static final Pattern THROWS_PATTERN =
    Pattern.compile("\\s*[a-zA-Z0-9._]*\\.(\\w*)($|\\s.*)");

  private static final String INHERIT_DOC =
    "{" + TagElement.TAG_INHERITDOC + "}";

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    int offset = getOffset(commandLine);

    ICompilationUnit src = JavaUtils.getCompilationUnit(project, file);
    IJavaElement element = src.getElementAt(offset);
    // don't comment import declarations.
    if(element.getElementType() == IJavaElement.IMPORT_DECLARATION){
      return null;
    }

    CompilationUnit cu = ASTUtils.getCompilationUnit(src, true);
    ASTNode node = ASTUtils.findNode(cu, offset, element);

    if(node != null){
      comment(src, node, element);

      ASTUtils.commitCompilationUnit(src, cu);

      // re-grab the compilation unit + node so we can get the javadoc node w/
      // its position and length set.
      cu = ASTUtils.getCompilationUnit(src, true);
      node = ASTUtils.findNode(cu, offset, element);
      Javadoc javadoc = (node instanceof PackageDeclaration) ?
        ((PackageDeclaration)node).getJavadoc() :
        ((BodyDeclaration)node).getJavadoc();
      int kind = CodeFormatter.K_COMPILATION_UNIT |
        CodeFormatter.F_INCLUDE_COMMENTS;
      int start = javadoc.getStartPosition();
      int length = javadoc.getLength();
      JavaUtils.format(src, kind, start, javadoc.getLength());
    }

    return null;
  }

  /**
   * Comment the supplied node.
   *
   * @param src The source file.
   * @param node The node to comment.
   * @param element The IJavaElement this node corresponds to.
   */
  private void comment(ICompilationUnit src, ASTNode node, IJavaElement element)
    throws Exception
  {
    Javadoc javadoc = null;
    boolean isNew = false;
    if (node instanceof PackageDeclaration){
      javadoc = ((PackageDeclaration)node).getJavadoc();
      if(javadoc == null){
        isNew = true;
        javadoc = node.getAST().newJavadoc();
        ((PackageDeclaration)node).setJavadoc(javadoc);
      }
    }else{
      javadoc = ((BodyDeclaration)node).getJavadoc();
      if(javadoc == null){
        isNew = true;
        javadoc = node.getAST().newJavadoc();
        ((BodyDeclaration)node).setJavadoc(javadoc);
      }
    }

    switch(node.getNodeType()){
      case ASTNode.PACKAGE_DECLARATION:
        commentPackage(src, javadoc, element, isNew);
        break;
      case ASTNode.ENUM_DECLARATION:
      case ASTNode.TYPE_DECLARATION:
        commentType(src, javadoc, element, isNew);
        break;
      case ASTNode.METHOD_DECLARATION:
        commentMethod(src, javadoc, element, isNew);
        break;
      default:
        commentOther(src, javadoc, element, isNew);
    }
  }

  /**
   * Comment a package declaration.
   *
   * @param src The source file.
   * @param javadoc The Javadoc.
   * @param element The IJavaElement.
   * @param isNew true if there was no previous javadoc for this element.
   */
  private void commentPackage(
      ICompilationUnit src, Javadoc javadoc, IJavaElement element, boolean isNew)
    throws Exception
  {
    IProject project = element.getJavaProject().getProject();
    String copyright = getPreferences().getValue(
      project, Preferences.PROJECT_COPYRIGHT_PREFERENCE);
    if(copyright != null && copyright.trim().length() > 0){
      File file = new File(ProjectUtils.getFilePath(project, copyright));
      if(!file.exists()){
        throw new IllegalArgumentException(
            Services.getMessage("project.copyright.not.found", file));
      }

      @SuppressWarnings("unchecked")
      List<TagElement> tags = javadoc.tags();
      tags.clear();

      BufferedReader reader = null;
      try{
        reader = new BufferedReader(new FileReader(file));
        String line = null;
        while ((line = reader.readLine()) != null){
          addTag(javadoc, tags.size(), null, line);
        }
      }finally{
        IOUtils.closeQuietly(reader);
      }
    }else{
      commentOther(src, javadoc, element, isNew);
    }
  }

  /**
   * Comment a type declaration.
   *
   * @param src The source file.
   * @param javadoc The Javadoc.
   * @param element The IJavaElement.
   * @param isNew true if there was no previous javadoc for this element.
   */
  private void commentType(
      ICompilationUnit src, Javadoc javadoc, IJavaElement element, boolean isNew)
    throws Exception
  {
    if(element.getParent().getElementType() == IJavaElement.COMPILATION_UNIT){
      @SuppressWarnings("unchecked")
      List<TagElement> tags = javadoc.tags();
      IProject project = element.getJavaProject().getProject();
      if(isNew){
        addTag(javadoc, tags.size(), null, null);
        addTag(javadoc, tags.size(), null, null);
        addTag(javadoc, tags.size(), TagElement.TAG_AUTHOR, getAuthor(project));
        String version = getPreferences().getValue(
            project, "org.eclim.java.doc.version");
        if(version != null && !StringUtils.EMPTY.equals(version.trim())){
          version = StringUtils.replace(version, "\\$", "$");
          addTag(javadoc, tags.size(), TagElement.TAG_VERSION, version);
        }
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
          TagElement authorTag = javadoc.getAST().newTagElement();
          TextElement authorText = javadoc.getAST().newTextElement();
          authorText.setText(author);
          authorTag.setTagName(TagElement.TAG_AUTHOR);

          @SuppressWarnings("unchecked")
          List<ASTNode> fragments = authorTag.fragments();
          fragments.add(authorText);
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
          String version = getPreferences().getValue(
              project, "org.eclim.java.doc.version");
          version = StringUtils.replace(version, "\\$", "$");
          addTag(javadoc, tags.size(), TagElement.TAG_VERSION, version);
        }
      }
    }else{
      commentOther(src, javadoc, element, isNew);
    }
  }

  /**
   * Comment a method declaration.
   *
   * @param src The source file.
   * @param javadoc The Javadoc.
   * @param element The IJavaElement.
   * @param isNew true if there was no previous javadoc for this element.
   */
  private void commentMethod(
      ICompilationUnit src, Javadoc javadoc, IJavaElement element, boolean isNew)
    throws Exception
  {
    @SuppressWarnings("unchecked")
    List<TagElement> tags = javadoc.tags();
    IMethod method = (IMethod)element;
    IType type = method.getDeclaringType();

    boolean hasOverride = false;
    IAnnotation[] annotations = method.getAnnotations();
    for (IAnnotation annotation : annotations){
      for (String[] result : type.resolveType(annotation.getElementName())){
        if (result[0].equals("java.lang") && result[1].equals("Override")){
          hasOverride = true;
          break;
        }
      }
    }

    if(isNew){
      // see if method is overriding / implementing method from superclass
      IType parentType = null;
      TypeInfo[] types = TypeUtils.getSuperTypes(type);
      for (TypeInfo info : types){
        if(MethodUtils.containsMethod(info, method)){
          parentType = info.getType();
          break;
        }
      }

      // if an inherited method, add inheritDoc and @see
      if(parentType != null){
        if (!hasOverride){
          addTag(javadoc, tags.size(), null, INHERIT_DOC);

          String typeName =
            JavaUtils.getCompilationUnitRelativeTypeName(src, parentType);

          StringBuffer signature = new StringBuffer();
          signature.append(typeName)
            .append('#').append(MethodUtils.getMinimalMethodSignature(method, null));
          addTag(javadoc, tags.size(), TagElement.TAG_SEE, signature.toString());
        }
      }else{
        addTag(javadoc, tags.size(), null, null);
        addTag(javadoc, tags.size(), null, null);
      }
    }

    // only add/update tags if javadoc doesn't contain inheritDoc.
    boolean update = true;
    for (TagElement tag : tags){
      if(tag.getTagName() == null && tag.fragments().size() > 0){
        if(INHERIT_DOC.equals(tag.fragments().get(0).toString())){
          update = false;
          break;
        }
      }
    }

    if(update){
      addUpdateParamTags(javadoc, method, isNew);
      addUpdateReturnTag(javadoc, method, isNew);
      addUpdateThrowsTags(javadoc, method, isNew);
    }
  }

  /**
   * Comment everything else.
   *
   * @param src The source file.
   * @param javadoc The Javadoc.
   * @param element The IJavaElement.
   * @param isNew true if there was no previous javadoc for this element.
   */
  private void commentOther(
      ICompilationUnit src, Javadoc javadoc, IJavaElement element, boolean isNew)
    throws Exception
  {
    if(isNew){
      addTag(javadoc, 0, null, null);
    }
  }

  /**
   * Add or update the param tags for the given method.
   *
   * @param javadoc The Javadoc instance.
   * @param method The method.
   * @param isNew true if we're adding to brand new javadocs.
   */
  private void addUpdateParamTags(
      Javadoc javadoc, IMethod method, boolean isNew)
    throws Exception
  {
    @SuppressWarnings("unchecked")
    List<TagElement> tags = javadoc.tags();
    String[] params = method.getParameterNames();
    if(isNew){
      for (int ii = 0; ii < params.length; ii++){
        addTag(javadoc, tags.size(), TagElement.TAG_PARAM, params[ii]);
      }
    }else{
      // find current params.
      int index = 0;
      HashMap<String, TagElement> current = new HashMap<String, TagElement>();
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
            addTag(javadoc, index + ii, TagElement.TAG_PARAM, params[ii]);
          }
        }

        // remove any other param tags.
        for (TagElement tag : current.values()){
          tags.remove(tag);
        }
      }else{
        for (int ii = 0; ii < params.length; ii++){
          addTag(javadoc, index + ii, TagElement.TAG_PARAM, params[ii]);
        }
      }
    }
  }

  /**
   * Add or update the return tag for the given method.
   *
   * @param javadoc The Javadoc instance.
   * @param method The method.
   * @param isNew true if we're adding to brand new javadocs.
   */
  private void addUpdateReturnTag(
      Javadoc javadoc, IMethod method, boolean isNew)
    throws Exception
  {
    @SuppressWarnings("unchecked")
    List<TagElement> tags = javadoc.tags();
    // get return type from element.
    if(!method.isConstructor()){
      String returnType =
        Signature.getSignatureSimpleName(method.getReturnType());
      if (!"void".equals(returnType)){
        if(isNew){
          addTag(javadoc, tags.size(), TagElement.TAG_RETURN, null);
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
            addTag(javadoc, index, TagElement.TAG_RETURN, null);
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
   * @param javadoc The Javadoc instance.
   * @param method The method.
   * @param isNew true if we're adding to brand new javadocs.
   */
  private void addUpdateThrowsTags(
      Javadoc javadoc, IMethod method, boolean isNew)
    throws Exception
  {
    @SuppressWarnings("unchecked")
    List<TagElement> tags = javadoc.tags();

    // get thrown exceptions from element.
    String[] exceptions = method.getExceptionTypes();
    if(isNew && exceptions.length > 0){
      addTag(javadoc, tags.size(), null, null);
      for (int ii = 0; ii < exceptions.length; ii++){
        addTag(javadoc, tags.size(), TagElement.TAG_THROWS,
            Signature.getSignatureSimpleName(exceptions[ii]));
      }
    }else{
      // get current throws tags
      HashMap<String, TagElement> current = new HashMap<String, TagElement>();
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
          addTag(javadoc, index, TagElement.TAG_THROWS, name);
        }else{
          current.remove(name);
        }
      }

      // remove any left over thows clauses.
      for (TagElement tag : current.values()){
        tags.remove(tag);
      }
    }
  }

  /**
   * Gets the author string.
   *
   * @return The author.
   */
  private String getAuthor(IProject project)
    throws Exception
  {
    String username = getPreferences().getValue(
        project.getProject(), Preferences.USERNAME_PREFERENCE);
    String email = getPreferences().getValue(
        project.getProject(), Preferences.USEREMAIL_PREFERENCE);

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
   * @param javadoc The Javadoc instance.
   * @param index The index to insert the new tag at.
   * @param name The tag name.
   * @param text The tag text.
   */
  private void addTag(
      Javadoc javadoc, int index, String name, String text)
    throws Exception
  {
    TagElement tag = javadoc.getAST().newTagElement();
    tag.setTagName(name);

    if(text != null){
      TextElement textElement = javadoc.getAST().newTextElement();
      textElement.setText(text);

      @SuppressWarnings("unchecked")
      List<ASTNode> fragments = tag.fragments();
      fragments.add(textElement);
    }

    @SuppressWarnings("unchecked")
    List<TagElement> tags = javadoc.tags();
    tags.add(index, tag);
  }
}
