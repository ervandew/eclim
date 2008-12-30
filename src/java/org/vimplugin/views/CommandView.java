/*
 * Vimplugin
 *
 * Copyright (c) 2007 by The Vimplugin Project.
 *
 * Released under the GNU General Public License
 * with ABSOLUTELY NO WARRANTY.
 *
 * See the file COPYING for more information.
 */
package org.vimplugin.views;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.Arrays;

import org.eclim.logging.Logger;

import org.eclipse.swt.SWT;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import org.eclipse.ui.part.ViewPart;

import org.vimplugin.VimPlugin;

/**
 * A very simple command launcher for both the netbeans protocol (start command
 * with nb: then) and "vim --remote-send" commands (start command with "vr:"
 * then). Examples:
 * <pre>
 * vr: iHallo Welt<ESC>
 * </pre>
 */
public class CommandView extends ViewPart {
  private static final Logger logger = Logger.getLogger(CommandView.class);

  private Text input;

  @Override
  public void createPartControl(Composite parent) {
    input = new Text(parent, SWT.MULTI);
    input.addListener(SWT.KeyDown, new Listener() {

      //TODO: cleanup launching of commands inside vim.
      public void handleEvent(Event e) {
        if (e.character == 0x0D) {
          String line1 = input.getText().substring(3);
          if (input.getText().startsWith("vr:")) {
            //TODO set and lookup --servername from VimServer instance.
            String[] args = {
              "vim", "--servername", "GVIM", "--remote-send", line1
            };
            try {
              logger.debug("running: " + Arrays.toString(args));
              Process process = new ProcessBuilder(args).start();

              InputStream is = process.getInputStream();
              InputStreamReader isr = new InputStreamReader(is);
              BufferedReader br = new BufferedReader(isr);
              String line;

              while ((line = br.readLine()) != null) {
                logger.debug(line);
              }
              process.waitFor();

              logger.debug("result: " + process.exitValue());
            } catch (IOException ioe) {
              // FIXME: open error dialog
              logger.error("Error sending command.", ioe);
            } catch (InterruptedException ie) {
              // FIXME: open error dialog
              logger.error("Error sending command.", ie);
            }
          } else if (input.getText().startsWith("nb:")) {
            VimPlugin.getDefault().getVimserver(0).getVc().plain(line1);
          }

          input.setText("");
        }
      }
    });
  }

  @Override
  public void setFocus() {
    input.setFocus();
  }
}
