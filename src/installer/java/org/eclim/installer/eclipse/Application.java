/**
 * Copyright (C) 2005 - 2010  Eric Van Dewoestine
 *
 * Portions of this class that are copied from the eclipse source are the
 * copyright (c) of IBM Corporation and others, and released under the Eclipse
 * Public License v1.0: http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclim.installer.eclipse;

import java.io.File;
import java.io.PrintStream;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.net.URI;
import java.net.URL;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;

import org.eclipse.equinox.app.IApplication;

import org.eclipse.equinox.internal.p2.director.app.Messages;

import org.eclipse.equinox.internal.provisional.p2.director.IPlanner;
import org.eclipse.equinox.internal.provisional.p2.director.PlanExecutionHelper;
import org.eclipse.equinox.internal.provisional.p2.director.ProfileChangeRequest;
import org.eclipse.equinox.internal.provisional.p2.director.ProvisioningPlan;

import org.eclipse.equinox.internal.provisional.p2.engine.IEngine;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.internal.provisional.p2.engine.ProvisioningContext;

import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;

import org.eclipse.osgi.util.NLS;

import org.osgi.framework.ServiceReference;

/**
 * Entry point for installer application.
 *
 * @author Eric Van Dewoestine
 */
@SuppressWarnings("unchecked")
public class Application
  extends org.eclipse.equinox.internal.p2.director.app.DirectorApplication
{
  private Object invokePrivate(String methodName, Class[] params, Object[] args)
    throws Exception
  {
    Method method =
      org.eclipse.equinox.internal.p2.director.app.DirectorApplication.class
      .getDeclaredMethod(methodName, params);
    method.setAccessible(true);
    return method.invoke(this, args);
  }

  private Object getPrivateField (String fieldName)
    throws Exception
  {
    Field field =
      org.eclipse.equinox.internal.p2.director.app.DirectorApplication.class
      .getDeclaredField(fieldName);
    field.setAccessible(true);
    return field.get(this);
  }

  // EV: straight copy from super w/ private field/method workarounds and
  // instead of returning EXIT_ERROR on error, throw an exception instead.
  public Object run(String[] args)
    throws CoreException
  {
    if ("-list".equals(args[0])){
      IBundleGroupProvider[] providers = Platform.getBundleGroupProviders();
      for(int ii = 0; ii < providers.length; ii++){
        System.out.println("Site: " + providers[ii].getName());
        IBundleGroup[] groups = providers[ii].getBundleGroups();
        for(int jj = 0; jj < groups.length; jj++){
          System.out.println("  Feature: " + groups[jj].getIdentifier() + ' ' + groups[jj].getVersion());
        }
      }
      return IApplication.EXIT_OK;
    }

    long time = System.currentTimeMillis();

    try {
      processArguments(args);
      // EV: pull some private vars in to local scope. must be after
      // processArguments
      boolean printHelpInfo = ((Boolean)this.getPrivateField("printHelpInfo")).booleanValue();
      boolean printIUList = ((Boolean)this.getPrivateField("printIUList")).booleanValue();

      if (printHelpInfo){
        // EV: invoke private method
        //performHelpInfo();
        invokePrivate("performHelpInfo", new Class[0], new Object[0]);
      } else {
        // EV: invoke private methods
        //initializeServices();
        //initializeRepositories();
        invokePrivate("initializeServices", new Class[0], new Object[0]);
        invokePrivate("initializeRepositories", new Class[0], new Object[0]);

        // EV: pull more private vars in.
        List rootsToInstall = (List)this.getPrivateField("rootsToInstall");
        List rootsToUninstall = (List)this.getPrivateField("rootsToUninstall");

        if (!(rootsToInstall.isEmpty() && rootsToUninstall.isEmpty()))
          performProvisioningActions();
        if (printIUList)
          // EV: invoke private method
          //performList();
          invokePrivate("performList", new Class[0], new Object[0]);

        System.out.println(NLS.bind(
              Messages.Operation_complete,
              new Long(System.currentTimeMillis() - time)));
      }
      return IApplication.EXIT_OK;
    } catch (CoreException e) {
      try{
        // EV: invoke private methods
        //deeplyPrint(e.getStatus(), System.err, 0);
        //logFailure(e.getStatus());
        invokePrivate("deeplyPrint",
            new Class[]{IStatus.class, PrintStream.class, Integer.TYPE},
            new Object[]{e.getStatus(), System.err, 0});
        invokePrivate("deeplyPrint",
            new Class[]{IStatus.class},
            new Object[]{e.getStatus()});

        //set empty exit data to suppress error dialog from launcher
        // EV: invoke private methods
        //setSystemProperty("eclipse.exitdata", ""); //$NON-NLS-1$ //$NON-NLS-2$
        invokePrivate("setSystemProperty",
            new Class[]{String.class, String.class},
            new Object[]{"eclipse.exitdata", ""});
      }catch(Exception ex){
        e.printStackTrace();
      }

      // EV: throw exception for the installer
      //return EXIT_ERROR;
      String workspace =
        ResourcesPlugin.getWorkspace().getRoot().getRawLocation().toOSString();
      String log = workspace + "/.metadata/.log";
      throw new RuntimeException(
          "Operation failed. See '" + log + "' for additional info.", e);

    // EV: handle reflection exceptions
    } catch(Exception e){
      String workspace =
        ResourcesPlugin.getWorkspace().getRoot().getRawLocation().toOSString();
      String log = workspace + "/.metadata/.log";
      throw new RuntimeException(
          "Operation failed. See '" + log + "' for additional info.", e);
    } finally {
      try{
        // EV: pull private var in.
        ServiceReference packageAdminRef = (ServiceReference)
          this.getPrivateField("packageAdminRef");
        if (packageAdminRef != null) {
          // EV: invoke private methods.
          //cleanupRepositories();
          //restoreServices();
          invokePrivate("cleanupRepositories", new Class[0], new Object[0]);
          invokePrivate("restoreServices", new Class[0], new Object[0]);
        }
      }catch(Exception e){
        e.printStackTrace();
      }
    }
  }

  // EV: straight copy from super w/ private field/method workarounds.
  private void performProvisioningActions()
    // EV: throw a regular Exception to account for reflection exceptions
    //throws CoreException
    throws Exception
  {
    // EV: pull private vars in.
    List rootsToInstall = (List)this.getPrivateField("rootsToInstall");
    List rootsToUninstall = (List)this.getPrivateField("rootsToUninstall");

    // EV: invoke private methods
    //IProfile profile = initializeProfile();
    //IInstallableUnit[] installs = collectRoots(profile, rootsToInstall, true);
    //IInstallableUnit[] uninstalls = collectRoots(profile, rootsToUninstall, false);
    IProfile profile = (IProfile)
      invokePrivate("initializeProfile", new Class[0], new Object[0]);
    IInstallableUnit[] installs = (IInstallableUnit[])
      invokePrivate("collectRoots",
          new Class[]{IProfile.class, List.class, Boolean.TYPE},
          new Object[]{profile, rootsToInstall, true});
    IInstallableUnit[] uninstalls = (IInstallableUnit[])
      invokePrivate("collectRoots",
          new Class[]{IProfile.class, List.class, Boolean.TYPE},
          new Object[]{profile, rootsToUninstall, false});

    // keep this result status in case there is a problem so we can report it to the user
    boolean wasRoaming = Boolean.valueOf(profile.getProperty(IProfile.PROP_ROAMING)).booleanValue();
    try {
      // EV: invoke private methods
      //updateRoamingProperties(profile);
      invokePrivate("updateRoamingProperties",
          new Class[]{IProfile.class}, new Object[]{profile});

      // EV: pull in private fields
      List metadataRepositoryLocations = (List)this.getPrivateField("metadataRepositoryLocations");
      List artifactRepositoryLocations = (List)this.getPrivateField("artifactRepositoryLocations");

      ProvisioningContext context = new ProvisioningContext((URI[])
          metadataRepositoryLocations.toArray(new URI[metadataRepositoryLocations.size()]));
      context.setArtifactRepositories((URI[])
          artifactRepositoryLocations.toArray(new URI[artifactRepositoryLocations.size()]));

      // EV: invoke private methods
      //ProfileChangeRequest request = buildProvisioningRequest(profile, installs, uninstalls);
      //printRequest(request);
      ProfileChangeRequest request = (ProfileChangeRequest)
        invokePrivate("buildProvisioningRequest",
            new Class[]{IProfile.class, IInstallableUnit[].class, IInstallableUnit[].class},
            new Object[]{profile, installs, uninstalls});
      invokePrivate(
          "printRequest",
          new Class[]{ProfileChangeRequest.class},
          new Object[]{request});

      planAndExecute(profile, context, request);
    } finally {
      // if we were originally were set to be roaming and we changed it, change it back before we return
      if (wasRoaming && !Boolean.valueOf(profile.getProperty(IProfile.PROP_ROAMING)).booleanValue())
        // EV: invoke private method
        //setRoaming(profile);
        invokePrivate("setRoaming", new Class[]{IProfile.class}, new Object[]{profile});
    }
  }

  private void planAndExecute(
      IProfile profile, ProvisioningContext context, ProfileChangeRequest request)
    // EV: throw a regular Exception to account for reflection exceptions
    //throws CoreException
    throws Exception
  {
    // EV: pull some private vars in to local scope.
    IPlanner planner = (IPlanner)this.getPrivateField("planner");
    IEngine engine = (IEngine)this.getPrivateField("engine");
    boolean verifyOnly = ((Boolean)this.getPrivateField("verifyOnly")).booleanValue();

    ProvisioningPlan result = planner.getProvisioningPlan(
        request, context, new NullProgressMonitor());
    IStatus operationStatus = result.getStatus();
    if (!operationStatus.isOK())
      throw new CoreException(operationStatus);
    if (!verifyOnly) {
      // EV: plug in the eclim installer progress monitor
      //operationStatus = PlanExecutionHelper.executePlan(
      //    result, engine, context, new NullProgressMonitor());
      operationStatus = PlanExecutionHelper.executePlan(
          result, engine, context, new ProgressMonitor());
      if (!operationStatus.isOK())
        throw new CoreException(operationStatus);
    }
  }

  private static class ProgressMonitor
    implements IProgressMonitor
  {
    private double totalWorked;
    private boolean canceled;

    /**
     * {@inheritDoc}
     * @see IProgressMonitor#beginTask(String,int)
     */
    public void beginTask (String name, int totalWork)
    {
      System.out.println("beginTask: totalWork=" + totalWork + " name=" + name);
    }

    /**
     * {@inheritDoc}
     * @see IProgressMonitor#done()
     */
    public void done ()
    {
      System.out.println("done");
    }

    /**
     * {@inheritDoc}
     * @see IProgressMonitor#internalWorked(double)
     */
    public void internalWorked (double work)
    {
      totalWorked += work;
      System.out.println("internalWorked: " + totalWorked);
    }

    /**
     * {@inheritDoc}
     * @see IProgressMonitor#isCanceled()
     */
    public boolean isCanceled ()
    {
      return canceled;
    }

    /**
     * {@inheritDoc}
     * @see IProgressMonitor#setCanceled(boolean)
     */
    public void setCanceled (boolean canceled)
    {
      this.canceled = canceled;
    }

    /**
     * {@inheritDoc}
     * @see IProgressMonitor#setTaskName(String)
     */
    public void setTaskName (String name)
    {
      System.out.println("setTaskName: " + name);
    }

    /**
     * {@inheritDoc}
     * @see IProgressMonitor#subTask(String)
     */
    public void subTask (String name)
    {
      if (name != null && !name.trim().equals(StringUtils.EMPTY)){
        System.out.println("subTask: " + name);
      }
    }

    /**
     * {@inheritDoc}
     * @see IProgressMonitor#worked(int)
     */
    public void worked (int work)
    {
      totalWorked += work;
      System.out.println("worked: " + totalWorked);
    }
  }
}
