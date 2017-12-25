package org.eclim.plugin.jdt.command.launching;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclipse.jdt.launching.AbstractVMInstall;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * Command which lists the available jvm installs grouped by their install type.
 *
 * @author Eric Van Dewoestine
 */
@Command(name = "java_list_installs")
public class ListVmInstalls
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    ArrayList<HashMap<String,Object>> results =
      new ArrayList<HashMap<String,Object>>();

    AbstractVMInstall defaultInstall =
      (AbstractVMInstall)JavaRuntime.getDefaultVMInstall();

    IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
    for (IVMInstallType type : types){
      IVMInstall[] installs = type.getVMInstalls();
      if (installs.length > 0){
        for (IVMInstall iinstall : installs){
          AbstractVMInstall install = (AbstractVMInstall)iinstall;
          HashMap<String,Object> result = new HashMap<String,Object>();
          results.add(result);
          result.put("type", type.getName());
          result.put("name", install.getName());
          result.put("dir", install.getInstallLocation().getPath());
          result.put("version", install.getJavaVersion());
          result.put("args", install.getVMArgs());
          result.put("default", install.equals(defaultInstall));
        }
      }
    }
    return results;
  }
}
