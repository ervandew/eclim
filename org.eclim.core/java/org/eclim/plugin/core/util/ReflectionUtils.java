/**
 * Copyright (C) 2012  Eric Van Dewoestine
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
package org.eclim.plugin.core.util;

/**
 * Utility class containing methods to ease some reflections tasks.
 *
 * @author Eric Van Dewoestine
 */
public class ReflectionUtils
{
  /**
   * Attempt to load a class for one ore more supplied class names, returning
   * the first one found. Useful for handling cross eclipse version
   * compatibility.
   *
   * @param context The class to use as the context (uses this class's
   * classloader).
   * @param names One or more fully qualified class names.
   * @return A Class for the first class found or null if none found.
   */
  public static Class<?> loadClass(Class context, String... names){
    for(String name : names){
      try{
        Class<?> clazz = Class.forName(name, true, context.getClassLoader());
        return clazz;
      }catch(ClassNotFoundException cnfe){
        // ignore, try the next name.
      }
    }
    return null;
  }

  /**
   * Get the value of an int field from the supplied class.
   *
   * @param clazz The class which defines the field.
   * @param name The name of the field.
   * @param instance The instance to get the value from, or null or a static
   * field.
   * @return
   */
  public static int getIntField(Class<?> clazz, String name, Object instance)
  {
    try{
      return ((Integer)clazz.getField(name).get(null)).intValue();
    }catch(NoSuchFieldException nsfe){
      throw new RuntimeException(nsfe);
    }catch(IllegalAccessException iae){
      throw new RuntimeException(iae);
    }
  }
}
