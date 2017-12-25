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

import java.util.ArrayList;
import java.util.List;

import org.eclim.logging.Logger;

import org.eclim.plugin.jdt.command.search.SearchRequestor;

import org.eclim.util.StringUtils;

import org.eclim.util.file.Position;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.Signature;

import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;

import org.eclipse.jdt.internal.core.CompilationUnit;

/**
 * Utility methods for working with IType.
 *
 * @author Eric Van Dewoestine
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
  {
    IJavaElement element = null;
    IType type = null;

    try{
      element = src.getElementAt(offset);
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }

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
  {
    try{
      ISourceRange range = reference.getSourceRange();
      return Position.fromOffset(
          type.getResource().getLocation().toOSString(), null,
          range.getOffset(), range.getLength());
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
  }

  /**
   * Gets the signature for the supplied type.
   *
   * @param typeInfo The typeInfo.
   * @return The signature.
   */
  public static String getTypeSignature(TypeInfo typeInfo)
  {
    StringBuffer buffer = new StringBuffer();
    try{
      IType type = typeInfo.getType();
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
      String[] params = typeInfo.getTypeParameters();
      String[] args = typeInfo.getTypeArguments();
      if (params != null && params.length > 0 && args != null && args.length > 0){
        buffer.append('<');
        for (int ii = 0; ii < args.length; ii++){
          if (ii > 0){
            buffer.append(',');
          }
          buffer.append(args[ii]);
        }
        buffer.append('>');
      }
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
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
  public static TypeInfo getSuperTypeContainingMethod(IType type, IMethod method)
  {
    try{
      TypeInfo[] types = getSuperTypes(type);
      for(TypeInfo info : types){
        IMethod[] methods = info.getType().getMethods();
        for (IMethod m : methods){
          if(m.isSimilar(method)){
            return info;
          }
        }
      }
    }catch(CoreException ce){
      throw new RuntimeException(ce);
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
  {
    try{
      TypeInfo[] types = getSuperTypes(type);
      for(TypeInfo info : types){
        IMethod[] methods = info.getType().getMethods();
        for (IMethod method : methods){
          String sig = MethodUtils.getMinimalMethodSignature(method, info);
          if(sig.equals(signature)){
            return new Object[]{info, method};
          }
        }
      }
    }catch(CoreException ce){
      throw new RuntimeException(ce);
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
      try{
        result = param.getBounds()[0];
      }catch(CoreException ce){
        throw new RuntimeException(ce);
      }
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
  {
    try{
      String name = "." + typeName;

      // strip of generic type if present.
      int index = name.indexOf('<');
      if(index != -1){
        name = name.substring(0, index);
      }

      // search imports
      IImportDeclaration[] imports = src.getImports();
      for(IImportDeclaration decl : imports){
        if(decl.getElementName().endsWith(name)){
          return src.getJavaProject().findType(decl.getElementName());
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
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
  }

  /**
   * Recursively gets all superclass and implemented interfaces from the
   * supplied type.
   *
   * @param type The type.
   * @return Array of types.
   */
  public static TypeInfo[] getSuperTypes(IType type)
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
  public static TypeInfo[] getSuperTypes(IType type, boolean returnNotFound)
  {
    TypeInfo[] interfaces = getInterfaces(type, returnNotFound);
    TypeInfo[] superClasses = getSuperClasses(type, returnNotFound);
    TypeInfo[] types = new TypeInfo[interfaces.length + superClasses.length];

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
  public static TypeInfo[] getSuperClasses(IType type)
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
  public static TypeInfo[] getSuperClasses(IType type, boolean returnNotFound)
  {
    ArrayList<TypeInfo> types = new ArrayList<TypeInfo>();

    try{
      getSuperClasses(type, types, returnNotFound, null);

      // add java.lang.Object if not already added
      IType objectType = type.getJavaProject().findType("java.lang.Object");
      TypeInfo objectTypeInfo = new TypeInfo(objectType, null, null);
      if(!types.contains(objectTypeInfo)){
        types.add(objectTypeInfo);
      }
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }

    return (TypeInfo[])types.toArray(new TypeInfo[types.size()]);
  }

  /**
   * Recursively gets all the implemented interfaces for the given type.
   *
   * @param type The type.
   * @return Array of interface types.
   */
  public static TypeInfo[] getInterfaces(IType type)
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
  public static TypeInfo[] getInterfaces(IType type, boolean returnNotFound)
  {
    ArrayList<TypeInfo> types = new ArrayList<TypeInfo>();
    getInterfaces(type, types, returnNotFound, null);

    return (TypeInfo[])types.toArray(new TypeInfo[types.size()]);
  }

  /**
   * Recursively gets all the superclasses for the given type.
   *
   * @param type The type.
   * @param superclasses The list to add results to.
   * @param includeNotFound Whether or not to include types that were not found
   * (adds them as handle only IType instances).
   * @param baseType The first type in the recursion stack.
   */
  public static void getSuperClasses(
      IType type,
      List<TypeInfo> superclasses,
      boolean includeNotFound,
      TypeInfo baseType)
  {
    TypeInfo superclassInfo = getSuperClass(type, baseType);
    if (superclassInfo != null){
      if (baseType == null ||
          baseType.getTypeArguments().length !=
            superclassInfo.getTypeArguments().length)
      {
        baseType = superclassInfo;
      }
    }

    if(superclassInfo != null && !superclasses.contains(superclassInfo)){
      superclasses.add(superclassInfo);
      getSuperClasses(
          superclassInfo.getType(), superclasses, includeNotFound, baseType);
    }else if(superclassInfo == null && includeNotFound){
      String typeName = null;
      try{
        typeName = type.getSuperclassName();
      }catch(CoreException ce){
        throw new RuntimeException(ce);
      }
      if(typeName != null){
        // get a handle only reference to the super class that wasn't found.
        try{
          IType superclass = type.getType(typeName);
          superclassInfo = new TypeInfo(superclass, null, null);
          if(!superclasses.contains(superclassInfo)){
            superclasses.add(superclassInfo);
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
  public static TypeInfo getSuperClass(IType type)
  {
    return getSuperClass(type, null);
  }

  /**
   * Gets the super type of the supplied type, if any.
   *
   * @param type The type to get the superclass of.
   * @return The superclass type or null if none.
   */
  private static TypeInfo getSuperClass(IType type, TypeInfo baseType)
  {
    try{
      String superclassSig = type.getSuperclassTypeSignature();
      if(superclassSig != null){
        String qualifier = Signature.getSignatureQualifier(superclassSig);
        qualifier =
          (qualifier != null && !qualifier.equals(StringUtils.EMPTY)) ?
          qualifier + '.' : StringUtils.EMPTY;
        String superclass =
          qualifier + Signature.getSignatureSimpleName(superclassSig);
        String[] args = Signature.getTypeArguments(superclassSig);
        String[] typeArgs = new String[args.length];
        for (int ii = 0; ii < args.length; ii++){
          typeArgs[ii] = Signature.getSignatureSimpleName(args[ii]);
        }
        if (baseType != null &&
            baseType.getTypeArguments().length == typeArgs.length)
        {
          typeArgs = baseType.getTypeArguments();
        }

        String[][] types = type.resolveType(superclass);
        if(types != null){
          for(String[] typeInfo : types){
            String typeName = typeInfo[0] + "." + typeInfo[1];
            IType found = type.getJavaProject().findType(typeName);
            if (found != null){
              ITypeParameter[] params = found.getTypeParameters();
              String[] typeParams = new String[params.length];
              for (int ii = 0; ii < params.length; ii++){
                typeParams[ii] = params[ii].getElementName();
              }
              return new TypeInfo(found, typeParams, typeArgs);
            }
          }
        }else{
          IType found = type.getJavaProject().findType(superclass);
          if (found != null){
            return new TypeInfo(found, null, typeArgs);
          }
        }
      }
    }catch(CoreException ce){
      throw new RuntimeException(ce);
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
  {
    try{
      String[] parents = type.getSuperInterfaceNames();
      ArrayList<IType> interfaces = new ArrayList<IType>(parents.length);
      for(String parent : parents){
        String[][] types = type.resolveType(parent);
        if(types != null){
          for(String[] typeInfo : types){
            String typeName = typeInfo[0] + "." + typeInfo[1];
            IType found = type.getJavaProject().findType(typeName);
            if(found != null){
              interfaces.add(found);
            }
          }
        }else{
          IType found = type.getJavaProject().findType(parent);
          if(found != null){
            interfaces.add(found);
          }
        }
      }
      return interfaces.toArray(new IType[interfaces.size()]);
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
  }

  /**
   * Recursively gets the interfaces implemented by the given type.
   *
   * @param type The type.
   * @param interfaces The list to add results to.
   * @param includeNotFound Whether or not to include types that were not found
   *  (adds them as handle only IType instances).
   * @param baseType The first type in the recursion stack.
   */
  public static void getInterfaces(
      IType type,
      List<TypeInfo> interfaces,
      boolean includeNotFound,
      TypeInfo baseType)
  {
    try{
      // directly implemented interfaces.
      String[] parentSigs = type.getSuperInterfaceTypeSignatures();
      for(String parentSig : parentSigs){
        String parent = Signature.getSignatureSimpleName(parentSig);

        String[] args = Signature.getTypeArguments(parentSig);
        String[] typeArgs = new String[args.length];
        for (int ii = 0; ii < args.length; ii++){
          typeArgs[ii] = Signature.getSignatureSimpleName(args[ii]);
        }
        if (baseType != null &&
            baseType.getTypeArguments().length == typeArgs.length)
        {
          typeArgs = baseType.getTypeArguments();
        }

        IType found = null;
        String[][] types = type.resolveType(parent);
        if(types != null){
          for(String[] typeInfo : types){
            String typeName = typeInfo[0] + "." + typeInfo[1];
            found = type.getJavaProject().findType(typeName);
          }
        }else{
          found = type.getJavaProject().findType(parent);
        }

        if(found != null){
          ITypeParameter[] params = found.getTypeParameters();
          String[] typeParams = new String[params.length];
          for (int ii = 0; ii < params.length; ii++){
            typeParams[ii] = params[ii].getElementName();
          }
          TypeInfo typeInfo = new TypeInfo(found, typeParams, typeArgs);
          if (!interfaces.contains(typeInfo)){
            interfaces.add(typeInfo);
            if (baseType == null ||
                baseType.getTypeArguments().length !=
                  typeInfo.getTypeArguments().length)
            {
              baseType = typeInfo;
            }
            getInterfaces(found, interfaces, includeNotFound, baseType);
          }
        }else if(found == null && includeNotFound){
          String typeName = parent;
          if(typeName != null){
            // get a handle only reference to the super class that wasn't found.
            try{
              found = type.getType(typeName);
              TypeInfo typeInfo = new TypeInfo(found, null, typeArgs);
              if(!interfaces.contains(typeInfo)){
                interfaces.add(typeInfo);
              }
            }catch(Exception e){
              // don't let the error cause the command to fail.
              logger.warn("Unable to get a handle to interface not found: '" +
                  typeName + "'", e);
            }
          }
        }else if(found == null){
          logger.warn("Unable to resolve implmented interface '{}' for '{}'",
              parent, type.getFullyQualifiedName());
        }
      }

      // indirectly implemented parents
      TypeInfo superclassInfo = getSuperClass(type);
      if(superclassInfo != null){
        getInterfaces(
            superclassInfo.getType(), interfaces, includeNotFound, superclassInfo);
      }
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
  }

  /**
   * If the supplied type represents a generic param type, then replace it with
   * the properly concrete type from the supplied type info.
   *
   * @param type The possibly generic param type.
   * @param typeInfo The type info.
   * @return The concrete type.
   */
  public static String replaceTypeParams(String type, TypeInfo typeInfo)
  {
    if(typeInfo != null){
      String[] params = typeInfo.getTypeParameters();
      String[] args = typeInfo.getTypeArguments();
      if (params != null && params.length == args.length){
        for (int ii = 0; ii < params.length; ii++){
          type = type.replaceAll("\\b" + params[ii] + "\\b", args[ii]);
        }
      }
    }
    return type;
  }

  /**
   * Find types by the supplied fully qualified name or unqualified class name.
   *
   * @param javaProject The java project to be searched.
   * @param name The name to search.
   *
   * @return A possibly empty array of IType results found.
   */
  public static IType[] findTypes(IJavaProject javaProject, String name)
  {
    SearchPattern pattern =
      SearchPattern.createPattern(name,
          IJavaSearchConstants.TYPE,
          IJavaSearchConstants.DECLARATIONS,
          SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);
    IJavaSearchScope scope =
      SearchEngine.createJavaSearchScope(new IJavaElement[]{javaProject});
    SearchRequestor requestor = new SearchRequestor();
    SearchEngine engine = new SearchEngine();
    SearchParticipant[] participants =
      new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()};
    try{
      engine.search(pattern, participants, scope, requestor, null);
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }

    ArrayList<IType> types = new ArrayList<IType>();
    if (requestor.getMatches().size() > 0){
      for (SearchMatch match : requestor.getMatches()){
        if(match.getAccuracy() != SearchMatch.A_ACCURATE){
          continue;
        }
        IJavaElement element = (IJavaElement)match.getElement();
        if (element.getElementType() == IJavaElement.TYPE){
          types.add((IType)element);
        }
      }
    }
    return types.toArray(new IType[types.size()]);
  }
}
