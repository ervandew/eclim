/**
 * Copyright (C) 2005 - 2009  Eric Van Dewoestine
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
package org.eclim.plugin.wst;

import org.eclim.Services;

import org.eclim.plugin.AbstractPluginResources;

/**
 * Implementation of AbstractPluginResources.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class PluginResources
  extends AbstractPluginResources
{
  /**
   * Name that can be used to lookup this PluginResources from
   * {@link Services#getPluginResources(String)}.
   */
  public static final String NAME = "org.eclim.wst";

  /**
   * {@inheritDoc}
   * @see AbstractPluginResources#initialize(String)
   */
  @Override
  public void initialize(String name)
  {
    super.initialize(name);
    registerCommand("css_complete",
        org.eclim.plugin.wst.command.complete.CssCodeCompleteCommand.class);
    registerCommand("html_complete",
        org.eclim.plugin.wst.command.complete.HtmlCodeCompleteCommand.class);
    registerCommand("javascript_complete",
        org.eclim.plugin.wst.command.complete.JavascriptCodeCompleteCommand.class);
    registerCommand("xml_complete",
        org.eclim.plugin.wst.command.complete.XmlCodeCompleteCommand.class);
    registerCommand("css_validate",
        org.eclim.plugin.wst.command.validate.CssValidateCommand.class);
    registerCommand("dtd_validate",
        org.eclim.plugin.wst.command.validate.DtdValidateCommand.class);
    registerCommand("html_validate",
        org.eclim.plugin.wst.command.validate.HtmlValidateCommand.class);
    registerCommand("wsdl_validate",
        org.eclim.plugin.wst.command.validate.WsdlValidateCommand.class);
    registerCommand("xsd_validate",
        org.eclim.plugin.wst.command.validate.XsdValidateCommand.class);
  }

  /**
   * {@inheritDoc}
   * @see AbstractPluginResources#getBundleBaseName()
   */
  protected String getBundleBaseName()
  {
    return "org/eclim/plugin/wst/messages";
  }
}
