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
package org.eclim.plugin.jdt.command.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.eclim.command.CommandLine;
import org.eclim.command.OutputFilter;

/**
 * Output filter for impl results.
 *
 * @author Eric Van Dewoestine
 */
public class ImplFilter
  implements OutputFilter<ImplResult>
{
  public static final ImplFilter instance = new ImplFilter();

  private static final String NOT_FOUND_HEADER =
    "// The following types were not found, either because they were not\n" +
    "// imported or they were not found in the classpath.";

  /**
   * {@inheritDoc}
   */
  public String filter(CommandLine commandLine, ImplResult result)
  {
    if(result != null){
      StringBuffer buffer = new StringBuffer();
      buffer.append(result.getType());

      List<ImplType> results = result.getSuperTypes();
      if(results != null){
        ArrayList<ImplType> notFound = new ArrayList<ImplType>();
        for(ImplType type : results){
          if(!type.getExists()){
            notFound.add(type);
          }

          if(type.getMethods() == null || type.getMethods().length == 0){
            continue;
          }

          if(buffer.length() > 0){
            buffer.append("\n\n");
          }

          buffer.append("package ").append(type.getPackage()).append(";\n");
          buffer.append(type.getSignature()).append(" {\n");
          ImplMethod[] methods = type.getMethods();
          if(methods != null){
            for(int jj = 0; jj < methods.length; jj++){
              if(jj > 0){
                buffer.append('\n');
              }
              String signature = methods[jj].getSignature();
              if(methods[jj].isImplemented()){
                signature = "//" + StringUtils.replace(signature, "\t", "\t//");
              }
              signature = StringUtils.replace(signature, "\n", "\n\t");
              buffer.append('\t').append(signature);
            }
          }
          buffer.append("\n}");
        }

        // print out those types that were not found.
        if(notFound.size() > 0){
          buffer.append("\n\n");
          buffer.append(NOT_FOUND_HEADER);
          for(ImplType type : notFound){
            buffer.append("\n\t// ").append(type.getSignature());
          }
        }
      }
      return buffer.toString();
    }
    return StringUtils.EMPTY;
  }
}
