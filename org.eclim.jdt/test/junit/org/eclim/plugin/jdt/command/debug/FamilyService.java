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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FamilyService
{
  private final ExecutorService execService;

  public FamilyService() {
    execService = Executors.newFixedThreadPool(2);
    System.out.println("Started family service");
  }

  public void submit(Runnable job) {
    execService.submit(job);
  }

  public void shutdown() {
    execService.shutdownNow();
    System.out.println("Stopped family service");
  }

  public void submitChore() {
    submit(new ForeverChore());
  }

  public static class ForeverChore implements Runnable {
    @Override
    public void run() {
      while (true) {
        try {
          System.out.println("Running chore ...");
          Thread.sleep(3 * 1000);
        } catch (InterruptedException e) {
          System.out.println("Done with chore ...");
          return;
        }
      }
    }
  }
}
