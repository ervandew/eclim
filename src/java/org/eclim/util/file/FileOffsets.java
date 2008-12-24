/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
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
package org.eclim.util.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.ArrayList;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import org.eclim.Services;

import org.eclim.logging.Logger;

import org.eclim.util.IOUtils;

/**
 * Compiles a list of char offsets to line numbers and stores them for quick
 * translation of offset to line number and column.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class FileOffsets
{
  private static final Logger logger = Logger.getLogger(FileOffsets.class);

  private Integer[] offsets;
  private String[] multiByteLines;

  private FileOffsets ()
  {
  }

  /**
   * Reads the supplied file and compiles a list of offsets.
   *
   * @param _filename The file to compile a list of offsets for.
   * @return The FileOffsets instance.
   */
  public static FileOffsets compile (String _filename)
  {
    FileOffsets offsets = new FileOffsets();
    offsets.compileOffsets(_filename);
    return offsets;
  }

  /**
   * Reads the supplied file and compiles a list of offsets.
   *
   * @param _filename The file to compile a list of offsets for.
   */
  private void compileOffsets (String _filename)
  {
    BufferedLineReader reader = null;
    try{
      FileSystemManager fsManager = VFS.getManager();
      FileObject file = fsManager.resolveFile(_filename);

      // disable caching (the cache seems to become invalid at some point
      // causing vfs errors).
      //fsManager.getFilesCache().clear(file.getFileSystem());

      if(!file.exists()){
        throw new IllegalArgumentException(
            Services.getMessage("file.not.found", _filename));
      }
      reader = new BufferedLineReader(
          new InputStreamReader(file.getContent().getInputStream()));

      ArrayList<Integer> lines = new ArrayList<Integer>();
      lines.add(new Integer(0));
      ArrayList<String> byteLines = new ArrayList<String>();
      byteLines.add(null);

      int offset = 0;
      String line = null;
      while((line = reader.readLine()) != null){
        offset += line.length();
        lines.add(new Integer(offset));
        if (line.length() != line.getBytes().length){
          byteLines.add(line);
        }else{
          byteLines.add(null);
        }
      }

      offsets = (Integer[])lines.toArray(new Integer[lines.size()]);
      multiByteLines = (String[])byteLines.toArray(new String[byteLines.size()]);
    }catch(Exception e){
      throw new RuntimeException(e);
    }finally{
      IOUtils.closeQuietly(reader);
    }
  }

  /**
   * Converts the supplied offset into an int array where the first element is
   * the line number and the second is the column number.
   *
   * @param _offset The offset.
   * @return The line and column int array.
   */
  public int[] offsetToLineColumn (int _offset)
  {
    if(_offset <= 0){
      return new int[]{1,1};
    }

    int bot = -1;
    int top = offsets.length - 1;
    while (top - bot > 1) {
      int mid = (top + bot) / 2;
      if (offsets[mid].intValue() <  _offset){
        bot = mid;
      }else{
        top = mid;
      }
    }
    if(offsets[top].intValue() > _offset){
      top--;
    }
    int line = top + 1;
    int column = 1 + _offset - offsets[top].intValue();
    String value = multiByteLines[line];
    if (value != null){
      column = value.substring(0, column).getBytes().length;
    }
    return new int[]{line, column};
  }

  /**
   * Gets the offset where the supplied line starts.
   *
   * @param _line The line.
   * @return The starting offset.
   */
  public int getLineStart (int _line)
  {
    return offsets[_line - 1].intValue();
  }

  /**
   * Gets the offset where the supplied line ends.
   *
   * @param _line The line.
   * @return The ending offset.
   */
  public int getLineEnd (int _line)
  {
    return offsets[_line].intValue() - 1;
  }

  /**
   * Extension to BufferedReader which reads lines with the line ending
   * characters include (LF or CR, LF).
   */
  private static class BufferedLineReader
    extends BufferedReader
  {
    public BufferedLineReader(Reader reader){
      super(reader);
    }

    public String readLine()
      throws IOException
    {
      try{
        StringBuffer s = null;
        int startChar;

        Method fill = BufferedReader.class.getDeclaredMethod("fill");
        fill.setAccessible(true);
        Method ensureOpen = BufferedReader.class.getDeclaredMethod("ensureOpen");
        ensureOpen.setAccessible(true);

        Field cbF = BufferedReader.class.getDeclaredField("cb");
        cbF.setAccessible(true);
        Field nextCharF = BufferedReader.class.getDeclaredField("nextChar");
        nextCharF.setAccessible(true);
        Field nCharsF = BufferedReader.class.getDeclaredField("nChars");
        nCharsF.setAccessible(true);

        synchronized (lock) {
          ensureOpen.invoke(this);

          char[] cb = (char[])cbF.get(this);
          int nextChar = ((Integer)nextCharF.get(this)).intValue();
          int nChars = ((Integer)nCharsF.get(this)).intValue();

          for (;;) {
            if (nextChar >= nChars)
              fill.invoke(this);
              cb = (char[])cbF.get(this);
              nextChar = ((Integer)nextCharF.get(this)).intValue();
              nChars = ((Integer)nCharsF.get(this)).intValue();
            if (nextChar >= nChars) { /* EOF */
              if (s != null && s.length() > 0)
                return s.toString();
              return null;
            }
            boolean eol = false;
            char c = 0;
            int i;

            charLoop:
            for (i = nextChar; i < nChars; i++) {
              c = cb[i];
              if (c == '\n') {
                eol = true;
                break charLoop;
              }
            }

            startChar = nextChar;
            nextChar = i;
            nextCharF.set(this, new Integer(nextChar));

            if (eol) {
              String str;
              if (s == null) {
                str = new String(cb, startChar, i - startChar + 1);
              } else {
                s.append(cb, startChar, i - startChar + 1);
                str = s.toString();
              }
              nextChar++;
              nextCharF.set(this, new Integer(nextChar));
              return str;
            }

            if (s == null)
              s = new StringBuffer(80);
            s.append(cb, startChar, i - startChar);
          }
        }
      }catch(IllegalAccessException iae){
        logger.error("Error reading lines.", iae);
      }catch(InvocationTargetException ite){
        logger.error("Error reading lines.", ite);
      }catch(NoSuchFieldException nsfe){
        logger.error("Error reading lines.", nsfe);
      }catch(NoSuchMethodException nsme){
        logger.error("Error reading lines.", nsme);
      }
      return null;
    }
  }
}
