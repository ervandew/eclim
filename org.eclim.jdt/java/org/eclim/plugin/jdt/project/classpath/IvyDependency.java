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
package org.eclim.plugin.jdt.project.classpath;

import org.eclipse.core.runtime.IPath;

/**
 * Extension to {@link Dependency} which resolves the dependency artifact
 * according to the ivy cache layout.
 *
 * @author Eric Van Dewoestine
 */
public class IvyDependency
  extends Dependency
{
  private static final String JARS = "jars";

  public IvyDependency(String org, String name, String version, IPath path)
  {
    super(org, name, version, path);
  }

  @Override
  public String resolveArtifact()
  {
    StringBuffer buffer = new StringBuffer();
    buffer.append(getOrganization()).append(SEPARATOR)
      .append(getName()).append(SEPARATOR)
      .append(JARS).append(SEPARATOR)
      .append(toString());

    return buffer.toString();
  }
}
