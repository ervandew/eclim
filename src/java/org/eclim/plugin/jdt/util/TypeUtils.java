/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
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
   * @param _src The ICompilationSource.
   * @param _offset The offet in the source file.
   * @return The IType.
   */
  public static IType getType (ICompilationUnit _src, int _offset)
    throws Exception
  {
    IJavaElement element = _src.getElementAt(_offset);
    IType type = null;

    // offset outside the class source (Above the package declaration most
    // likely)
    if(element == null){
      type = ((CompilationUnit)_src).getTypeRoot().findPrimaryType();

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
   * @param _type The type.
   * @param _reference The reference.
   * @return The position.
   */
  public static Position getPosition (IType _type, ISourceReference _reference)
    throws Exception
  {
    ISourceRange range = _reference.getSourceRange();
    return new Position(
        _type.getResource().getLocation().toOSString(),
        range.getOffset(), range.getLength());
  }

  /**
   * Gets the signature for the supplied type.
   *
   * @param _type The type.
   * @return The signature.
   */
  public static String getTypeSignature (IType _type)
    throws Exception
  {
    StringBuffer buffer = new StringBuffer();
    int flags = _type.getFlags();
    if(Flags.isPublic(flags)){
      buffer.append("public ");
    }

    buffer.append(_type.isClass() ? "class " : "interface ");
    IJavaElement parent = _type.getParent();
    if(parent.getElementType() == IJavaElement.TYPE){
      buffer.append(_type.getParent().getElementName()).append('.');
    }else if(parent.getElementType() == IJavaElement.CLASS_FILE){
      int index = parent.getElementName().indexOf('$');
      if(index != -1){
        buffer.append(parent.getElementName().substring(0, index)).append('.');
      }
    }
    buffer.append(_type.getElementName());
    return buffer.toString();
  }

  /**
   * Determines if any of the super types of the supplied type contains the
   * supplied method and if so return that super type.
   *
   * @param _type The type.
   * @param _method The method.
   * @return The super type that contains the method, or null if none.
   */
  public static IType getSuperTypeContainingMethod (
      IType _type, IMethod _method)
    throws Exception
  {
    IType[] types = getSuperTypes(_type);
    for(int ii = 0; ii < types.length; ii++){
      IMethod[] methods = types[ii].getMethods();
      for (int jj = 0; jj < methods.length; jj++){
        if(methods[jj].isSimilar(_method)){
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
   * @param _type The type.
   * @param _signature The method signature.
   * @return The super type that contains the method, or null if none.
   */
  public static Object[] getSuperTypeContainingMethod (
      IType _type, String _signature)
    throws Exception
  {
    IType[] types = getSuperTypes(_type);
    for(int ii = 0; ii < types.length; ii++){
      IMethod[] methods = types[ii].getMethods();
      for (int jj = 0; jj < methods.length; jj++){
        if(MethodUtils.getMinimalMethodSignature(methods[jj]).equals(_signature)){
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
   * @param _type The parent IType.
   * @param _typeSignature The type signature.
   * @return The base type.
   */
  public static String getBaseTypeFromGeneric (
      IType _type, String _typeSignature)
    throws Exception
  {
    int arrayCount = Signature.getArrayCount(_typeSignature);
    if(arrayCount > 0){
      for(int ii = 0; ii < arrayCount; ii++){
        _typeSignature = Signature.getElementType(_typeSignature);
      }
    }

    String result = null;
    ITypeParameter param = _type.getTypeParameter(
        Signature.getSignatureSimpleName(_typeSignature));
    if(param.exists()){
      result = param.getBounds()[0];
    }else{
      result =  Signature.getSignatureSimpleName(
          Signature.getTypeErasure(_typeSignature));
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
   * @param _src The source file.
   * @param _typeName The unqualified type name.
   * @return The type or null if not found.
   */
  public static IType findUnqualifiedType (
      ICompilationUnit _src, String _typeName)
    throws Exception
  {
    String typeName = "." + _typeName;

    // strip of generic type if present.
    int index = typeName.indexOf('<');
    if(index != -1){
      typeName = typeName.substring(0, index);
    }

    // search imports
    IImportDeclaration[] imports = _src.getImports();
    for(int ii = 0; ii < imports.length; ii++){
      if(imports[ii].getElementName().endsWith(typeName)){
        return _src.getJavaProject().findType(imports[ii].getElementName());
      }
    }

    // attempt to find in current package.
    IPackageDeclaration[] packages = _src.getPackageDeclarations();
    if(packages != null && packages.length > 0){
      typeName = packages[0].getElementName() + typeName;
    }else{
      typeName = _typeName;
    }
    IType type = _src.getJavaProject().findType(typeName);

    // last effort, search java.lang
    if(type == null){
      type = _src.getJavaProject().findType("java.lang." + _typeName);
    }

    return type;
  }

  /**
   * Recursively gets all superclass and implemented interfaces from the
   * supplied type.
   *
   * @param _type The type.
   * @return Array of types.
   */
  public static IType[] getSuperTypes (IType _type)
    throws Exception
  {
    return getSuperTypes(_type, false);
  }

  /**
   * Recursively gets all superclass and implemented interfaces from the
   * supplied type.
   *
   * @param _type The type.
   * @param _returnNotFound Whether or not to return handle only instances to
   *  super types that could not be found in the project.
   * @return Array of types.
   */
  public static IType[] getSuperTypes (IType _type, boolean _returnNotFound)
    throws Exception
  {
    IType[] interfaces = getInterfaces(_type, _returnNotFound);
    IType[] superClasses = getSuperClasses(_type, _returnNotFound);
    IType[] types = new IType[interfaces.length + superClasses.length];

    System.arraycopy(interfaces, 0, types, 0, interfaces.length);
    System.arraycopy(
        superClasses, 0, types, interfaces.length, superClasses.length);

    return types;
  }

  /**
   * Recursively gets all the superclasses for the given type.
   *
   * @param _type The type.
   * @return Array of superclass types.
   */
  public static IType[] getSuperClasses (IType _type)
    throws Exception
  {
    return getSuperClasses(_type, false);
  }

  /**
   * Recursively gets all the superclasses for the given type.
   *
   * @param _type The type.
   * @param _returnNotFound Whether or not to return handle only instances to
   *  super class types that could not be found in the project.
   * @return Array of superclass types.
   */
  public static IType[] getSuperClasses (IType _type, boolean _returnNotFound)
    throws Exception
  {
    ArrayList<IType> types = new ArrayList<IType>();

    getSuperClasses(_type, types, _returnNotFound);

    // add java.lang.Object if not already added
    IType objectType = _type.getJavaProject().findType("java.lang.Object");
    if(!types.contains(objectType)){
      types.add(objectType);
    }

    return (IType[])types.toArray(new IType[types.size()]);
  }

  /**
   * Recursively gets all the implemented interfaces for the given type.
   *
   * @param _type The type.
   * @return Array of interface types.
   */
  public static IType[] getInterfaces (IType _type)
    throws Exception
  {
    return getInterfaces(_type, false);
  }

  /**
   * Recursively gets all the implemented interfaces for the given type.
   *
   * @param _type The type.
   * @param _returnNotFound Whether or not to return handle only instances to
   *  super interface types that could not be found in the project.
   * @return Array of interface types.
   */
  public static IType[] getInterfaces (IType _type, boolean _returnNotFound)
    throws Exception
  {
    ArrayList<IType> types = new ArrayList<IType>();
    getInterfaces(_type, types, _returnNotFound);

    return (IType[])types.toArray(new IType[types.size()]);
  }

  /**
   * Recursively gets all the superclasses for the given type.
   *
   * @param _type The type.
   * @param _superclasses The list to add results to.
   * @param _includeNotFound Whether or not to include types that were not found
   * (adds them as handle only IType instances).
   */
  private static void getSuperClasses (
      IType _type, List<IType> _superclasses, boolean _includeNotFound)
    throws Exception
  {
    IType superclass = getSuperClass(_type);
    if(superclass != null && !_superclasses.contains(superclass)){
      _superclasses.add(superclass);
      getSuperClasses(superclass, _superclasses, _includeNotFound);
    }else if(superclass == null && _includeNotFound){
      String typeName = _type.getSuperclassName();
      if(typeName != null){
        // get a handle only reference to the super class that wasn't found.
        try{
          superclass = _type.getType(typeName);
          if(!_superclasses.contains(superclass)){
            _superclasses.add(superclass);
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
   * @param _type The type to get the superclass of.
   * @return The superclass type or null if none.
   */
  private static IType getSuperClass (IType _type)
    throws Exception
  {
    String superclass = _type.getSuperclassName();
    if(superclass != null){
      String[][] types = _type.resolveType(superclass);
      if(types != null){
        for(int ii = 0; ii < types.length; ii++){
          String typeName = types[ii][0] + "." + types[ii][1];
          IType type = _type.getJavaProject().findType(typeName);
          return type;
        }
      }else{
        IType type = _type.getJavaProject().findType(superclass);
        return type;
      }
    }
    return null;
  }

  /**
   * Recursively gets the interfaces implemented by the given type.
   *
   * @param _type The type.
   * @param _interfaces The list to add results to.
   * @param _includeNotFound Whether or not to include types that were not found
   *  (adds them as handle only IType instances).
   */
  private static void getInterfaces (
      IType _type, List<IType> _interfaces, boolean _includeNotFound)
    throws Exception
  {
    // directly implemented interfaces.
    String[] interfaces = _type.getSuperInterfaceNames();
    for(int ii = 0; ii < interfaces.length; ii++){
      String[][] types = _type.resolveType(interfaces[ii]);
      if(types != null){
        for(int jj = 0; jj < types.length; jj++){
          String typeName = types[jj][0] + "." + types[jj][1];
          IType type = _type.getJavaProject().findType(typeName);
          if(type != null && !_interfaces.contains(type)){
            _interfaces.add(type);
            getInterfaces(type, _interfaces, _includeNotFound);
          }
        }
      }else{
        IType type = _type.getJavaProject().findType(interfaces[ii]);
        if(type != null && !_interfaces.contains(type)){
          _interfaces.add(type);
          getInterfaces(type, _interfaces, _includeNotFound);
        }else if(type == null && _includeNotFound){
          String typeName = interfaces[ii];
          if(typeName != null){
            // get a handle only reference to the super class that wasn't found.
            try{
              type = _type.getType(typeName);
              if(!_interfaces.contains(type)){
                _interfaces.add(type);
              }
            }catch(Exception e){
              // don't let the error cause the command to fail.
              logger.warn("Unable to get a handle to interface not found: '" +
                  typeName + "'", e);
            }
          }
        }else if(type == null){
          logger.warn("Unable to resolve implmented interface '{}' for '{}'",
              interfaces[ii], _type.getFullyQualifiedName());
        }
      }
    }

    // indirectly implemented interfaces
    IType superclass = getSuperClass(_type);
    if(superclass != null){
      getInterfaces(superclass, _interfaces, _includeNotFound);
    }
  }
}
