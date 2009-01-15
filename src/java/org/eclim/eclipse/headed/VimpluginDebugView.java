package org.eclim.eclipse.headed;

import org.eclipse.swt.SWT;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import org.eclipse.ui.part.ViewPart;

/**
 * View used as a debug console for vipmlugin debugging output.
 *
 * @author Eric Van Dewoestine
 */
public class VimpluginDebugView
  extends ViewPart
{
  private static Text log;

  /**
   * {@inheritDoc}
   * @see ViewPart#createPartControl(Composite)
   */
  @Override
  public void createPartControl(Composite parent)
  {
    log = new Text(
        parent,
        SWT.LEFT | SWT.MULTI | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
  }

  /**
   * {@inheritDoc}
   * @see ViewPart#dispose()
   */
  @Override
  public void dispose()
  {
    log.dispose();
    log = null;
  }

  /**
   * {@inheritDoc}
   * @see ViewPart#setFocus()
   */
  @Override
  public void setFocus()
  {
    log.setFocus();
  }

  public static Text getLog()
  {
    return log;
  }
}
