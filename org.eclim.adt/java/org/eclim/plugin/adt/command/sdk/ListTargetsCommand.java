/**
 * Copyright (C) 2012 - 2017  Eric Van Dewoestine
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
package org.eclim.plugin.adt.command.sdk;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;

import org.eclim.plugin.core.command.AbstractCommand;

import com.android.ide.eclipse.adt.internal.sdk.Sdk;

import com.android.sdklib.IAndroidTarget;

/**
 * Command list the available android sdk platform targets.
 *
 * @author Eric Van Dewoestine
 */
@Command(name = "android_list_targets")
public class ListTargetsCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    ArrayList<HashMap<String,String>> results =
      new ArrayList<HashMap<String,String>>();
    Sdk sdk = Sdk.getCurrent();
    if (sdk == null){
      return "Android SDK not available.";
    }

    IAndroidTarget[] targets = sdk.getTargets();
    for (IAndroidTarget target : targets){
      if (!target.isPlatform()){
        continue;
      }
      HashMap<String,String> info = new HashMap<String,String>();
      info.put("name", target.getFullName());
      info.put("hash", target.hashString());
      info.put("api", target.getVersion().getApiString());
      results.add(info);
    }
    return results;
  }
}
