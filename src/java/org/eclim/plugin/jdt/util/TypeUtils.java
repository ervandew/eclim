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
package org.eclim.plugin.jdt.util;

import java.util.ArrayList;
import java.util.List;

import org.eclim.logging.Logger;

import org.eclim.util.file.Position;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.Signature;

import org.eclipse.jdt.internal.core.CompilationUnit;

/**
 * Utility methods for working with IType.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class TypeUtils
{
  private static final Logger logger = Logger.getLogger(TypeUtils.class);

  /**
   * Gets the type at the supplied offset, which will either be the primary type
   * of the comilation unit, or an inner class.
   *
   * @param src The ICompilationSource.
   * @param offset The offet in the source file.
   * @return The IType.
   */
  public static IType getType(ICompilationUnit src, int offset)
    throws Exception
  {
    IJavaElement element = src.getElementAt(offset);
    IType type = null;

    // offset outside the class source (Above the package declaration most
    // likely)
    if(element == null){
      type = ((CompilationUnit)src).getTypeRoot().findPrimaryType();

    // inner class
    }else if(element != null && element.getElementType() == IJavaElement.TYPE){
      type = (IType)element;
    }else{
      element = element.getParent();

      // offset on import statement
      if(element.getElementType() == IJavaElement.IMPORT_CONTAINER){
        element = element.getParent();
      }

      // offset on the package declaration or continuation of import ^
      if(element.getElementType() == IJavaElement.COMPILATION_UNIT){
        element = ((CompilationUnit)element).getTypeRoot().findPrimaryType();
      }
      type = (IType)element;
    }
    return type;
  }

  /**
   * Gets the Position of the suplied ISourceReference.
   *
   * @param type The type.
   * @param reference The reference.
   * @return The position.
   */
  public static Position getPosition(IType type, ISourceReference reference)
    throws Exception
  {
    ISourceRange range = reference.getSourceRange();
    return new Position(
        type.getResource().getLocation().toOSString(),
        range.getOffset(), range.getLength());
  }

  /**
   * Gets the signature for the supplied type.
   *
   * @param type The type.
   * @return The signature.
   */
  public static String getTypeSignature(IType type)
    throws Exception
  {
    StringBuffer buffer = new StringBuffer();
    int flags = type.getFlags();
    if(Flags.isPublic(flags)){
      buffer.append("public ");
    }

    buffer.append(type.isClass() ? "class " : "interface ");
    IJavaElement parent = type.getParent();
    if(parent.getElementType() == IJavaElement.TYPE){
      buffer.append(type.getParent().getElementName()).append('.');
    }else if(parent.getElementType() == IJavaElement.CLASS_FILE){
      int index = parent.getElementName().indexOf('$');
      if(index != -1){
        buffer.append(parent.getElementName().substring(0, index)).append('.');
      }
    }
    buffer.append(type.getElementName());
    return buffer.toString();
  }

  /**
   * Determines if any of the super types of the supplied type contains the
   * supplied method and if so return that super type.
   *
   * @param type The type.
   * @param method The method.
   * @return The super type that contains the method, or null if none.
   */
  public static IType getSuperTypeContainingMethod(IType type, IMethod method)
    throws Exception
  {
    IType[] types = getSuperTypes(type);
    for(int ii = 0; ii < types.length; ii++){
      IMethod[] methods = types[ii].getMethods();
      for (int jj = 0; jj < methods.length; jj++){
        if(methods[jj].isSimilar(method)){
          return types[ii];
        }
      }
    }
    return null;
  }

  /**
   * Determines if any of the super types of the supplied type contains a method
   * with the supplied signature and if so return that super type.
   *
   * @param type The type.
   * @param signature The method signature.
   * @return The super type that contains the method, or null if none.
   */
  public static Object[] getSuperTypeContainingMethod(
      IType type, String signature)
    throws Exception
  {
    IType[] types = getSuperTypes(type);
    for(int ii = 0; ii < types.length; ii++){
      IMethod[] methods = types[ii].getMethods();
      for (int jj = 0; jj < methods.length; jj++){
        if(MethodUtils.getMinimalMethodSignature(methods[jj]).equals(signature)){
          return new Object[]{types[ii], methods[jj]};
        }
      }
    }
    return null;
  }

  /**
   * Converts the supplied type signature with generic information to the most
   * basic type that it supports.
   *
   * @param type The parent IType.
   * @param typeSignature The type signature.
   * @return The base type.
   */
  public static String getBaseTypeFromGeneric(
      IType type, String typeSignature)
    throws Exception
  {
    int arrayCount = Signature.getArrayCount(typeSignature);
    if(arrayCount > 0){
      for(int ii = 0; ii < arrayCount; ii++){
        typeSignature = Signature.getElementType(typeSignature);
      }
    }

    String result = null;
    ITypeParameter param = type.getTypeParameter(
        Signature.getSignatureSimpleName(typeSignature));
    if(param.exists()){
      result = param.getBounds()[0];
    }else{
      result =  Signature.getSignatureSimpleName(
          Signature.getTypeErasure(typeSignature));
    }

    if(arrayCount > 0){
      for(int ii = 0; ii < arrayCount; ii++){
        result = result + "[]";
      }
    }
    return result;
  }

  /**
   * Attempts to find an unqualified type by searching the import declarations
   * of the supplied source file and attempting to find the type in the same
   * package as the source file..
   *
   * @param src The source file.
   * @param typeName The unqualified type name.
   * @return The type or null if not found.
   */
  public static IType findUnqualifiedType(
      ICompilationUnit src, String typeName)
    throws Exception
  {
    String name = "." + typeName;

    // strip of generic type if present.
    int index = name.indexOf('<');
    if(index != -1){
      name = name.substring(0, index);
    }

    // search imports
    IImportDeclaration[] imports = src.getImports();
    for(int ii = 0; ii < imports.length; ii++){
      if(imports[ii].getElementName().endsWith(name)){
        return src.getJavaProject().findType(imports[ii].getElementName());
      }
    }

    // attempt to find in current package.
    IPackageDeclaration[] packages = src.getPackageDeclarations();
    if(packages != null && packages.length > 0){
      name = packages[0].getElementName() + name;
    }else{
      name = typeName;
    }
    IType type = src.getJavaProject().findType(name);

    // last effort, search java.lang
    if(type == null){
      type = src.getJavaProject().findType("java.lang." + typeName);
    }

    return type;
  }

  /**
   * Recursively gets all superclass and implemented interfaces from the
   * supplied type.
   *
   * @param type The type.
   * @return Array of types.
   */
  public static IType[] getSuperTypes(IType type)
    throws Exception
  {
    return getSuperTypes(type, false);
  }

  /**
   * Recursively gets all superclass and implemented interfaces from the
   * supplied type.
   *
   * @param type The type.
   * @param returnNotFound Whether or not to return handle only instances to
   *  super types that could not be found in the project.
   * @return Array of types.
   */
  public static IType[] getSuperTypes(IType type, boolean returnNotFound)
    throws Exception
  {
    IType[] interfaces = getInterfaces(type, returnNotFound);
    IType[] superClasses = getSuperClasses(type, returnNotFound);
    IType[] types = new IType[interfaces.length + superClasses.length];

    System.arraycopy(interfaces, 0, types, 0, interfaces.length);
    System.arraycopy(
        superClasses, 0, types, interfaces.length, superClasses.length);

    return types;
  }

  /**
   * Recursively gets all the superclasses for the given type.
   *
   * @param type The type.
   * @return Array of superclass types.
   */
  public static IType[] getSuperClasses(IType type)
    throws Exception
  {
    return getSuperClasses(type, false);
  }

  /**
   * Recursively gets all the superclasses for the given type.
   *
   * @param type The type.
   * @param returnNotFound Whether or not to return handle only instances to
   *  super class types that could not be found in the project.
   * @return Array of superclass types.
   */
  public static IType[] getSuperClasses(IType type, boolean returnNotFound)
    throws Exception
  {
    ArrayList<IType> types = new ArrayList<IType>();

    getSuperClasses(type, types, returnNotFound);

    // add java.lang.Object if not already added
    IType objectType = type.getJavaProject().findType("java.lang.Object");
    if(!types.contains(objectType)){
      types.add(objectType);
    }

    return (IType[])types.toArray(new IType[types.size()]);
  }

  /**
   * Recursively gets all the implemented interfaces for the given type.
   *
   * @param type The type.
   * @return Array of interface types.
   */
  public static IType[] getInterfaces(IType type)
    throws Exception
  {
    return getInterfaces(type, false);
  }

  /**
   * Recursively gets all the implemented interfaces for the given type.
   *
   * @param type The type.
   * @param returnNotFound Whether or not to return handle only instances to
   *  super interface types that could not be found in the project.
   * @return Array of interface types.
   */
  public static IType[] getInterfaces(IType type, boolean returnNotFound)
    throws Exception
  {
    ArrayList<IType> types = new ArrayList<IType>();
    getInterfaces(type, types, returnNotFound);

    return (IType[])types.toArray(new IType[types.size()]);
  }

  /**
   * Recursively gets all the superclasses for the given type.
   *
   * @param type The type.
   * @param superclasses The list to add results to.
   * @param includeNotFound Whether or not to include types that were not found
   * (adds them as handle only IType instances).
   */
  private static void getSuperClasses(
      IType type, List<IType> superclasses, boolean includeNotFound)
    throws Exception
  {
    IType superclass = getSuperClass(type);
    if(superclass != null && !superclasses.contains(superclass)){
      superclasses.add(superclass);
      getSuperClasses(superclass, superclasses, includeNotFound);
    }else if(superclass == null && includeNotFound){
      String typeName = type.getSuperclassName();
      if(typeName != null){
        // get a handle only reference to the super class that wasn't found.
        try{
          superclass = type.getType(typeName);
          if(!superclasses.contains(superclass)){
            superclasses.add(superclass);
          }
        }catch(Exception e){
          // don't let the error cause the command to fail.
          logger.warn("Unable to get a handle to class not found: '" +
              typeName + "'", e);
        }
      }
    }
  }

  /**
   * Gets the super type of the supplied type, if any.
   *
   * @param type The type to get the superclass of.
   * @return The superclass type or null if none.
   */
  public static IType getSuperClass(IType type)
    throws Exception
  {
    String superclass = type.getSuperclassName();
    if(superclass != null){
      String[][] types = type.resolveType(superclass);
      if(types != null){
        for(int ii = 0; ii < types.length; ii++){
          String typeName = types[ii][0] + "." + types[ii][1];
          IType found = type.getJavaProject().findType(typeName);
          return found;
        }
      }else{
        IType found = type.getJavaProject().findType(superclass);
        return found;
      }
    }
    return null;
  }

  /**
   * Gets an array of directly implemented interfaces for the supplied type, if
   * any.
   *
   * @param type The type to get the interfaces of.
   * @return Array of interface types.
   */
  public static IType[] getSuperInterfaces(IType type)
    throws Exception
  {
    String[] parents = type.getSuperInterfaceNames();
    ArrayList<IType> interfaces = new ArrayList<IType>(parents.length);
    for(int ii = 0; ii < parents.length; ii++){
      String[][] types = type.resolveType(parents[ii]);
      if(types != null){
        for(int jj = 0; jj < types.length; jj++){
          String typeName = types[jj][0] + "." + types[jj][1];
          IType found = type.getJavaProject().findType(typeName);
          if(found != null){
            interfaces.add(found);
          }
        }
      }else{
        IType found = type.getJavaProject().findType(parents[ii]);
        if(found != null){
          interfaces.add(found);
        }
      }
    }
    return interfaces.toArray(new IType[interfaces.size()]);
  }

  /**
   * Recursively gets the interfaces implemented by the given type.
   *
   * @param type The type.
   * @param interfaces The list to add results to.
   * @param includeNotFound Whether or not to include types that were not found
   *  (adds them as handle only IType instances).
   */
  private static void getInterfaces(
      IType type, List<IType> interfaces, boolean includeNotFound)
    throws Exception
  {
    // directly implemented interfaces.
    String[] parents = type.getSuperInterfaceNames();
    for(int ii = 0; ii < parents.length; ii++){
      String[][] types = type.resolveType(parents[ii]);
      if(types != null){
        for(int jj = 0; jj < types.length; jj++){
          String typeName = types[jj][0] + "." + types[jj][1];
          IType found = type.getJavaProject().findType(typeName);
          if(found != null && !interfaces.contains(found)){
            interfaces.add(found);
            getInterfaces(found, interfaces, includeNotFound);
          }
        }
      }else{
        IType found = type.getJavaProject().findType(parents[ii]);
        if(found != null && !interfaces.contains(found)){
          interfaces.add(found);
          getInterfaces(found, interfaces, includeNotFound);
        }else if(found == null && includeNotFound){
          String typeName = parents[ii];
          if(typeName != null){
            // get a handle only reference to the super class that wasn't found.
            try{
              found = type.getType(typeName);
              if(!interfaces.contains(found)){
                interfaces.add(found);
              }
            }catch(Exception e){
              // don't let the error cause the command to fail.
              logger.warn("Unable to get a handle to interface not found: '" +
                  typeName + "'", e);
            }
          }
        }else if(found == null){
          logger.warn("Unable to resolve implmented interface '{}' for '{}'",
              parents[ii], type.getFullyQualifiedName());
        }
      }
    }

    // indirectly implemented parents
    IType superclass = getSuperClass(type);
    if(superclass != null){
      getInterfaces(superclass, interfaces, includeNotFound);
    }
  }
}
