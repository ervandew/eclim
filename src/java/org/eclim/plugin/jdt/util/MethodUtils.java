/**
 * Copyright (c) 2005 - 2008
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

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;

/**
 * Utility methods for working with IMethod elements.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class MethodUtils
{
  private static final String VARARGS = "...";
  private static final String TYPE_REGEX = "\\bQ(.*?);";

  /**
   * Determines if the supplied types contains the specified method.
   *
   * @param _type The type.
   * @param _method The method.
   * @return true if the type contains the method, false otherwise.
   */
  public static boolean containsMethod (IType _type, IMethod _method)
    throws Exception
  {
    /*IMethod[] methods = _type.getMethods();
    for(int ii = 0; ii < methods.length; ii++){
      if(methods[ii].isSimilar(_method)){
        return true;
      }
    }
    return false;*/

    String signature = getMinimalMethodSignature(_method);
    if(_method.isConstructor()){
      signature = signature.replaceFirst(
          _method.getDeclaringType().getElementName(), _type.getElementName());
    }
    IMethod[] methods = _type.getMethods();
    for (int ii = 0; ii < methods.length; ii++){
      String methodSig = getMinimalMethodSignature(methods[ii]);
      if(methodSig.equals(signature)){
        return true;
      }
    }
    return false;
  }

  /**
   * Gets the method from the supplied type that matches the signature of the
   * specified method.
   *
   * @param _type The type.
   * @param _method The method.
   * @return The method or null if none found.
   */
  public static IMethod getMethod (IType _type, IMethod _method)
    throws Exception
  {
    String signature = getMinimalMethodSignature(_method);
    if(_method.isConstructor()){
      signature = signature.replaceFirst(
          _method.getDeclaringType().getElementName(), _type.getElementName());
    }
    IMethod[] methods = _type.getMethods();
    for (int ii = 0; ii < methods.length; ii++){
      String methodSig = getMinimalMethodSignature(methods[ii]);
      if(methodSig.equals(signature)){
        return methods[ii];
      }
    }
    return null;
  }

  /**
   * Retrieves the method which follows the supplied method in the specified
   * type.
   *
   * @param _type The type.
   * @param _method The method.
   * @return The method declared after the supplied method.
   */
  public static IMethod getMethodAfter (IType _type, IMethod _method)
    throws Exception
  {
    if(_type == null || _method == null){
      return null;
    }

    // get the method after the sibling.
    IMethod[] all = _type.getMethods();
    for (int ii = 0; ii < all.length; ii++){
      if(all[ii].equals(_method) && ii < all.length - 1){
        return all[ii + 1];
      }
    }
    return null;
  }

  /**
   * Gets a String representation of the supplied method's signature.
   *
   * @param _method The method.
   * @return The signature.
   */
  public static String getMethodSignature (IMethod _method)
    throws Exception
  {
    int flags = _method.getFlags();
    StringBuffer buffer = new StringBuffer();
    if(_method.getDeclaringType().isInterface()){
      buffer.append("public ");
    }else{
      buffer.append(
          Flags.isPublic(_method.getFlags()) ? "public " : "protected ");
    }
    buffer.append(Flags.isAbstract(flags) ? "abstract " : "");
    if(!_method.isConstructor()){
      buffer.append(Signature.getSignatureSimpleName(_method.getReturnType()))
      .append(' ');
    }
    buffer.append(_method.getElementName())
      .append(" (")
      .append(getMethodParameters(_method, true))
      .append(')');

    String[] exceptions = _method.getExceptionTypes();
    if(exceptions.length > 0){
      buffer.append("\n\tthrows ").append(getMethodThrows(_method));
    }
    return buffer.toString();
  }

  /**
   * Gets just enough of a method's signature that it can be distiguished from
   * the other methods.
   *
   * @param _method The method.
   * @return The signature.
   */
  public static String getMinimalMethodSignature (IMethod _method)
    throws Exception
  {
    StringBuffer buffer = new StringBuffer();
    buffer.append(_method.getElementName())
      .append('(')
      .append(getMethodParameters(_method, false))
      .append(')');

    return buffer.toString();
  }

  /**
   * Gets the supplied method's parameter types and optoinally names, in a comma
   * separated string.
   *
   * @param _method The method.
   * @param _includeNames true to include the paramter names in the string.
   * @return The parameters as a string.
   */
  public static String getMethodParameters (IMethod _method, boolean _includeNames)
    throws Exception
  {
    StringBuffer buffer = new StringBuffer();
    String[] paramTypes = _method.getParameterTypes();
    String[] paramNames = null;
    if(_includeNames){
      paramNames = _method.getParameterNames();
    }
    boolean varargs = false;
    for(int ii = 0; ii < paramTypes.length; ii++){
      if(ii != 0){
        buffer.append(_includeNames ? ", " : ",");
      }

      String type = paramTypes[ii];

      // check for varargs
      if (ii == paramTypes.length - 1 &&
          Signature.getTypeSignatureKind(type) == Signature.ARRAY_TYPE_SIGNATURE &&
          Flags.isVarargs(_method.getFlags()))
      {
        type = Signature.getElementType(paramTypes[ii]);
        varargs = true;
      }

      // check for unresolved types first.
      if(type.startsWith("Q")){
        type = type.replaceAll(TYPE_REGEX, "$1");
      }else{
        type = Signature.getSignatureSimpleName(type);
      }

      int genericStart = type.indexOf("<");
      if(genericStart != -1){
        type = type.substring(0, genericStart);
      }
      buffer.append(type);
      if(varargs){
        buffer.append(VARARGS);
      }

      if(_includeNames){
        buffer.append(' ').append(paramNames[ii]);
      }
    }
    return buffer.toString();
  }

  /**
   * Gets the list of thrown exceptions as a comma separated string.
   *
   * @param _method The method.
   * @return The thrown exceptions or null if none.
   */
  public static String getMethodThrows (IMethod _method)
    throws Exception
  {
    String[] exceptions = _method.getExceptionTypes();
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
    return null;
  }
}
