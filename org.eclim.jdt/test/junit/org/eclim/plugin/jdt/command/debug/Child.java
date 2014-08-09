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

import java.util.HashMap;
import java.util.Map;

public class Child extends Person
{
  private Map<String, Integer> toysMap = new HashMap<String, Integer>();

  public Child(String name, Sex sex)
  {
    super(name, sex);
  }

  public void addToys() {
    toysMap.put("Ball", 2);
    toysMap.put("Bat", 1);
    toysMap.put("Car", 1);
  }

  public Map<String, Integer> getToysMap() {
    return toysMap;
  }
}
