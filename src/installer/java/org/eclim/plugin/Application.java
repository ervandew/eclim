/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
 *
 * Portions of this class that are copied from the eclipse source are the
 * copyright (c) of IBM Corporation and others, and released under the Eclipse
 * Public License v1.0: http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclim.plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.net.URL;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;

import org.eclipse.equinox.app.IApplication;

import org.eclipse.equinox.internal.p2.console.ProvisioningHelper;

import org.eclipse.equinox.internal.p2.core.helpers.LogHelper;

import org.eclipse.equinox.internal.p2.director.app.Activator;
import org.eclipse.equinox.internal.p2.director.app.LatestIUVersionCollector;
import org.eclipse.equinox.internal.p2.director.app.Messages;

import org.eclipse.equinox.internal.provisional.p2.director.IPlanner;
import org.eclipse.equinox.internal.provisional.p2.director.ProfileChangeRequest;
import org.eclipse.equinox.internal.provisional.p2.director.ProvisioningPlan;

import org.eclipse.equinox.internal.provisional.p2.engine.DefaultPhaseSet;
import org.eclipse.equinox.internal.provisional.p2.engine.IEngine;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.internal.provisional.p2.engine.ProvisioningContext;

import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;

import org.eclipse.equinox.internal.provisional.p2.metadata.query.InstallableUnitQuery;

import org.eclipse.equinox.internal.provisional.p2.query.Collector;

import org.eclipse.osgi.service.resolver.VersionRange;

import org.eclipse.osgi.util.NLS;

import org.osgi.framework.Version;

/**
 * Entry point for installer application.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class Application
  extends org.eclipse.equinox.internal.p2.director.app.Application
{
  private Object invokePrivate (String methodName, Class[] params, Object[] args)
    throws Exception
  {
    Method method =
      org.eclipse.equinox.internal.p2.director.app.Application.class
      .getDeclaredMethod(methodName, params);
    method.setAccessible(true);
    return method.invoke(this, args);
  }

  private Object getPrivateField (String fieldName)
    throws Exception
  {
    Field thread =
      org.eclipse.equinox.internal.p2.director.app.Application.class
      .getDeclaredField(fieldName);
    thread.setAccessible(true);
    return thread.get(this);
  }

  // Copied directly from superclass. changes noted (should just be changes to
  // access private fields/methods).
  public Object run(String[] args) throws Exception {
    long time = -System.currentTimeMillis();
    // EV: invoke private method
    //initializeServices();
    this.invokePrivate("initializeServices", new Class[0], new Object[0]);
    processArguments(args);

    // EV: pull some private vars in to local scope. must be after
    // processArguments
    int command = ((Integer)this.getPrivateField("command")).intValue();
    String root = (String)this.getPrivateField("root");
    Version version = (Version)this.getPrivateField("version");
    URL[] metadataRepositoryLocations = (URL[])this.getPrivateField("metadataRepositoryLocations");

    IStatus operationStatus = Status.OK_STATUS;
    InstallableUnitQuery query;
    Collector roots;
    switch (command) {
      case COMMAND_INSTALL :
      case COMMAND_UNINSTALL :
        // EV: invoke private method
        //initializeRepositories(command == COMMAND_INSTALL);
        this.invokePrivate("initializeRepositories",
            new Class[]{Boolean.TYPE},
            new Object[]{Boolean.valueOf(command == COMMAND_INSTALL)});

        // EV: invoke private method
        //IProfile profile = initializeProfile();
        IProfile profile = (IProfile)this.invokePrivate(
            "initializeProfile", new Class[0], new Object[0]);
        query = new InstallableUnitQuery(root, version == null ? VersionRange.emptyRange : new VersionRange(version, true, version, true));
        // EV: print preperation info
        System.out.println("prepare: Getting Installable Units");
        roots = ProvisioningHelper.getInstallableUnits(null, query, new LatestIUVersionCollector(), new NullProgressMonitor());
        if (roots.size() <= 0)
          roots = profile.query(query, roots, new NullProgressMonitor());
        if (roots.size() <= 0) {
          LogHelper.log(new Status(IStatus.ERROR, Activator.ID, NLS.bind(Messages.Missing_IU, root)));
          // EV: throw exception for the installer
          //System.out.println(NLS.bind(Messages.Missing_IU, root));
          //return EXIT_ERROR;
          throw new RuntimeException(NLS.bind(Messages.Missing_IU, root));
        }
        // EV: invoke private method
        //if (!updateRoamingProperties(profile).isOK()) {
        if(!((IStatus)this.invokePrivate(
                "updateRoamingProperties",
                new Class[]{IProfile.class},
                new Object[]{profile})).isOK())
        {
          LogHelper.log(new Status(IStatus.ERROR, Activator.ID, NLS.bind(Messages.Cant_change_roaming, profile.getProfileId())));
          // EV: throw exception for the installer
          //System.out.println(NLS.bind(Messages.Cant_change_roaming, profile.getProfileId()));
          //return EXIT_ERROR;
          throw new RuntimeException(NLS.bind(Messages.Cant_change_roaming, profile.getProfileId()));
        }
        ProvisioningContext context = new ProvisioningContext();
        // EV: invoke private method
        //ProfileChangeRequest request = buildProvisioningRequest(profile, roots, command == COMMAND_INSTALL);
        ProfileChangeRequest request = (ProfileChangeRequest)this.invokePrivate(
            "buildProvisioningRequest",
            new Class[]{IProfile.class, Collector.class, Boolean.TYPE},
            new Object[]{profile, roots, command == COMMAND_INSTALL});
        // EV: invoke private method
        this.invokePrivate(
            "printRequest",
            new Class[]{ProfileChangeRequest.class},
            new Object[]{request});
        operationStatus = planAndExecute(profile, context, request);
        break;
      case COMMAND_LIST :
        query = new InstallableUnitQuery(null, VersionRange.emptyRange);
        if (metadataRepositoryLocations == null)
          // EV: invoke private method
          //missingArgument("metadataRepository"); //$NON-NLS-1$
          this.invokePrivate(
              "missingArgument",
              new Class[]{String.class},
              new Object[]{"metadataRepository"}); //$NON-NLS-1$

        for (int i = 0; i < metadataRepositoryLocations.length; i++) {
          roots = ProvisioningHelper.getInstallableUnits(metadataRepositoryLocations[i], query, new NullProgressMonitor());

          Iterator unitIterator = roots.iterator();
          while (unitIterator.hasNext()) {
            IInstallableUnit iu = (IInstallableUnit) unitIterator.next();
            System.out.println(iu.getId());
          }
        }
        break;
    }

    time += System.currentTimeMillis();
    if (operationStatus.isOK())
      System.out.println(NLS.bind(Messages.Operation_complete, new Long(time)));
    else {
      LogHelper.log(operationStatus);
      // EV: throw exception for the installer
      //System.out.println(Messages.Operation_failed);
      //return EXIT_ERROR;
      String workspace =
        ResourcesPlugin.getWorkspace().getRoot().getRawLocation().toOSString();
      String log = workspace + "/.metadata/.log";
      throw new RuntimeException(
          "Operation failed. See '" + log + "' for additional info.");
    }
    return IApplication.EXIT_OK;
  }

  // Copied directly from superclass. changes noted
  private IStatus planAndExecute(IProfile profile, ProvisioningContext context, ProfileChangeRequest request)
    throws Exception
  {
    // EV: pull some private vars in to local scope.
    IPlanner planner = (IPlanner)this.getPrivateField("planner");
    IEngine engine = (IEngine)this.getPrivateField("engine");

    ProvisioningPlan result;
    IStatus operationStatus;
    result = planner.getProvisioningPlan(request, context, new NullProgressMonitor());
    if (!result.getStatus().isOK())
      operationStatus = result.getStatus();
    else {
      // EV: plug in the eclim installer progress monitor
      //operationStatus = engine.perform(profile, new DefaultPhaseSet(), result.getOperands(), context, new NullProgressMonitor());
      operationStatus = engine.perform(
          profile, new DefaultPhaseSet(), result.getOperands(), context, new ProgressMonitor());
    }
    return operationStatus;
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
