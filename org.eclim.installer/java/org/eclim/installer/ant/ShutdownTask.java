/**
 * Copyright (C) 2005 - 2011  Eric Van Dewoestine
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
package org.eclim.installer.ant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;

import java.net.Socket;

import java.util.Iterator;

import org.apache.commons.io.IOUtils;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Task for shutting down eclimd.
 *
 * @author Eric Van Dewoestine
 */
public class ShutdownTask
  extends Task
{
  private static final long WAIT_TIME = 3000;

  /**
   * Executes this task.
   */
  @SuppressWarnings("unchecked")
  public void execute()
    throws BuildException
  {
    FileReader reader = null;
    try{
      File instances = new File(
          System.getProperty("user.home") + "/.eclim/.eclimd_instances");
      int count = 0;
      if (instances.exists()){
        reader = new FileReader(instances);
        for(Iterator<String> ii = IOUtils.lineIterator(reader); ii.hasNext();){
          count++;
          String instance = ii.next();
          try{
            log("Shutting down eclimd: " + instance);
            int port = Integer.parseInt(instance.replaceFirst(".*:", ""));
            shutdown(port);
          }catch(Exception e){
            log("Unable to shut down eclimd (" + instance + "): " +
                e.getClass().getName() + " - " + e.getMessage());
          }
        }
      }

      // if no registered instances found, try shutting down the default port to
      // account for users on old eclim versions
      if (count == 0){
        try{
          shutdown(9091);
        }catch(Exception e){
          log("Unable to shut down eclimd (9091): " +
              e.getClass().getName() + " - " + e.getMessage());
        }
      }
    }catch(FileNotFoundException fnfe){
      log("Unable to locate eclimd instances file.");
    }finally{
      IOUtils.closeQuietly(reader);
    }
  }

  private void shutdown(int port)
    throws Exception
  {
    Socket socket = null;
    try{
      socket = new Socket("localhost", port);
      OutputStream out = socket.getOutputStream();
      out.write(nailgunPacket('A', "-command"));
      out.write(nailgunPacket('A', "shutdown"));
      out.write(nailgunPacket('C', "org.eclim.command.Main"));
      out.flush();
      Thread.sleep(WAIT_TIME);
    }finally{
      try{
        socket.close();
      }catch(IOException ioe){
        // ignore
      }
    }
  }

  private byte[] nailgunPacket(char type, String value)
  {
    int length = value.length();

    byte[] packet = new byte[5 + length];
    packet[0] = (byte)((length >> 24) & 0xff);
    packet[1] = (byte)((length >> 16) & 0xff);
    packet[2] = (byte)((length >> 8) & 0xff);
    packet[3] = (byte)(length & 0xff);
    packet[4] = (byte)type;
    System.arraycopy(value.getBytes(), 0, packet, 5, length);
    return packet;
  }
}
