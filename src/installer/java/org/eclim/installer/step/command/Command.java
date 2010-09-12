/**
 * Copyright (C) 2005 - 2010  Eric Van Dewoestine
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

import org.eclim.installer.step.EclipseUtils;

import org.formic.Installer;

import org.formic.util.CommandExecutor;

/**
 * Abstract class encapsulating logic to execute an eclipse feature command.
 *
 * @author Eric Van Dewoestine
 */
public abstract class Command
  extends CommandExecutor
{
  private static final String[] LAUNCHER = new String[]{
    "java", "-jar", null, "-clean", "-application", null
  };

  private OutputHandler handler;

  public Command(OutputHandler handler, String[] cmd)
    throws Exception
  {
    this(handler, cmd, "org.eclim.installer.application");
  }

  public Command(OutputHandler handler, String[] cmd, String application)
    throws Exception
  {
    this.handler = handler;
    this.cmd = new String[cmd.length + LAUNCHER.length];

    String launcher = EclipseUtils.findEclipseLauncherJar();
    if (launcher == null){
      throw new RuntimeException(
        "Could not find the eclipse launcher jar for eclipse home: " +
        Installer.getProject().getProperty("eclipse.home"));
    }

    System.arraycopy(LAUNCHER, 0, this.cmd, 0, LAUNCHER.length);
    System.arraycopy(cmd, 0, this.cmd, LAUNCHER.length, cmd.length);
    this.cmd[2] = launcher;
    this.cmd[5] = application;
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
