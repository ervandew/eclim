/**
 * Copyright (C) 2005 - 2009  Eric Van Dewoestine
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
package org.eclim.plugin.core.command.archive;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

import java.text.Collator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;

import org.apache.tools.ant.taskdefs.Untar.UntarCompressionMethod;

import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.util.IOUtils;
import org.eclim.util.StringUtils;

/**
 * Command to list all contents of an archive.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "archive_list_all",
  options = "REQUIRED f file ARG"
)
public class ArchiveListAllCommand
  extends ArchiveListCommand
{
  private static final Comparator<String[]> COMPARATOR = new EntryComparator();

  /**
   * {@inheritDoc}
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String file = commandLine.getValue(Options.FILE_OPTION);
    Object[] results = null;
    if (file.endsWith(".jar") ||
        file.endsWith(".ear") ||
        file.endsWith(".war") ||
        file.endsWith(".egg") ||
        file.endsWith(".zip")){
      results = expand(file);
    } else if (file.endsWith(".tar") ||
        file.endsWith(".tar.gz") ||
        file.endsWith(".tar.bz2") ||
        file.endsWith(".tgz") ||
        file.endsWith(".tbz2")){
      results = expandTar(file);
    }

    File tmp = File.createTempFile("eclim", "archive");
    BufferedWriter out = null;
    try{
      out = new BufferedWriter(new FileWriter(tmp));
      ArrayList<String[]> entries = (ArrayList<String[]>)results[0];
      Collections.sort(entries, COMPARATOR);
      int maxName = (Integer)results[1] + 2;
      int maxSize = (Integer)results[2] + 2;

      for (String[] entry : entries) {
        out.write(
            StringUtils.rightPad(entry[0], maxName, ' ') +
            StringUtils.rightPad(entry[1], maxSize, ' ') +
            entry[2] + '\n');
      }
    }finally{
      IOUtils.closeQuietly(out);
      tmp.deleteOnExit();
    }

    return tmp.getAbsolutePath();
  }

  private Object[] expand(String file)
    throws Exception
  {
    ArrayList<String[]> results = new ArrayList<String[]>();
    int maxName = 0;
    int maxSize = 0;

    ZipFile zf = null;
    try{
      zf = new ZipFile(file, "UTF8");
      Enumeration<ZipEntry> e = zf.getEntries();
      while (e.hasMoreElements()) {
        ZipEntry ze = e.nextElement();
        if(!ze.isDirectory()){
          String name = ze.getName();
          String size = String.valueOf(ze.getSize());
          results.add(new String[]{
            name, size, formatTime(ze.getTime())
          });
          maxName = name.length() > maxName ? name.length() : maxName;
          maxSize = size.length() > maxSize ? size.length() : maxSize;
        }
      }
    }finally{
      ZipFile.closeQuietly(zf);
    }
    return new Object[]{results, maxName, maxSize};
  }

  private Object[] expandTar(String file)
    throws Exception
  {
    ArrayList<String[]> results = new ArrayList<String[]>();
    int maxName = 0;
    int maxSize = 0;

    TarInputStream tis = null;
    FileInputStream fis = null;
    try{
      UntarCompressionMethod compression = new UntarCompressionMethod();
      if(file.endsWith(".tar.gz") || file.endsWith(".tgz")){
        compression.setValue("gzip");
      }else if(file.endsWith(".tar.bz2") || file.endsWith(".tbz2")){
        compression.setValue("bzip2");
      }
      fis = new FileInputStream(file);
      tis = new TarInputStream(
          compression.decompress("", new BufferedInputStream(fis)));
      TarEntry te = null;
      while ((te = tis.getNextEntry()) != null) {
        if(!te.isDirectory()){
          String name = te.getName();
          String size = String.valueOf(te.getSize());
          results.add(new String[]{
            name, size, formatTime(te.getModTime())
          });
          maxName = name.length() > maxName ? name.length() : maxName;
          maxSize = size.length() > maxSize ? size.length() : maxSize;
        }
      }
    }finally{
        IOUtils.closeQuietly(tis);
        IOUtils.closeQuietly(fis);
    }
    return new Object[]{results, maxName, maxSize};
  }

  /*private String toUrl (String archive, String file)
  {
    if (archive.endsWith(".jar") ||
        archive.endsWith(".ear") ||
        archive.endsWith(".war") ||
        archive.endsWith(".egg") ||
        archive.endsWith(".zip"))
    {
      return "jar:" + archive + "!/" + file;
    }

    if(archive.endsWith(".tar")){
      return "tar:" + archive + "!/" + file;
    }

    if(archive.endsWith(".tar.gz") || archive.endsWith(".tgz")){
      return "tgz:" + archive + "!/" + file;
    }

    if(archive.endsWith(".tar.bz2") || archive.endsWith(".tbz2")){
      return "tbz2:" + archive + "!/" + file;
    }
    // shouldn't happen
    return archive + '/' + file;
  }*/

  private static class EntryComparator
    implements Comparator<String[]>
  {
    private static final Collator COLLATOR = Collator.getInstance();

    /**
     * {@inheritDoc}
     * @see Comparator#compare(T,T)
     */
    public int compare(String[] o1, String[] o2)
    {
      return COLLATOR.compare(o1[0], o2[0]);
    }

    /**
     * {@inheritDoc}
     * @see Comparator#equals(Object)
     */
    public boolean equals(Object obj)
    {
      if(obj instanceof EntryComparator){
        return true;
      }
      return false;
    }
  }
}
