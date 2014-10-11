/**
 * Copyright (C) 2014  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.debug;

public class Breakpoint
{
  public String project;
  public String filename;
  public String name;
  public int line;
  public boolean enabled;

  public Breakpoint(
      String project,
      String filename,
      String name,
      int line,
      boolean enabled)
  {
    this.project = project;
    this.filename = filename;
    this.name = name;
    this.line = line;
    this.enabled = enabled;
  }
}
