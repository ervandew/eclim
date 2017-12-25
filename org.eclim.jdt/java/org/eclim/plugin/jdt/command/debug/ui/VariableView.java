/**
 * Copyright (C) 2014 - 2017  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.debug.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclim.logging.Logger;

import org.eclim.plugin.jdt.command.debug.context.ThreadContext;

import org.eclipse.debug.core.DebugException;

import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;

import org.eclipse.jdt.core.Signature;

import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

import org.eclipse.jdt.internal.debug.core.logicalstructures.JDIAllInstancesValue;

import org.eclipse.jdt.internal.debug.core.model.JDIReferenceListValue;
import org.eclipse.jdt.internal.debug.core.model.JDIValue;
import org.eclipse.jdt.internal.debug.core.model.JDIVariable;

import org.eclipse.osgi.util.NLS;

/**
 * UI model for displaying variables.
 *
 * <p>
 * The formatting code is borrowed from Eclipse JDT UI.
 * @see org.eclipse.jdt.internal.debug.ui.JDIModelPresentation
 */
public class VariableView
{
  private static final Logger logger =
    Logger.getLogger(VariableView.class);

  /**
   * Depth of the root node.
   */
  private static final int ROOT_DEPTH = 0;

  /**
   * The selector of <code>java.lang.Object#toString()</code>,
   * used to evaluate 'toString()' for displaying details of a value.
   */
  private final String toStringSelector = "toString";

  /**
   * The signature of <code>java.lang.Object#toString()</code>,
   * used to evaluate 'toString()' for displaying details of a value.
   */
  private final String toStringSig = "()Ljava/lang/String;";

  /**
   * Thread being shown in UI.
   */
  private IJavaThread viewingThread;

  /**
   * Expanded variable map for the thread being shown in UI.
   */
  private Map<Long, ExpandableVar> expandableVarMap =
    new HashMap<Long, ExpandableVar>();

  private ThreadContext threadCtx;

  /**
   * Variable value that is shown in UI and is expandable; i.e., has inner
   * variables/fields. These values are instances of IJavaObject.
   */
  private class ExpandableVar
  {
    private IJavaValue value;

    /**
     * Determines if the variable value is expanded in the UI.
     */
    private boolean expanded = false;

    /**
     * Depth of this variable in tree. The root node will have depth = 0.
     */
    private int depth;

    public ExpandableVar(IJavaValue value, int depth)
    {
      this.value = value;
      this.depth = depth;
    }
  }

  public VariableView(ThreadContext threadCtx)
  {
    this.threadCtx = threadCtx;
  }

  /**
   * Returns the variable view for the thread currently being stepped through.
   * If there is no such thread, then a <code>null</code> is returned.
   *
   * @return List of variables.
   * @throws DebugException on failure.
   */
  public List<String> get()
    throws DebugException
  {
    IJavaThread thread = threadCtx.getSteppingThread();

    // Since the view is being reloaded, we can clear existing entries
    clear();

    if (thread == null) {
      return null;
    }
    this.viewingThread = thread;
    List<String> results = new ArrayList<String>();

    // Protect against variable information unavailable for native
    // methods
    try {
      IStackFrame stackFrame = thread.getTopStackFrame();
      if (stackFrame != null) {
        process(thread.getTopStackFrame().getVariables(), results, ROOT_DEPTH);
      }
    } catch (DebugException e) {
      // Suppress exception as it is possible to get an error when the current
      // stack frame points to native method. Variable information is not
      // available in this case.
      if (logger.isDebugEnabled()) {
        logger.debug("Unable to get variables", e);
      }
    }
    return results;
  }

  public List<String> expandValue(long valueId)
  {
    ExpandableVar expandableVar = expandableVarMap.get(valueId);

    if (expandableVar == null) {
      if (logger.isDebugEnabled()) {
        logger.debug("No variable value found with ID: " + valueId);
      }
      return null;
    }

    // If variable is already expanded, just return.
    if (expandableVar.expanded) {
      return null;
    }

    IJavaValue value = expandableVar.value;
    List<String> results = new ArrayList<String>();
    // Suppress any exception. No point in letting it propagate
    try {
      process(value.getVariables(), results, expandableVar.depth + 1);

      // Mark as expanded.
      expandableVar.expanded = true;
    } catch (DebugException e) {
      logger.error("Unable to get variables", e);
    }
    return results;
  }

  public void clear()
    throws DebugException
  {
    viewingThread = null;
    expandableVarMap.clear();
  }

  public boolean isViewingThread(IJavaThread thread)
    throws DebugException
  {
    return viewingThread != null &&
      (thread.getThreadObject().getUniqueId() ==
       viewingThread.getThreadObject().getUniqueId());
  }

  /**
   * Process the variables and adds them to the result set.
   * Some variables may be excluded because they are not important for
   * deugging purposes.
   * @see #ignoreVar method.
   *
   * @param vars variables
   * @param results final results containing the variable text
   * @param depth current nesting depth in the tree hierarchy
   * @throws DebugException on failure.
   */
  private void process(IVariable[] vars, List<String> results, int depth)
    throws DebugException
  {
    if (vars == null) {
      return;
    }

    for (IVariable var : vars) {
      if (var instanceof JDIVariable) {
        JDIVariable jdivar = (JDIVariable) var;
        if (jdivar.isSynthetic() ||
            ignoreVar(jdivar))
        {
          continue;
        }
      }
      if (!(var instanceof IJavaVariable)) {
	  continue;
      }

      IJavaVariable jvar = (IJavaVariable) var;
      IJavaValue value = (IJavaValue)var.getValue();
      boolean isLeafNode = !((value != null) &&
        (value instanceof IJavaObject) &&
        value.hasVariables());

      // Treat String as leaf node even though it has child variables
      isLeafNode = isLeafNode || ViewUtils.isStringValue(value);

      String prefix = getIndentation(depth, isLeafNode);
      results.add(prefix + getVariableText(jvar));

      // Keep track of this value as it is shown in UI and could be expanded
      if (!isLeafNode) {
        long valueId = ((IJavaObject) value).getUniqueId();
        // If this value was already seen, then don't update the map. This is
        // to prevent infinite recursion and also to not change the node depth
        // of the previously seen value. This case is normal for enum.
        if (!expandableVarMap.containsKey(valueId)) {
          expandableVarMap.put(valueId,
              new ExpandableVar(value, depth));
        }

        // Hack: Add an empty line so that VIM will think there are child nodes
        // and fold correctly.
        String childPrefix = getIndentation(depth + 1, true);
        results.add(childPrefix);
      }
    }
  }

  /**
   * Returns the toString value of the Java object.
   *
   * @param valueId The id of the value.
   * @return The string value of the value.
   * @throws DebugException on failure.
   */
  public String getDetail(long valueId)
    throws DebugException
  {
    ExpandableVar expandableVar = expandableVarMap.get(valueId);

    if (expandableVar == null) {
      if (logger.isDebugEnabled()) {
        logger.debug("No variable value found with ID: " + valueId);
      }
      return ViewUtils.UNKNOWN;
    }

    IJavaValue value = expandableVar.value;
    if (value instanceof IJavaObject) {
      IJavaValue toStrValue = ((IJavaObject) value).sendMessage(
          toStringSelector,
          toStringSig,
          null,
          viewingThread,
          false);

      return toStrValue == null ? ViewUtils.UNKNOWN : toStrValue.getValueString();
    } else {
      return ViewUtils.UNKNOWN;
    }
  }

  /**
   * Igmores final primitive variables.
   */
  private boolean ignoreVar(JDIVariable var)
    throws DebugException
  {
    if (var.isFinal()) {
      JDIValue value = (JDIValue) var.getValue();
      if (value instanceof IJavaObject) {
        return false;
      } else {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns the prefix string to use to simulate indentation.
   */
  private String getIndentation(int level, boolean isLeafNode)
  {
    if (level == ROOT_DEPTH) {
      if (isLeafNode) {
        return ViewUtils.LEAF_NODE_SYMBOL;
      } else {
        return ViewUtils.EXPANDED_NODE_SYMBOL;
      }
    }

    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < level - 1; i++) {
      sb.append(ViewUtils.LEAF_NODE_INDENT);
    }

    // Special indent for last level since the tree symbol is involved
    if (isLeafNode) {
      sb.append(ViewUtils.LEAF_NODE_INDENT +
          ViewUtils.LEAF_NODE_SYMBOL);
    } else {
      sb.append(ViewUtils.NON_LEAF_NODE_INDENT +
          ViewUtils.EXPANDED_NODE_SYMBOL);
    }

    return sb.toString();
  }

  private String getVariableText(IJavaVariable var)
  {
    String varLabel = ViewUtils.UNKNOWN;
    try {
      varLabel = var.getName();
    } catch (DebugException exception) {}

    IJavaValue javaValue = null;
    try {
      javaValue = (IJavaValue) var.getValue();
    } catch (DebugException e1) {}

    StringBuilder buff = new StringBuilder();
    buff.append(varLabel);

    // Add declaring type name if required
    if (var instanceof IJavaFieldVariable) {
      IJavaFieldVariable field = (IJavaFieldVariable) var;
      if (isDuplicateName(field)) {
        try {
          String decl = field.getDeclaringType().getName();
          buff.append(NLS.bind(" ({0})",
                new String[]{ViewUtils.getQualifiedName(decl)}));
        } catch (DebugException e) {}
      }
    }

    String valueString = getFormattedValueText(javaValue);

    // Do not put the equal sign for array partitions
    if (valueString.length() != 0) {
      buff.append(" = ");
      buff.append(valueString);
    }
    return buff.toString();
  }

  /**
   * Returns whether the given field variable has the same name as any variables
   */
  private boolean isDuplicateName(IJavaFieldVariable variable)
  {
    IJavaReferenceType javaType = variable.getReceivingType();
    try {
      String[] names = javaType.getAllFieldNames();
      boolean found = false;
      for (int i = 0; i < names.length; i++) {
        if (variable.getName().equals(names[i])) {
          if (found) {
            return true;
          }
          found = true;
        }
      }
      return false;
    } catch (DebugException e) {}

    return false;
  }

  /**
   * Returns text for the given value based on user preferences to display
   * toString() details.
   *
   * @param javaValue
   * @return text
   */
  private String getFormattedValueText(IJavaValue javaValue)
  {
    String valueString = ViewUtils.UNKNOWN;
    if (javaValue != null) {
      try {
        valueString = getValueText(javaValue);
      } catch (DebugException exception) {}
    }

    return valueString;
  }

  /**
   * Build the text for an IJavaValue.
   *
   * @param value the value to get the text for
   * @return the value string
   * @throws DebugException if something happens trying to compute the value string
   */
  private String getValueText(IJavaValue value)
    throws DebugException
  {
    String refTypeName = value.getReferenceTypeName();
    String valueString = value.getValueString();
    boolean isString = ViewUtils.isStringValue(value);
    IJavaType type = value.getJavaType();
    String signature = null;
    if (type != null) {
      signature = type.getSignature();
    }
    if ("V".equals(signature)) {
      valueString = ViewUtils.NO_EXPLICIT_RETURN_VALUE;
    }
    boolean isObject = isObjectValue(signature);
    boolean isArray = value instanceof IJavaArray;
    StringBuilder buffer = new StringBuilder();
    if (isUnknown(signature)) {
      buffer.append(signature);
    } else if (isObject && !isString && (refTypeName.length() > 0)) {
      // Don't show type name for instances and references
      if (!(value instanceof JDIReferenceListValue ||
            value instanceof JDIAllInstancesValue))
      {

        String qualTypeName = ViewUtils.getQualifiedName(refTypeName).trim();
        if (isArray) {
          qualTypeName = adjustTypeNameForArrayIndex(qualTypeName,
              ((IJavaArray)value).getLength());
        }
        buffer.append(qualTypeName);
        buffer.append(' ');
      }
    }

    // Put double quotes around Strings
    if (valueString != null && (isString || valueString.length() > 0)) {
      if (isString) {
        buffer.append('"');
      }
      buffer.append(valueString);
      if (isString) {
        buffer.append('"');
      }

    }

    return buffer.toString().trim();
  }

  /**
   * Given a JNI-style signature String for a IJavaValue, return true
   * if the signature represents an Object or an array of Objects.
   *
   * @param signature the signature to check
   * @return <code>true</code> if the signature represents an object;
   * <code>false</code> otherwise
   */
  private boolean isObjectValue(String signature)
  {
    if (signature == null) {
      return false;
    }
    String type = Signature.getElementType(signature);
    char sigchar = type.charAt(0);
    if(sigchar == Signature.C_UNRESOLVED ||
        sigchar == Signature.C_RESOLVED)
    {
      return true;
    }
    return false;
  }

  /**
   * Returns <code>true</code> if the given signature is not <code>null</code> and
   * matches the text '&lt;unknown&gt;'
   *
   * @param signature the signature to compare
   * @return <code>true</code> if the signature matches '&lt;unknown&gt;'
   */
  boolean isUnknown(String signature)
  {
    if(signature == null) {
      return false;
    }
    return ViewUtils.UNKNOWN.equals(signature);
  }

  /**
   * Given the reference type name of an array type, insert the array length
   * in between the '[]' for the first dimension and return the result.
   */
  private String adjustTypeNameForArrayIndex(String typeName, int arrayIndex)
  {
    int firstBracket = typeName.indexOf("[]");
    if (firstBracket < 0) {
      return typeName;
    }
    StringBuilder buffer = new StringBuilder(typeName);
    buffer.insert(firstBracket + 1, Integer.toString(arrayIndex));
    return buffer.toString();
  }
}
