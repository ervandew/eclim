/**
 * Copyright (c) 2005 - 2006
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclim.plugin.wst;

import org.eclim.Services;

import org.eclim.plugin.AbstractPluginResources;

/**
 * Implementation of AbstractPluginResources.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
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
  public void initialize (String _name)
  {
    super.initialize(_name);
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
  protected String getBundleBaseName ()
  {
    return "org/eclim/plugin/wst/messages";
  }
}
