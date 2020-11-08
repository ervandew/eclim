/**
 * Copyright (C) 2011 - 2020  Eric Van Dewoestine
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
package org.eclim.eclipse;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;

import java.net.URL;

import java.security.CodeSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import org.eclim.logging.Logger;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.util.IOUtils;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.osgi.util.ManifestElement;

import org.eclipse.swt.SWT;

import org.osgi.framework.Bundle;

public class EclimClasspathInitializer
  extends ClasspathContainerInitializer
{
  private static final Logger logger =
    Logger.getLogger(EclimClasspathInitializer.class);

  private Map<IPath, Map<String, String>> docJars =
    new HashMap<IPath, Map<String, String>>();
  private Set<String> platformNames = new HashSet<String>();
  {
    platformNames.add("org.eclipse.ant");
    platformNames.add("org.eclipse.compare");
    platformNames.add("org.eclipse.core");
    platformNames.add("org.eclipse.debug");
    platformNames.add("org.eclipse.equinox");
    platformNames.add("org.eclipse.help");
    platformNames.add("org.eclipse.jface");
    platformNames.add("org.eclipse.jsch");
    platformNames.add("org.eclipse.ltk");
    platformNames.add("org.eclipse.osgi");
    platformNames.add("org.eclipse.search");
    platformNames.add("org.eclipse.swt");
    platformNames.add("org.eclipse.team");
    platformNames.add("org.eclipse.text");
    platformNames.add("org.eclipse.ui");
    platformNames.add("org.eclipse.update");
  }

  @Override
  public void initialize(IPath path, IJavaProject javaProject)
    throws CoreException
  {
    if (javaProject.getProject().getName().equals("eclim")){
      EclimClasspathContainer container =
        new EclimClasspathContainer(path, computeClasspathEntries(javaProject));
      JavaCore.setClasspathContainer(
          path,
          new IJavaProject[]{javaProject},
          new IClasspathContainer[]{container},
          new NullProgressMonitor());
    }
  }

  private IClasspathEntry[] computeClasspathEntries(IJavaProject javaProject)
  {
    final ArrayList<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
    try{
      final LinkedHashSet<String> bundleNames = new LinkedHashSet<String>();
      final ArrayList<String> jarPaths = new ArrayList<String>();
      final String projectPath = ProjectUtils.getPath(javaProject.getProject());

      new File(projectPath).list(new FilenameFilter(){
        public boolean accept(File dir, String name)
        {
          if (name.startsWith("org.eclim")){
            // first pull bundle dependencies from the manifest
            File manifest = new File(
              dir.getPath() + '/' + name + "/META-INF/MANIFEST.MF");
            if (manifest.exists()){
              FileInputStream fin = null;
              try{
                fin = new FileInputStream(manifest);
                HashMap<String, String> headers = new HashMap<String, String>();
                ManifestElement.parseBundleManifest(fin, headers);
                bundleNames.addAll(
                  getBundleNamesFromHeader(headers, "Require-Bundle"));
                bundleNames.addAll(
                  getBundleNamesFromHeader(headers, "Eclim-ClassPath-Bundle"));
              }catch(Exception e){
                logger.error("Failed to load manifest: " + manifest, e);
              }finally{
                IOUtils.closeQuietly(fin);
              }
            }

            // then look for plugin jar files
            File lib = new File(dir.getPath() + '/' + name + "/lib");
            if (lib.exists()){
              for (String jar : lib.list()){
                if (jar.endsWith(".jar")){
                  jarPaths.add(name + "/lib/" + jar);
                }
              }
            }
          }
          return false;
        }
      });

      // load platform dependent swt jar
      CodeSource source = SWT.class.getProtectionDomain().getCodeSource();
      if (source != null){
        URL swt = source.getLocation();
        if (swt != null){
          logger.debug("adding swt to classpath: {}", swt.getPath());
          Path path = new Path(swt.getPath());
          list.add(JavaCore.newLibraryEntry(
                path, sourcePath(path), null, null, attributes(path), false));
        }
      }

      for (String name : bundleNames){
        logger.debug("adding bundle to classpath: {}", name);
        try{
          Bundle bundle = Platform.getBundle(name);
          if (bundle != null){
            String pathName = FileLocator.getBundleFile(bundle).getPath();
            Path path = new Path(pathName);
            list.add(JavaCore.newLibraryEntry(
                  path, sourcePath(path), null, null, attributes(path), false));

            if (path.toFile().isDirectory()){
              listFiles(path.toFile(), new FileFilter(){
                public boolean accept(File file) {
                  if (file.getName().endsWith(".jar")){
                    list.add(JavaCore.newLibraryEntry(
                          new Path(file.getPath()),
                          null, null, null, null, false));
                  }else if (file.isDirectory()){
                    listFiles(file, this);
                  }
                  return false;
                }
              });
            }
          }
        }catch(IOException ioe){
          logger.error("Failed to locate bundle: " + name, ioe);
        }
      }

      // some plugins need access to nested libraries extracted at build time,
      // handle adding those jars to the classpath here.
      listFiles(new File(projectPath + "/build/temp/lib"), new FileFilter(){
        public boolean accept(File file) {
          if (file.getName().endsWith(".jar")){
            String jar = file.getPath().replace(projectPath, "");
            if (jar.startsWith("/")){
              jar = jar.substring(1);
            }
            jarPaths.add(jar);
          }
          if (file.isDirectory()){
            listFiles(file, this);
          }
          return false;
        }
      });

      for (String jarPath : jarPaths){
        logger.debug("adding jar to classpath: {}", jarPath);
        list.add(JavaCore.newLibraryEntry(
              new Path(projectPath + '/' + jarPath),
              null, null, null, null, false));
      }
    }catch(Exception e){
      logger.error("Failed to load eclim classpath container", e);
    }
    return (IClasspathEntry[])list.toArray(new IClasspathEntry[list.size()]);
  }

  private List<String> getBundleNamesFromHeader(
      HashMap<String, String> headers, String header)
  {
    ArrayList<String> names = new ArrayList<String>();
    String bundles = headers.get(header);
    if (bundles != null){
      for (String bname : bundles.split(",\\s*")){
        if (bname.startsWith("org.eclim")){
          continue;
        }
        names.add(bname);
      }
    }
    return names;
  }

  private void listFiles(File dir, FileFilter filter)
  {
    dir.listFiles(filter);
  }

  private Path sourcePath(Path path)
  {
    Path sourcePath = null;

    // handle native bundles (swt)
    if (path.lastSegment().indexOf(".x86_64_") != -1){
      sourcePath = new Path(
          path.uptoSegment(path.segmentCount() - 1).toString() +
          File.separator +
          path.lastSegment().replace(".x86_64_", ".x86_64.source_"));

    // all other bundles
    }else{
      String[] parts = StringUtils.split(path.lastSegment(), "_", 2);
      sourcePath = parts.length == 2 ?
        new Path(
            path.uptoSegment(path.segmentCount() - 1).toString() +
            File.separator + parts[0] + ".source_" + parts[1]) :
        null;
    }

    return sourcePath != null && sourcePath.toFile().exists() ? sourcePath : null;
  }

  private IClasspathAttribute[] attributes(Path path)
  {
    String name = null;
    // handle native bundles (swt)
    if (path.lastSegment().indexOf(".x86_64_") != -1){
      name = path.lastSegment().replaceFirst(".x86_64_.*", "");

    // all other bundles
    }else{
      String[] parts = StringUtils.split(path.lastSegment(), "_", 2);
      name = parts[0];
    }
    if (!name.startsWith("org.eclipse.")){
      return null;
    }

    final IPath dir = path.uptoSegment(path.segmentCount() - 1);
    if (!docJars.containsKey(dir)){
      docJars.put(dir, new HashMap<String, String>());
      dir.toFile().list(new FilenameFilter(){
        public boolean accept(File path, String name){
          if (name.endsWith(".jar") && name.indexOf(".doc.isv_") != -1){
            String url = "jar:file:" + path + '/' + name + "!/reference/api";
            String bundleName = name.replaceFirst("\\.doc\\.isv_.*", "");
            docJars.get(dir).put(bundleName, url);
          }
          return false;
        }
      });
    }

    String url = null;
    Map<String, String> jars = docJars.get(dir);
    String shortName = StringUtils.join(
        StringUtils.split(name, ".", 4), ".", 0, 3);
    if (jars.containsKey(shortName)){
      url = jars.get(shortName);
    }else if (platformNames.contains(shortName) &&
        docJars.containsKey("org.eclipse.platform"))
    {
      url = jars.get("org.eclipse.platform");
    }

    if (url != null){
      return new IClasspathAttribute[]{
        JavaCore.newClasspathAttribute(
            IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME,
            url),
      };
    }

    return null;
  }
}
