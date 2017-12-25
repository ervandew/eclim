/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.util;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;

/**
 * Utility methods for working with IMethod elements.
 *
 * @author Eric Van Dewoestine
 */
public class MethodUtils
{
  private static final String VARARGS = "...";

  /**
   * Determines if the supplied types contains the specified method.
   *
   * @param typeInfo The type info.
   * @param method The method.
   * @return true if the type contains the method, false otherwise.
   */
  public static boolean containsMethod(TypeInfo typeInfo, IMethod method)
  {
    /*IMethod[] methods = type.getMethods();
    for(int ii = 0; ii < methods.length; ii++){
      if(methods[ii].isSimilar(method)){
        return true;
      }
    }
    return false;*/

    try{
      IType type = typeInfo.getType();
      String signature = getMinimalMethodSignature(method, typeInfo);
      if(method.isConstructor()){
        signature = signature.replaceFirst(
            method.getDeclaringType().getElementName(), type.getElementName());
      }
      IMethod[] methods = type.getMethods();
      for (int ii = 0; ii < methods.length; ii++){
        String methodSig = getMinimalMethodSignature(methods[ii], typeInfo);
        if(methodSig.equals(signature)){
          return true;
        }
      }
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
    return false;
  }

  /**
   * Gets the method from the supplied type that matches the signature of the
   * specified method.
   *
   * @param typeInfo The type info.
   * @param method The method.
   * @return The method or null if none found.
   */
  public static IMethod getMethod(TypeInfo typeInfo, IMethod method)
  {
    try{
      IType type = typeInfo.getType();
      String signature = getMinimalMethodSignature(method, typeInfo);
      if(method.isConstructor()){
        signature = signature.replaceFirst(
            method.getDeclaringType().getElementName(), type.getElementName());
      }
      IMethod[] methods = type.getMethods();
      for (int ii = 0; ii < methods.length; ii++){
        String methodSig = getMinimalMethodSignature(methods[ii], typeInfo);
        if(methodSig.equals(signature)){
          return methods[ii];
        }
      }
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
    return null;
  }

  /**
   * Retrieves the method which follows the supplied method in the specified
   * type.
   *
   * @param type The type.
   * @param method The method.
   * @return The method declared after the supplied method.
   */
  public static IMethod getMethodAfter(IType type, IMethod method)
  {
    if(type == null || method == null){
      return null;
    }

    try{
      // get the method after the sibling.
      IMethod[] all = type.getMethods();
      for (int ii = 0; ii < all.length; ii++){
        if(all[ii].equals(method) && ii < all.length - 1){
          return all[ii + 1];
        }
      }
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
    return null;
  }

  /**
   * Gets a String representation of the supplied method's signature.
   *
   * @param method The method.
   * @param typeInfo The type info.
   * @return The signature.
   */
  public static String getMethodSignature(IMethod method, TypeInfo typeInfo)
  {
    StringBuffer buffer = new StringBuffer();
    try{
      int flags = method.getFlags();
      if(method.getDeclaringType().isInterface()){
        buffer.append("public ");
      }else{
        buffer.append(
            Flags.isPublic(method.getFlags()) ? "public " : "protected ");
      }
      buffer.append(Flags.isAbstract(flags) ? "abstract " : "");
      if(!method.isConstructor()){
        String name = Signature.getSignatureSimpleName(method.getReturnType());
        buffer.append(TypeUtils.replaceTypeParams(name, typeInfo)).append(' ');
      }
      buffer.append(method.getElementName())
        .append("(")
        .append(getMethodParameters(method, typeInfo, true))
        .append(')');

      String[] exceptions = method.getExceptionTypes();
      if(exceptions.length > 0){
        buffer.append("\n\tthrows ").append(getMethodThrows(method));
      }
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
    return buffer.toString();
  }

  /**
   * Gets just enough of a method's signature that it can be distiguished from
   * the other methods.
   *
   * @param method The IMethod.
   * @param typeInfo The TypeInfo.
   * @return The signature.
   */
  public static String getMinimalMethodSignature(
      IMethod method, TypeInfo typeInfo)
  {
    StringBuffer buffer = new StringBuffer();
    buffer.append(method.getElementName())
      .append('(')
      .append(getMethodParameters(method, typeInfo, false))
      .append(')');

    return buffer.toString();
  }

  /**
   * Gets the supplied method's parameter types and optoinally names, in a comma
   * separated string.
   *
   * @param method The method.
   * @param typeInfo The type info.
   * @param includeNames true to include the paramter names in the string.
   * @return The parameters as a string.
   */
  public static String getMethodParameters(
      IMethod method, TypeInfo typeInfo, boolean includeNames)
  {
    StringBuffer buffer = new StringBuffer();
    try{
      String[] paramTypes = method.getParameterTypes();
      String[] paramNames = null;
      if(includeNames){
        paramNames = method.getParameterNames();
      }
      boolean varargs = false;
      for(int ii = 0; ii < paramTypes.length; ii++){
        if(ii != 0){
          buffer.append(includeNames ? ", " : ",");
        }

        String type = paramTypes[ii];

        // check for varargs
        if (ii == paramTypes.length - 1 &&
            Signature.getTypeSignatureKind(type) == Signature.ARRAY_TYPE_SIGNATURE &&
            Flags.isVarargs(method.getFlags()))
        {
          type = Signature.getElementType(paramTypes[ii]);
          varargs = true;
        }

        type = Signature.getSignatureSimpleName(type);
        type = type.replaceAll("\\?\\s+extends\\s+", "");
        type = TypeUtils.replaceTypeParams(type, typeInfo);

        buffer.append(type);
        if(varargs){
          buffer.append(VARARGS);
        }

        if(includeNames){
          buffer.append(' ').append(paramNames[ii]);
        }
      }
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
    return buffer.toString();
  }

  /**
   * Gets the list of thrown exceptions as a comma separated string.
   *
   * @param method The method.
   * @return The thrown exceptions or null if none.
   */
  public static String getMethodThrows(IMethod method)
  {
    try{
      String[] exceptions = method.getExceptionTypes();
      if(exceptions.length > 0){
        StringBuffer buffer = new StringBuffer();
        for(int ii = 0; ii < exceptions.length; ii++){
          if(ii != 0){
            buffer.append(", ");
          }
          buffer.append(Signature.getSignatureSimpleName(exceptions[ii]));
        }
        return buffer.toString();
      }
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
    return null;
  }
}
