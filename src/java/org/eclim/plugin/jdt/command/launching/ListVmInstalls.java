package org.eclim.plugin.jdt.command.launching;

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
  /**
   * {@inheritDoc}
   * @see org.eclim.command.Command#execute(CommandLine)
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    StringBuffer result = new StringBuffer();
    AbstractVMInstall defaultInstall =
      (AbstractVMInstall)JavaRuntime.getDefaultVMInstall();
    if (defaultInstall != null){
      result.append("default: ")
        .append(defaultInstall.getInstallLocation()).append('\n');
    }

    IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
    for (IVMInstallType type : types){
      IVMInstall[] installs = type.getVMInstalls();
      if (installs.length > 0){
        result.append("type: ").append(type.getName()).append('\n');
        for (IVMInstall iinstall : installs){
          AbstractVMInstall install = (AbstractVMInstall)iinstall;
          result.append("     name: ").append(install.getName()).append('\n');
          result.append("      dir: ")
            .append(install.getInstallLocation()).append('\n');
          result.append("  version: ")
            .append(install.getJavaVersion()).append('\n');
          String args = install.getVMArgs();
          if (args != null){
            result.append("     args: ").append(args).append('\n');
          }
        }
      }
    }
    return result.toString();
  }
}
