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

/**
 * Defines a parse capable of parsing some external build file that defines the
 * dependencies for the project.
 *
 * @author Eric Van Dewoestine
 */
public interface Parser
{
  /**
   * Returns the classpath var that all entries from the build file will be
   * prefixed with.
   *
   * @return the classpath var.
   */
  public String getClasspathVar();

  /**
   * Parses the supplied file name and returns an array of Dependency defined in
   * that fila.
   *
   * @param filename The file to parse.
   * @return The array of Dependency.
   */
  public Dependency[] parse(String filename);
}
