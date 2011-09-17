/**
 * Copyright (C) 2005 - 2011  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.constructor;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.TemplateUtils;

import org.eclim.plugin.jdt.PluginResources;

import org.eclim.plugin.jdt.util.JavaUtils;
import org.eclim.plugin.jdt.util.TypeUtils;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;

/**
 * Command used to generate class constructors.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_constructor",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED o offset ARG," +
    "OPTIONAL e encoding ARG," +
    "OPTIONAL r properties ARG"
)
public class ConstructorCommand
  extends AbstractCommand
{
  private static final String TEMPLATE = "constructor.gst";

  /**
   * {@inheritDoc}
   */
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    String propertiesOption = commandLine.getValue(Options.PROPERTIES_OPTION);
    String[] properties = {};
    if(propertiesOption != null){
      properties = StringUtils.split(propertiesOption, ',');
    }
    int offset = getOffset(commandLine);

    // validate supplied fields.
    ICompilationUnit src = JavaUtils.getCompilationUnit(project, file);
    IType type = TypeUtils.getType(src, offset);
    for(int ii = 0; ii < properties.length; ii++){
      if(!type.getField(properties[ii]).exists()){
        throw new RuntimeException(
            Services.getMessage(
              "field.not.found", properties[ii], type.getElementName()));
      }
    }

    // check if constructor already exists.
    IMethod method = null;
    if(properties.length == 0){
      method = type.getMethod(type.getElementName(), null);
    }else{
      String[] fieldSigs = new String[properties.length];
      for (int ii = 0; ii < properties.length; ii++){
        fieldSigs[ii] = type.getField(properties[ii]).getTypeSignature();
      }
      method = type.getMethod(type.getElementName(), fieldSigs);
    }
    if(method.exists()){
      throw new RuntimeException(
          Services.getMessage("constructor.already.exists",
            type.getElementName() + " (" + buildParams(type, properties) + ")"));
    }

    // find the sibling to insert before.
    IJavaElement sibling = null;
    IMethod[] methods = type.getMethods();
    for(int ii = 0; ii < methods.length; ii++){
      if(methods[ii].isConstructor()){
        sibling = ii < methods.length - 1 ? methods[ii + 1] : null;
      }
    }
    // insert before any other methods or inner classes if any.
    if(sibling == null){
      if(methods.length > 0){
        sibling = methods[0];
      }else{
        IType[] types = type.getTypes();
        sibling = types != null && types.length > 0 ? types[0] : null;
      }
    }

    HashMap<String, Object> values = new HashMap<String, Object>();
    values.put("type", type.getElementName());
    boolean hasProperties = properties != null && properties.length > 0;
    values.put("fields", hasProperties ? properties : null);
    values.put("params",
        hasProperties ? buildParams(type, properties) : StringUtils.EMPTY);

    PluginResources resources = (PluginResources)
      Services.getPluginResources(PluginResources.NAME);
    String constructor = TemplateUtils.evaluate(resources, TEMPLATE, values);
    TypeUtils.getPosition(
        type, type.createMethod(constructor, sibling, false, null));

    return null;
  }

  /**
   * Builds a string of the constructor parameters.
   *
   * @param type The type containing the fields.
   * @param fields The array of fields.
   * @return The parameters string.
   */
  protected String buildParams(IType type, String[] fields)
    throws Exception
  {
    StringBuffer params = new StringBuffer();
    for (int ii = 0; ii < fields.length; ii++){
      if(ii != 0){
        params.append(", ");
      }
      IField field = type.getField(fields[ii]);
      params.append(Signature.getSignatureSimpleName(field.getTypeSignature()))
        .append(' ').append(field.getElementName());
    }
    return params.toString();
  }
}
