/**
 * Copyright (C) 2005 - 2014  Eric Van Dewoestine
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

public class Person
{
  protected final String name;

  protected final Sex sex;

  public Person(String name, Sex sex)
  {
    this.name = name;
    this.sex = sex;
  }

  public String getName() {
    return name;
  }

  public Sex getSex() {
    return sex;
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("Name:")
      .append(name)
      .append("\n")
      .append("Sex:")
      .append(sex);

    return sb.toString();
  }
}
