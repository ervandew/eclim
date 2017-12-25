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
package org.eclim.plugin.jdt.command.hierarchy;

import java.util.List;

import org.eclim.plugin.jdt.util.JavaUtils;
import org.eclim.plugin.jdt.util.TypeInfo;
import org.eclim.plugin.jdt.util.TypeUtils;

import org.eclipse.jdt.core.IType;

/**
 * Represents a node in the class/interface hierarchy of a type.
 *
 * @author Eric Van Dewoestine
 */
public class HierarchyNode
{
  private String name;
  private String qualified;
  private List<HierarchyNode> children;

  /**
   * Constructs a new node.
   *
   * @param type The type for this node.
   * @param children The children for this node.
   */
  public HierarchyNode(IType type, List<HierarchyNode> children)
  {
    this.name = TypeUtils.getTypeSignature(new TypeInfo(type, null, null));
    this.qualified = JavaUtils.getFullyQualifiedName(type);
    this.children = children;
  }

  /**
   * Gets the name for this node.
   *
   * @return The name.
   */
  public String getName()
  {
    return this.name;
  }

  /**
   * Gets the fully qualified name for this node.
   *
   * @return The fully qualified name.
   */
  public String getQualified()
  {
    return this.qualified;
  }

  /**
   * Gets the children for this node.
   *
   * @return The children.
   */
  public List<HierarchyNode> getChildren()
  {
    return this.children;
  }
}
