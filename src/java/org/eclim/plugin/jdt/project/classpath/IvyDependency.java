/**
 * Copyright (c) 2005 - 2006
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
package org.eclim.plugin.jdt.project.classpath;

import org.eclipse.core.runtime.IPath;

/**
 * Extension to {@link Dependency} which resolves the dependency artifact
 * according to the ivy cache layout.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class IvyDependency
  extends Dependency
{
  private static final String SEPARATOR = "/";
  private static final String JARS = "jars";

  /**
   * @see Dependency#Dependency(String,String,String,IPath)
   */
  public IvyDependency (String _org, String _name, String _version, IPath _path)
  {
    super(_org, _name, _version, _path);
  }

  /**
   * {@inheritDoc}
   * @see Dependency#resolveArtifact()
   */
  public String resolveArtifact ()
  {
    StringBuffer buffer = new StringBuffer();
    buffer.append(getOrganization()).append(SEPARATOR)
      .append(getName()).append(SEPARATOR)
      .append(JARS).append(SEPARATOR)
      .append(toString());

    return buffer.toString();
  }
}
