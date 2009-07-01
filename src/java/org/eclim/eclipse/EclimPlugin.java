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
package org.eclim.eclipse;

import java.io.PrintWriter;
import java.io.StringWriter;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Plugin;

import org.eclipse.core.runtime.internal.adaptor.EclipseAdaptorMsg;
import org.eclipse.core.runtime.internal.adaptor.MessageHelper;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.osgi.service.resolver.ResolverError;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.osgi.service.resolver.VersionConstraint;

import org.eclipse.osgi.util.NLS;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.EclimShell;
import org.eclipse.swt.widgets.Shell;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The main plugin class.
 */
public class EclimPlugin
  extends Plugin
{
  //The shared instance.
  private static EclimPlugin plugin;

  private static Shell shell;

  private static final String FILE_PREFIX = "file:";
  private static final String PLUGIN_XML = "plugin.xml";

  /**
   * The constructor.
   */
  public EclimPlugin ()
  {
    plugin = this;
  }

  /**
   * This method is called upon plug-in activation
   *
   * @param context The bundle context.
   */
  public void start(BundleContext context)
    throws Exception
  {
    super.start(context);
    URL url = FileLocator.toFileURL(getBundle().getResource(PLUGIN_XML));
    String home = url.toString();
    home = home.substring(
        FILE_PREFIX.length(), home.length() - PLUGIN_XML.length());
    // handle windows edge case
    home = home.replaceFirst("^/([A-Za-z]:)", "$1");
    System.setProperty("eclim.home", home);
  }

  /**
   * This method is called when the plug-in is stopped
   *
   * @param context The bundle context.
   */
  public void stop(BundleContext context)
    throws Exception
  {
    super.stop(context);
    plugin = null;
  }

  /**
   * Returns the shared instance.
   */
  public static EclimPlugin getDefault()
  {
    return plugin;
  }

  /**
   * Gets the shell to use.
   *
   * @return The Shell.
   */
  public static Shell getShell()
  {
    if(shell == null){
      final Display display = Display.getDefault();
      final Shell[] result = new Shell[1];
      // obtaining via synExec required when running inside of headed eclipse.
      display.syncExec(new Runnable(){
        public void run()
        {
          result[0] = new EclimShell(display);
        }
      });
      shell = result[0];
    }
    return shell;
  }

  /**
   * Diagnose loading of the bundle with the supplied name (ex. org.eclim.core).
   *
   * Gleaned from org.eclipse.core.runtime.internal.adaptor.EclipseCommandProvider
   *
   * @param bundleName
   * @return
   */
  public String diagnose(String bundleName)
  {
    StringWriter out = new StringWriter();
    PrintWriter writer = new PrintWriter(out);

    BundleContext context = getDefault().getBundle().getBundleContext();
    ServiceReference platformAdminRef =
      context.getServiceReference(PlatformAdmin.class.getName());
    PlatformAdmin platformAdmin =
      (PlatformAdmin)context.getService(platformAdminRef);
    State state = platformAdmin.getState(false);

    BundleDescription bundle = null;
    BundleDescription[] allBundles = state.getBundles(bundleName);
    if (allBundles.length == 0){
      writer.println(NLS.bind(
            EclipseAdaptorMsg.ECLIPSE_CONSOLE_CANNOT_FIND_BUNDLE_ERROR, bundleName));
    }else{
      bundle = allBundles[0];

      VersionConstraint[] unsatisfied =
        platformAdmin.getStateHelper().getUnsatisfiedConstraints(bundle);
      ResolverError[] resolverErrors =
        platformAdmin.getState(false).getResolverErrors(bundle);

      for (int i = 0; i < resolverErrors.length; i++) {
        if ((resolverErrors[i].getType() &
              (ResolverError.MISSING_FRAGMENT_HOST |
               ResolverError.MISSING_GENERIC_CAPABILITY |
               ResolverError.MISSING_IMPORT_PACKAGE |
               ResolverError.MISSING_REQUIRE_BUNDLE)) != 0)
        {
          continue;
        }
        writer.print("  ");
        writer.println(resolverErrors[i].toString());
      }

      if (unsatisfied.length == 0 && resolverErrors.length == 0) {
        writer.print("  ");
        writer.println(EclipseAdaptorMsg.ECLIPSE_CONSOLE_NO_CONSTRAINTS);
      }
      if (unsatisfied.length > 0) {
        writer.print("  ");
        writer.println(EclipseAdaptorMsg.ECLIPSE_CONSOLE_DIRECT_CONSTRAINTS);
      }
      for (int i = 0; i < unsatisfied.length; i++) {
        writer.print("    ");
        writer.println(MessageHelper.getResolutionFailureMessage(unsatisfied[i]));
      }

      VersionConstraint[] unsatisfiedLeaves =
        platformAdmin.getStateHelper().getUnsatisfiedLeaves(
            new BundleDescription[] {bundle});
      boolean foundLeaf = false;
      for (int i = 0; i < unsatisfiedLeaves.length; i++) {
        if (unsatisfiedLeaves[i].getBundle() == bundle)
          continue;
        if (!foundLeaf) {
          foundLeaf = true;
          writer.print("  ");
          writer.println(EclipseAdaptorMsg.ECLIPSE_CONSOLE_LEAF_CONSTRAINTS);
        }
        writer.print("    ");
        writer.println(unsatisfiedLeaves[i].getBundle().getLocation() +
            " [" + unsatisfiedLeaves[i].getBundle().getBundleId() + "]");
        writer.print("      ");
        writer.println(MessageHelper.getResolutionFailureMessage(unsatisfiedLeaves[i]));
      }
    }
    return out.toString();
  }
}
