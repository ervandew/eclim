/**
 * Copyright (C) 2005 - 2020  Eric Van Dewoestine
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
package org.eclim.logging;

import org.eclipse.core.runtime.Platform;

/**
 * Facade over the logger used by eclim.
 *
 * @author Eric Van Dewoestine
 */
public class Logger
{
  private static String workspace = Platform.getLocation().toOSString();
  static{
    // set on class load so that the logger can log to:
    // ${eclimd.workspace}/eclimd.log
    System.setProperty("eclimd.workspace", workspace);
  }

  private org.slf4j.Logger logger;

  private Logger(org.slf4j.Logger logger)
  {
    this.logger = logger;
  }

  /**
   * Gets the logger for the specified class.
   *
   * @param theClass The class to get the Logger for.
   * @return The Logger instance.
   */
  public static Logger getLogger(Class<?> theClass)
  {
    return getLogger(theClass.getName());
  }

  /**
   * Gets the logger with the specified name.
   *
   * @param name The name of the Logger to get.
   * @return The Logger instance.
   */
  public static Logger getLogger(String name)
  {
    return new Logger(org.slf4j.LoggerFactory.getLogger(name));
  }

  /**
   * Log a debug message.
   *
   * @param message The message to log.
   * @param t The associated exception.
   */
  public void debug(String message, Throwable t)
  {
    logger.debug(message, t);
  }

  /**
   * Log a debug message.
   *
   * @param message The message to log.
   * @param args Optional arguments for the message.
   */
  public void debug(String message, Object... args)
  {
    logger.debug(message, args);
  }

  /**
   * Log an info message.
   *
   * @param message The message to log.
   * @param t The associated exception.
   */
  public void info(String message, Throwable t)
  {
    logger.info(message, t);
  }

  /**
   * Log an info message.
   *
   * @param message The message to log.
   * @param args Optional arguments for the message.
   */
  public void info(String message, Object... args)
  {
    logger.info(message, args);
  }

  /**
   * Log a warning message.
   *
   * @param message The message to log.
   * @param t The associated exception.
   */
  public void warn(String message, Throwable t)
  {
    logger.warn(message, t);
  }

  /**
   * Log a warning message.
   *
   * @param message The message to log.
   * @param args Optional arguments for the message.
   */
  public void warn(String message, Object... args)
  {
    logger.warn(message, args);
  }

  /**
   * Log an error message.
   *
   * @param message The message to log.
   * @param t The associated exception.
   */
  public void error(String message, Throwable t)
  {
    logger.error(message, t);
  }

  /**
   * Log an error message.
   *
   * @param message The message to log.
   * @param args Optional arguments for the message.
   */
  public void error(String message, Object... args)
  {
    logger.error(message, args);
  }

  /**
   * Determines if the debug level is enabled for this logger.
   *
   * @return true if debug enabled, false otherwise.
   */
  public boolean isDebugEnabled()
  {
    return logger.isDebugEnabled();
  }

  /**
   * Determines if the info level is enabled for this logger.
   *
   * @return true if info enabled, false otherwise.
   */
  public boolean isInfoEnabled()
  {
    return logger.isInfoEnabled();
  }

  /**
   * Determines if the warning level is enabled for this logger.
   *
   * @return true if warning enabled, false otherwise.
   */
  public boolean isWarnEnabled()
  {
    return logger.isWarnEnabled();
  }

  /**
   * Determines if the error level is enabled for this logger.
   *
   * @return true if error enabled, false otherwise.
   */
  public boolean isErrorEnabled()
  {
    return logger.isErrorEnabled();
  }
}
