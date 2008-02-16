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
package org.eclim.command.archive;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

import java.util.Enumeration;

import org.apache.tools.ant.taskdefs.Untar.UntarCompressionMethod;

import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.util.IOUtils;
import org.eclim.util.StringUtils;

import org.eclim.util.file.FileUtils;

/**
 * Command to list all contents of an archive.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class ArchiveListAllCommand
  extends ArchiveListCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String file = _commandLine.getValue(Options.FILE_OPTION);
    if (file.endsWith(".jar") ||
        file.endsWith(".ear") ||
        file.endsWith(".war") ||
        file.endsWith(".egg") ||
        file.endsWith(".zip"))
    {
      return expand(file);
    }

    if (file.endsWith(".tar") ||
        file.endsWith(".tar.gz") ||
        file.endsWith(".tar.bz2") ||
        file.endsWith(".tgz") ||
        file.endsWith(".tbz2"))
    {
      return expandTar(file);
    }

    return StringUtils.EMPTY;
  }

  private String expand (String file)
    throws Exception
  {
    StringBuffer result = new StringBuffer();

    ZipFile zf = null;
    try{
      zf = new ZipFile(file, "UTF8");
      Enumeration e = zf.getEntries();
      while (e.hasMoreElements()) {
        ZipEntry ze = (ZipEntry)e.nextElement();
        if(result.length() > 0){
          result.append('\n');
        }
        result
          .append(ze.getName()).append('|')
          .append(FileUtils.getBaseName(ze.getName())).append('|')
          .append(toUrl(file, ze.getName())).append('|')
          .append(ze.isDirectory() ? "folder" : "file").append('|')
          .append(ze.getSize()).append('|')
          .append(formatTime(ze.getTime()));
      }
    }finally{
      ZipFile.closeQuietly(zf);
    }
    return result.toString();
  }

  private String expandTar (String file)
    throws Exception
  {
    StringBuffer result = new StringBuffer();

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
        if(result.length() > 0){
          result.append('\n');
        }
        result
          .append(te.getName()).append('|')
          .append(FileUtils.getBaseName(te.getName())).append('|')
          .append(toUrl(file, te.getName())).append('|')
          .append(te.isDirectory() ? "folder" : "file").append('|')
          .append(te.getSize()).append('|')
          .append(formatTime(te.getModTime()));
      }
    }finally{
        IOUtils.closeQuietly(tis);
        IOUtils.closeQuietly(fis);
    }
    return result.toString();
  }

  private String toUrl (String archive, String file)
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
  }
}
