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
package org.eclim.plugin.cdt.command.project;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.IPath;

/**
 * Command to obtain the current configuration (source entries, include
 * locations, etc.) for the specified project.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "c_project_configs",
  options = "REQUIRED p project ARG"
)
public class ConfigurationsCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);

    IProject project = ProjectUtils.getProject(projectName);
    ICProjectDescription desc =
      CCorePlugin.getDefault().getProjectDescription(project, false);
    ICConfigurationDescription[] cconfigs = desc.getConfigurations();

    ArrayList<HashMap<String,Object>> configs =
      new ArrayList<HashMap<String,Object>>();
    for(ICConfigurationDescription cconfig : cconfigs){
      HashMap<String,Object> cfg = new HashMap<String,Object>();
      configs.add(cfg);
      cfg.put("name", cconfig.getName());

      // source entries
      ICSourceEntry[] sources = cconfig.getSourceEntries();
      if (sources.length > 0){
        ArrayList<HashMap<String,Object>> srcs =
          new ArrayList<HashMap<String,Object>>();
        cfg.put("sources", srcs);

        for(ICSourceEntry entry : sources){
          HashMap<String,Object> src = new HashMap<String,Object>();
          srcs.add(src);
          String dirname = entry.getFullPath().removeFirstSegments(1).toString();
          if (dirname.length() == 0){
            dirname = "/";
          }
          src.put("dir", dirname);
          IPath[] excludes = entry.getExclusionPatterns();
          if (excludes.length > 0){
            ArrayList<String> ex = new ArrayList<String>();
            src.put("excludes", ex);
            for (int ii = 0; ii < excludes.length; ii++){
              ex.add(excludes[ii].toString());
            }
          }
        }
      }

      IConfiguration config =
        ManagedBuildManager.getConfigurationForDescription(cconfig);
      if (config == null){
        continue;
      }

      ITool[] tools = config.getTools();
      if(tools.length > 0){
        ArrayList<HashMap<String,Object>> tls =
          new ArrayList<HashMap<String,Object>>();
        cfg.put("tools", tls);
        for(ITool tool : tools){
          if (!tool.isEnabled() || !acceptTool(project, tool)){
            continue;
          }

          IOption ioption = getOptionByType(tool, IOption.INCLUDE_PATH);
          IOption soption = getOptionByType(tool, IOption.PREPROCESSOR_SYMBOLS);
          if (ioption == null && soption == null){
            continue;
          }

          HashMap<String,Object> tl = new HashMap<String,Object>();
          tls.add(tl);
          tl.put("name", tool.getName());

          // includes
          if(ioption != null){
            String[] includes = ioption.getIncludePaths();
            if(includes.length > 0){
              ArrayList<String> ins = new ArrayList<String>();
              tl.put("includes", ins);
              for(String include : includes){
                ins.add(include);
              }
            }
          }

          // symbols
          if(soption != null){
            String[] symbols = soption.getDefinedSymbols();
            if(symbols.length > 0){
              ArrayList<String> syms = new ArrayList<String>();
              tl.put("symbols", syms);
              for(String symbol : symbols){
                syms.add(symbol);
              }
            }
          }
        }
      }
    }

    return configs;
  }

  private boolean acceptTool(IProject project, ITool tool)
    throws Exception
  {
    switch (tool.getNatureFilter()) {
      case ITool.FILTER_C:
        if (project.hasNature(CProjectNature.C_NATURE_ID) &&
            !project.hasNature(CCProjectNature.CC_NATURE_ID))
        {
          return true;
        }
        break;
      case ITool.FILTER_CC:
        if (project.hasNature(CCProjectNature.CC_NATURE_ID)) {
          return true;
        }
        break;
      case ITool.FILTER_BOTH:
        return true;
    }
    return false;
  }

  private IOption getOptionByType(ITool tool, int type)
    throws Exception
  {
    IOption[] options = tool.getOptions();
    for(IOption option : options){
      if(option.getValueType() == type){
        return option;
      }
    }
    return null;
  }
}
