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
package org.eclim.plugin.jdt.project.classpath;

/**
 * Defines a parse capable of parsing some external build file that defines the
 * dependencies for the project.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public interface Parser
{
  /**
   * Parses the supplied file name and returns an array of Dependency defined in
   * that fila.
   *
   * @param _filename The file to parse.
   * @return The array of Dependency.
   */
  public Dependency[] parse (String _filename)
    throws Exception;
}
