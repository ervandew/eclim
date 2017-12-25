/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
 *
 * Portions of this class that are copied from the eclipse source are the
 * copyright (c) of IBM Corporation and others, and released under the Eclipse
 * Public License v1.0: http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclim.installer.eclipse;

import java.io.PrintStream;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.net.URI;

import java.util.Collection;
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

import org.eclipse.equinox.internal.p2.director.ProfileChangeRequest;

import org.eclipse.equinox.internal.p2.director.app.Messages;

//import org.eclipse.equinox.internal.p2.ui.ProvUI;

import org.eclipse.equinox.internal.provisional.p2.director.PlanExecutionHelper;

import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.ProvisionException;

import org.eclipse.equinox.p2.engine.IEngine;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProvisioningPlan;
import org.eclipse.equinox.p2.engine.ProvisioningContext;

import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.IVersionedId;

import org.eclipse.equinox.p2.planner.IPlanner;

/*import org.eclipse.equinox.p2.repository.IRepositoryManager;

import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;

import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;

import org.eclipse.equinox.p2.ui.ProvisioningUI;*/

import org.eclipse.osgi.util.NLS;

import org.eclipse.update.internal.configurator.FeatureEntry;

import org.osgi.framework.ServiceReference;

/**
 * Entry point for installer application.
 *
 * @author Eric Van Dewoestine
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class Application
  extends org.eclipse.equinox.internal.p2.director.app.DirectorApplication
{
  private static final Integer EXIT_ERROR = new Integer(13);

  private Object invokePrivate(String methodName, Class[] params, Object[] args)
    throws Exception
  {
    Method method =
      org.eclipse.equinox.internal.p2.director.app.DirectorApplication.class
      .getDeclaredMethod(methodName, params);
    method.setAccessible(true);
    return method.invoke(this, args);
  }

  private Object getPrivateField(String fieldName)
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
  {
    // get eclipse information:
    //   - profile name
    //   - configuration location (user local dir for root installed eclipse)
    //   - least of installed features
    if ("-info".equals(args[0])){
      System.out.println("  Profile: " +
          System.getProperty("eclipse.p2.profile"));
      System.out.println("  Configuration: " +
          System.getProperty("osgi.configuration.area"));
      IBundleGroupProvider[] providers = Platform.getBundleGroupProviders();
      for(IBundleGroupProvider provider : providers){
        IBundleGroup[] groups = provider.getBundleGroups();
        for(IBundleGroup group : groups){
          FeatureEntry feature = (FeatureEntry)group;
          System.out.println("  Feature: " +
              feature.getIdentifier() + ' ' +
              feature.getVersion() + ' ' +
              feature.getSite().getResolvedURL());
        }
      }
      return IApplication.EXIT_OK;
    }

    // remove temp p2 repositories
    // FIXME: after removing the repositories the changes don't seem to be
    // committed. Either some event listener is registered or the application
    // exists before the changes can be committed. Need to find out how to wait
    // or force a synchronous saving of the changes. Also, the
    // getKnownRepositories seems to not return the uri of the current update
    // site, perhaps making this those block moot.
    /*if ("-removeRepos".equals(args[0])){
      // see
      //   org.eclipse.equinox.p2.ui.RepositoryManipulationPage
      //   org.eclipse.equinox.internal.p2.ui.model.ElementUtils

      ProvisioningUI ui = ProvisioningUI.getDefaultUI();
      ui.signalRepositoryOperationStart();
      IMetadataRepositoryManager metaManager =
        ProvUI.getMetadataRepositoryManager(ui.getSession());
      IArtifactRepositoryManager artManager =
        ProvUI.getArtifactRepositoryManager(ui.getSession());
      URI[] repos = metaManager.getKnownRepositories(
          IRepositoryManager.REPOSITORIES_ALL);
      for (URI repo : repos){
        if (repo.toString().indexOf("formic_") != -1){
          System.out.println("Remove repository: " + repo);
          metaManager.removeRepository(repo);
          artManager.removeRepository(repo);
        }
      }
      ui.signalRepositoryOperationComplete(null, true);

      return IApplication.EXIT_OK;
    }*/

    long time = System.currentTimeMillis();

    try {
      processArguments(args);
      // EV: pull some private vars in to local scope. must be after
      // processArguments
      boolean printHelpInfo = ((Boolean)this.getPrivateField("printHelpInfo")).booleanValue();
      boolean printIUList = ((Boolean)this.getPrivateField("printIUList")).booleanValue();
      boolean printRootIUList = ((Boolean)this.getPrivateField("printRootIUList")).booleanValue();
      boolean printTags = ((Boolean)this.getPrivateField("printTags")).booleanValue();
      boolean purgeRegistry = ((Boolean)this.getPrivateField("purgeRegistry")).booleanValue();

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
        String NOTHING_TO_REVERT_TO = (String)this.getPrivateField("NOTHING_TO_REVERT_TO");
        String revertToPreviousState = (String)this.getPrivateField("revertToPreviousState");
        List<IVersionedId> rootsToInstall = (List<IVersionedId>)this.getPrivateField("rootsToInstall");
        List<IVersionedId> rootsToUninstall = (List<IVersionedId>)this.getPrivateField("rootsToUninstall");

        if (revertToPreviousState != NOTHING_TO_REVERT_TO) {
          // EV: invoke private methods
          //revertToPreviousState();
          invokePrivate("revertToPreviousState", new Class[0], new Object[0]);
        } else if (!(rootsToInstall.isEmpty() && rootsToUninstall.isEmpty()))
          performProvisioningActions();
        if (printIUList)
          // EV: invoke private method
          //performList();
          invokePrivate("performList", new Class[0], new Object[0]);
        if (printRootIUList)
          // EV: invoke private method
          //performListInstalledRoots();
          invokePrivate("performListInstalledRoots", new Class[0], new Object[0]);
        if (printTags)
          // EV: invoke private method
          //performPrintTags();
          invokePrivate("performPrintTags", new Class[0], new Object[0]);
        if (purgeRegistry)
          // EV: invoke private method
          //purgeRegistry();
          invokePrivate("purgeRegistry", new Class[0], new Object[0]);

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
        invokePrivate("logFailure",
            new Class[]{IStatus.class},
            new Object[]{e.getStatus()});

        //set empty exit data to suppress error dialog from launcher
        // EV: invoke private methods
        //setSystemProperty("eclipse.exitdata", ""); //$NON-NLS-1$ //$NON-NLS-2$
        invokePrivate("setSystemProperty",
            new Class[]{String.class, String.class},
            new Object[]{"eclipse.exitdata", ""});

        // EV: throw exception for the installer
        //return EXIT_ERROR;
        String workspace =
          ResourcesPlugin.getWorkspace().getRoot().getRawLocation().toOSString();
        String log = workspace + "/.metadata/.log";
        throw new RuntimeException(
            "Operation failed. See '" + log + "' for additional info.", e);
      }catch(Exception ex){
        ex.printStackTrace();
        return EXIT_ERROR;
      }

    // EV: handle reflection exceptions
    } catch(Exception e){
      e.printStackTrace();
      return EXIT_ERROR;
    } finally {
      try{
        // EV: pull private var in.
        ServiceReference packageAdminRef = (ServiceReference)
          this.getPrivateField("packageAdminRef");
        if (packageAdminRef != null) {
          // EV: invoke private methods.
          //cleanupRepositories();
          //cleanupServices();
          invokePrivate("cleanupRepositories", new Class[0], new Object[0]);
          invokePrivate("cleanupServices", new Class[0], new Object[0]);
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
    List<IVersionedId> rootsToInstall = (List<IVersionedId>)this.getPrivateField("rootsToInstall");
    List<IVersionedId> rootsToUninstall = (List<IVersionedId>)this.getPrivateField("rootsToUninstall");

    // EV: invoke private methods
    //IProfile profile = initializeProfile();
    //Collection<IInstallableUnit> installs = collectRoots(profile, rootsToInstall, true);
    //Collection<IInstallableUnit> uninstalls = collectRoots(profile, rootsToUninstall, false);
    IProfile profile = (IProfile)
      invokePrivate("initializeProfile", new Class[0], new Object[0]);
    Collection<IInstallableUnit> installs = (Collection<IInstallableUnit>)
      invokePrivate("collectRoots",
          new Class[]{IProfile.class, List.class, Boolean.TYPE},
          new Object[]{profile, rootsToInstall, true});
    Collection<IInstallableUnit> uninstalls = (Collection<IInstallableUnit>)
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
      IProvisioningAgent targetAgent = (IProvisioningAgent)this.getPrivateField("targetAgent");
      List<URI> metadataRepositoryLocations = (List<URI>)this.getPrivateField("metadataRepositoryLocations");
      List<URI> artifactRepositoryLocations = (List<URI>)this.getPrivateField("artifactRepositoryLocations");
      boolean followReferences = ((Boolean)this.getPrivateField("followReferences")).booleanValue();
      String FOLLOW_ARTIFACT_REPOSITORY_REFERENCES = (String)this.getPrivateField("FOLLOW_ARTIFACT_REPOSITORY_REFERENCES");

      ProvisioningContext context = new ProvisioningContext(targetAgent);
      context.setMetadataRepositories(metadataRepositoryLocations.toArray(new URI[metadataRepositoryLocations.size()]));
      context.setArtifactRepositories(artifactRepositoryLocations.toArray(new URI[artifactRepositoryLocations.size()]));
      context.setProperty(ProvisioningContext.FOLLOW_REPOSITORY_REFERENCES, String.valueOf(followReferences));
      context.setProperty(FOLLOW_ARTIFACT_REPOSITORY_REFERENCES, String.valueOf(followReferences));

      // EV: invoke private methods
      //ProfileChangeRequest request = buildProvisioningRequest(profile, installs, uninstalls);
      //printRequest(request);
      ProfileChangeRequest request = (ProfileChangeRequest)
        invokePrivate("buildProvisioningRequest",
            new Class[]{IProfile.class, Collection.class, Collection.class},
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

    IProvisioningPlan result = planner.getProvisioningPlan(
        request, context, new NullProgressMonitor());
    IStatus operationStatus = result.getStatus();
    if (!operationStatus.isOK())
      throw new CoreException(operationStatus);
    executePlan(context, result);
  }

  private void executePlan(ProvisioningContext context, IProvisioningPlan result)
    // EV: throw a regular Exception to account for reflection exceptions
    //throws CoreException
    throws Exception
  {
    // EV: pull some private vars in to local scope.
    IEngine engine = (IEngine)this.getPrivateField("engine");
    boolean verifyOnly = ((Boolean)this.getPrivateField("verifyOnly")).booleanValue();
    boolean noArtifactRepositorySpecified = ((Boolean)this.getPrivateField("noArtifactRepositorySpecified")).booleanValue();

    IStatus operationStatus;
    if (!verifyOnly) {
      // EV: plug in the eclim installer progress monitor
      //operationStatus = PlanExecutionHelper.executePlan(result, engine, context, new NullProgressMonitor());
      operationStatus = PlanExecutionHelper.executePlan(result, engine, context, new ProgressMonitor());
      if (!operationStatus.isOK()) {
        // EV: invoke private method
        //if (noArtifactRepositorySpecified && hasNoRepositoryFound(operationStatus))
        boolean hasNoRepositoryFound = ((Boolean)invokePrivate(
              "hasNoRepositoryFound",
              new Class[]{IStatus.class},
              new Object[]{operationStatus})).booleanValue();
        if (noArtifactRepositorySpecified && hasNoRepositoryFound)
          throw new ProvisionException(Messages.Application_NoRepositories);
        throw new CoreException(operationStatus);
      }
    }
  }

  private static class ProgressMonitor
    implements IProgressMonitor
  {
    private double totalWorked;
    private boolean canceled;

    @Override
    public void beginTask(String name, int totalWork)
    {
      System.out.println("beginTask: totalWork=" + totalWork + " name=" + name);
    }

    @Override
    public void done()
    {
      System.out.println("done");
    }

    @Override
    public void internalWorked(double work)
    {
      totalWorked += work;
      System.out.println("internalWorked: " + totalWorked);
    }

    @Override
    public boolean isCanceled()
    {
      return canceled;
    }

    @Override
    public void setCanceled(boolean canceled)
    {
      this.canceled = canceled;
    }

    @Override
    public void setTaskName(String name)
    {
      System.out.println("setTaskName: " + name);
    }

    @Override
    public void subTask(String name)
    {
      if (name != null && !name.trim().equals(StringUtils.EMPTY)){
        System.out.println("subTask: " + name);
      }
    }

    @Override
    public void worked(int work)
    {
      totalWorked += work;
      System.out.println("worked: " + totalWorked);
    }
  }
}
