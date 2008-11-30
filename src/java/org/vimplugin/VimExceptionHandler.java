package org.vimplugin;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class VimExceptionHandler implements Thread.UncaughtExceptionHandler {

  /**
   * Handle Exceptions in a Thread like {@link VimConnection} right.
   */
  public void uncaughtException(Thread t, Throwable e) {
    //convert stacktrace to string
    String stacktrace;
    StringWriter sw = null;
    PrintWriter pw = null;
    try {
      sw = new StringWriter();
      pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      stacktrace = sw.toString();
    } finally {
      try {
        if (pw != null)
          pw.close();
        if (sw != null)
          sw.close();
      } catch (IOException ignore) {
      }
    }

    System.err.println("VimConnection: "+stacktrace);
  }
}
