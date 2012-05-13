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

import java.io.File;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Feature
{
  public static final Pattern VERSION =
    Pattern.compile("^(\\d+\\.\\d+\\.\\d+)(\\..*)?");

  private String version;
  private File site;

  public Feature(String version, File site)
  {
    this.site = site;

    Matcher matcher = VERSION.matcher(version);
    matcher.find();
    this.version = matcher.group(1);
  }

  public String getVersion()
  {
    return version;
  }

  public File getSite()
  {
    return this.site;
  }
}
