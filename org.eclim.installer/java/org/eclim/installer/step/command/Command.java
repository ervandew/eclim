/**
 * Copyright (C) 2005 - 2012  Eric Van Dewoestine
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
package org.eclim.installer.step.command;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Map;

import java.util.regex.Pattern;

import org.eclim.installer.step.EclipseUtils;

import org.formic.Installer;

import org.formic.util.CommandExecutor;

/**
 * Class encapsulating logic to execute an eclipse application command.
 *
 * @author Eric Van Dewoestine
 */
public class Command
  extends CommandExecutor
{
  private static final String[] LAUNCHER = new String[]{
    "-jar", null, "-clean", "-application", null,
  };
  private static final Pattern PROPERTY_RE =
    Pattern.compile("^(http\\.|java\\.net\\.|org\\.eclipse\\.).*");

  private OutputHandler handler;

  public Command(OutputHandler handler, String[] cmd)
  {
    this(handler, cmd, "org.eclim.installer.application");
  }

  public Command(OutputHandler handler, String[] cmd, String application)
  {
    String[] jargs = {"java", "-Xmx256m"};
    String[] vmargs = getJvmArgs();

    this.handler = handler;
    this.cmd = new String[
      jargs.length + vmargs.length + cmd.length + LAUNCHER.length];

    String launcher = EclipseUtils.findEclipseLauncherJar();
    if (launcher == null){
      throw new RuntimeException(
        "Could not find the eclipse launcher jar for eclipse home: " +
        Installer.getProject().getProperty("eclipse.home"));
    }

    int index = 0;

    System.arraycopy(jargs, 0, this.cmd, index, jargs.length);
    index += jargs.length;

    System.arraycopy(vmargs, 0, this.cmd, index, vmargs.length);
    index += vmargs.length;

    System.arraycopy(LAUNCHER, 0, this.cmd, index, LAUNCHER.length);
    index += LAUNCHER.length;

    this.cmd[jargs.length + vmargs.length + 1] = launcher;
    this.cmd[jargs.length + vmargs.length + 4] = application;
    System.arraycopy(cmd, 0, this.cmd, index, cmd.length);
  }

  @SuppressWarnings("rawtypes")
  protected String[] getJvmArgs()
  {
    ArrayList<String> vmargs = new ArrayList<String>();
    for (Map.Entry entry : System.getProperties().entrySet()){
      String name = (String)entry.getKey();
      if (PROPERTY_RE.matcher(name).matches()){
        String value = (String)entry.getValue();
        if (value.length() > 0){
          vmargs.add("-D" + name + '=' + entry.getValue());
        }else{
          vmargs.add("-D" + name);
        }
      }
    }
    return vmargs.toArray(new String[vmargs.size()]);
  }

  /**
   * {@inheritDoc}
   * @see CommandExecutor#createOutThread(OutputStream)
   */
  protected Thread createOutThread(final OutputStream out)
  {
    return new Thread(){
      public void run(){
        StringBuffer buffer = new StringBuffer();
        try{
          BufferedReader reader = new BufferedReader(
              new InputStreamReader(process.getInputStream()));
          String line = null;
          while((line = reader.readLine()) != null){
            if (buffer.length() != 0){
              buffer.append('\n');
            }
            buffer.append(line);

            if (handler != null){
              handler.process(line);
            }
          }
        }catch(Exception e){
          e.printStackTrace();
          error = e.getMessage();
          returnCode = 1000;
          process.destroy();
        }finally{
          result = buffer.toString();
        }
      }
    };
  }
}
