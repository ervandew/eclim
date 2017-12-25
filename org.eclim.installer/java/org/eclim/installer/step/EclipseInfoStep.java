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
package org.eclim.installer.step;

import java.util.Properties;

import org.formic.Installer;

import org.formic.wizard.step.gui.InstallStep;

import foxtrot.Worker;

/**
 * Step which installs the eclim installer feature and uses that to gather info
 * about the current eclipse install.
 *
 * @author Eric Van Dewoestine
 */
public class EclipseInfoStep
  extends InstallStep
{
  /**
   * Constructs this step.
   */
  public EclipseInfoStep(String name, Properties properties)
  {
    super(name, properties);
  }

  @Override
  public void displayed()
  {
    setBusy(true);
    setPreviousEnabled(false);

    EclipseInfo info = null;
    try{
      overallLabel.setText("");
      overallProgress.setValue(0);
      taskLabel.setText("");
      taskProgress.setValue(0);
      taskProgress.setIndeterminate(true);

      info = (EclipseInfo)Worker.post(new foxtrot.Task(){
        public Object run()
          throws Exception
        {
          overallLabel.setText(
            "Installing eclim installer feature (may take a few moments).");
          if (EclipseInfo.installInstallerPlugin()){
            overallProgress.setMaximum(2);
            overallProgress.setValue(1);
            overallLabel.setText("Analyzing installed features...");

            EclipseInfo info = EclipseInfo.gatherEclipseInfo();

            overallLabel.setText("Finished analyzing your eclipse installation.");
            overallProgress.setValue(2);
            taskProgress.setIndeterminate(false);
            taskProgress.setMaximum(1);
            taskProgress.setValue(1);

            return info;
          }
          return null;
        }
      });

      Installer.getContext().setValue("eclipse.info", info);
    }catch(Exception e){
      setError(e);
    }finally{
      setValid(info != null);
      setBusy(false);
      taskProgress.setIndeterminate(false);
    }
  }
}
