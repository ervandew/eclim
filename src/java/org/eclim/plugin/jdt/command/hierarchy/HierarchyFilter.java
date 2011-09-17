/**
 * Copyright (C) 2005 - 2009  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.hierarchy;

import java.io.StringWriter;
import java.util.List;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.eclim.command.CommandLine;
import org.eclim.command.OutputFilter;
import org.eclim.plugin.jdt.util.JavaUtils;
import org.eclim.plugin.jdt.util.TypeInfo;
import org.eclim.plugin.jdt.util.TypeUtils;
import org.eclipse.jdt.core.IType;

/**
 * Filter for outputting a type hierarchy.
 *
 * @author Eric Van Dewoestine
 */
public class HierarchyFilter
  implements OutputFilter<HierarchyNode>
{
  public static final HierarchyFilter instance = new HierarchyFilter();

  /**
   * {@inheritDoc}
   * @see OutputFilter#filter(CommandLine,Object)
   */
  public String filter(CommandLine commandLine, HierarchyNode hierarchy)
  {
    StringWriter writer = new StringWriter();
    JsonFactory jsonFactory = new JsonFactory();
    try{
      JsonGenerator out = jsonFactory.createJsonGenerator(writer);
      toString(hierarchy,out);
      out.close();
    }catch(Exception e){
      throw new RuntimeException(e);
    }
    return writer.toString();
  }

  public void toString(HierarchyNode hierarchy, JsonGenerator out)
    throws Exception
  {
    IType type = hierarchy.getType();
    TypeInfo info = new TypeInfo(type, null, null);
    out.writeStartObject();
    out.writeStringField("name", TypeUtils.getTypeSignature(info));
    out.writeStringField("qualified", JavaUtils.getFullyQualifiedName(type));
    out.writeArrayFieldStart("children");
    List<HierarchyNode> children = hierarchy.getChildren();
    for (int ii = 0; ii < children.size(); ii++) {
      toString(children.get(ii), out);
    }
    out.writeEndArray();
    out.writeEndObject();
  }
}
