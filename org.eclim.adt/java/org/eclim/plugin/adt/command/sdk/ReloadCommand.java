/**
 * Copyright (C) 2012  Eric Van Dewoestine
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

import java.lang.reflect.Field;

import java.text.MessageFormat;

import java.util.HashMap;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;

import org.eclim.logging.Logger;

import org.eclim.plugin.core.command.AbstractCommand;

import com.android.ide.eclipse.adt.internal.sdk.Sdk;

import com.android.sdklib.ISdkLog;
import com.android.sdklib.SdkManager;

/**
 * Command to reload the android sdk environment.
 *
 * @author Eric Van Dewoestine
 */
@Command(name = "android_reload")
public class ReloadCommand
  extends AbstractCommand
  implements ISdkLog
{
  private static final Logger logger = Logger.getLogger(ReloadCommand.class);

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    HashMap<String,String> result = new HashMap<String,String>();
    Sdk sdk = Sdk.getCurrent();
    if (sdk == null){
      result.put("error", "Android SDK not available.");
    }else{
      Field mManager = sdk.getClass().getDeclaredField("mManager");
      mManager.setAccessible(true);
      ((SdkManager)mManager.get(sdk)).reloadSdk(this);
      result.put("message", "Android SDK Reloaded");
    }

    return result;
  }

  @Override
  public void warning(String message, Object... args)
  {
    logger.warn(MessageFormat.format(message, args));
  }

  @Override
  public void error(Throwable throwable, String message, Object... args)
  {
    throw new RuntimeException(MessageFormat.format(message, args), throwable);
  }

  @Override
  public void printf(String message, Object... args)
  {
    logger.info(MessageFormat.format(message, args));
  }
}
