/**
 * Copyright (c) 2004 - 2005
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
package org.eclim.util.file;

import java.io.File;

import java.util.Enumeration;

import javax.naming.CompositeName;

import org.apache.commons.io.FilenameUtils;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.VFS;

/**
 * Utilities for working w/ files and commons vfs.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class FileUtils
{
  public static final String JAR_PREFIX = "jar:";
  public static final String ZIP_PREFIX = "zip:";
  public static final String JAR_EXT = ".jar";
  public static final String ZIP_EXT = ".zip";

  /**
   * Translates a file name that does not conform to the standard url file
   * format.
   * <p/>
   * Main purpose is to convert paths like:<br/>
   * <code>/opt/sun-jdk-1.5.0.05/src.zip/javax/swing/Spring.java</code><br/>
   * to<br/>
   * <code>zip:file:///opt/sun-jdk-1.5.0.05/src.zip!/javax/swing/Spring.java</code>
   *
   * @param _file
   * @return
   */
  public static String toUrl (String _file)
  {
    // if the path points to a real file, return it.
    if(new File(_file).exists()){
      return _file;
    }

    // otherwise do some convertion.
    StringBuffer buffer = new StringBuffer(FilenameUtils.getPrefix(_file));

    try{
      int index = FilenameUtils.getPrefixLength(_file);
      CompositeName fileName = new CompositeName(_file.substring(index));
      Enumeration names = fileName.getAll();
      while(names.hasMoreElements()){
        String name = (String)names.nextElement();
        if(name.indexOf("$") != -1){
          name = name.substring(0, name.indexOf("$")) + '.' +
            FilenameUtils.getExtension(name);
        }
        if(name.length() != 0){
          if(buffer.length() != index){
            buffer.append('/');
          }
          buffer.append(name);

          FileObject file = VFS.getManager()
            .resolveFile(buffer.toString());
          if(!file.exists()){
            String path = file.getParent().getName().getPath();
            if(path.endsWith(JAR_EXT)){
              buffer = new StringBuffer(JAR_PREFIX)
                .append(path).append('!').append(name);
            }else if(path.endsWith(ZIP_EXT)){
              buffer = new StringBuffer(ZIP_PREFIX)
                .append(path).append('!').append(name);
            }
          }
        }
      }
    }catch(Exception e){
      throw new RuntimeException(e);
    }

    return buffer.toString();
  }
}
