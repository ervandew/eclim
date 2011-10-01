/**
 * Copyright (C) 2005 - 2011  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.include;

import java.util.List;

/**
 * Encapsulates a result for an undefined type and the possible imports to
 * define it.
 *
 * @author Eric Van Dewoestine
 */
public class ImportMissingResult
{
  private String type;
  private List<String> imports;

  /**
   * Constructs a new instance.
   *
   * @param type The type for this instance.
   * @param imports The imports for this instance.
   */
  public ImportMissingResult(String type, List<String> imports)
  {
    this.type = type;
    this.imports = imports;
  }

  /**
   * Gets the type for this instance.
   *
   * @return The type.
   */
  public String getType()
  {
    return this.type;
  }

  /**
   * Gets the imports for this instance.
   *
   * @return The imports.
   */
  public List<String> getImports()
  {
    return this.imports;
  }
}
