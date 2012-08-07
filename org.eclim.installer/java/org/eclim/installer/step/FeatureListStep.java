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
package org.eclim.installer.step;

import java.util.Properties;

/**
 * Override the formic provided FeatureListStep to prevent navigating back from
 * this point in the install process.
 *
 * @author Eric Van Dewoestine
 */
public class FeatureListStep
  extends org.formic.wizard.step.gui.FeatureListStep
{
  public FeatureListStep(String name, Properties properties)
  {
    super(name, properties);
    setPreviousEnabled(false);
  }
}
