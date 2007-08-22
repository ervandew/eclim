/**
 * Copyright (c) 2005 - 2007
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
package org.eclim.logging;

/**
 * Facade over the logger used by eclim.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class Logger
{
  private org.slf4j.Logger logger;

  private Logger (org.slf4j.Logger logger)
  {
    this.logger = logger;
  }

  /**
   * Gets the logger for the specified class.
   *
   * @param _class The class to get the Logger for.
   * @return The Logger instance.
   */
  public static Logger getLogger (Class _class)
  {
    return getLogger(_class.getName());
  }

  /**
   * Gets the logger with the specified name.
   *
   * @param _name The name of the Logger to get.
   * @return The Logger instance.
   */
  public static Logger getLogger (String _name)
  {
    return new Logger(org.slf4j.LoggerFactory.getLogger(_name));
  }

  /**
   * Log a debug message.
   *
   * @param message The message to log.
   * @param t The associated exception.
   */
  public void debug (String message, Throwable t)
  {
    logger.debug(message, t);
  }

  /**
   * Log a debug message.
   *
   * @param message The message to log.
   * @param args Optional arguments for the message.
   */
  public void debug (String message, Object... args)
  {
    logger.debug(message, args);
  }

  /**
   * Log an info message.
   *
   * @param message The message to log.
   * @param t The associated exception.
   */
  public void info (String message, Throwable t)
  {
    logger.info(message, t);
  }

  /**
   * Log an info message.
   *
   * @param message The message to log.
   * @param args Optional arguments for the message.
   */
  public void info (String message, Object... args)
  {
    logger.info(message, args);
  }

  /**
   * Log a warning message.
   *
   * @param message The message to log.
   * @param t The associated exception.
   */
  public void warn (String message, Throwable t)
  {
    logger.warn(message, t);
  }

  /**
   * Log a warning message.
   *
   * @param message The message to log.
   * @param args Optional arguments for the message.
   */
  public void warn (String message, Object... args)
  {
    logger.warn(message, args);
  }

  /**
   * Log an error message.
   *
   * @param message The message to log.
   * @param t The associated exception.
   */
  public void error (String message, Throwable t)
  {
    logger.error(message, t);
  }

  /**
   * Log an error message.
   *
   * @param message The message to log.
   * @param args Optional arguments for the message.
   */
  public void error (String message, Object... args)
  {
    logger.error(message, args);
  }

  /**
   * Determines if the debug level is enabled for this logger.
   *
   * @return true if debug enabled, false otherwise.
   */
  public boolean isDebugEnabled ()
  {
    return logger.isDebugEnabled();
  }

  /**
   * Determines if the info level is enabled for this logger.
   *
   * @return true if info enabled, false otherwise.
   */
  public boolean isInfoEnabled ()
  {
    return logger.isInfoEnabled();
  }

  /**
   * Determines if the warning level is enabled for this logger.
   *
   * @return true if warning enabled, false otherwise.
   */
  public boolean isWarnEnabled ()
  {
    return logger.isWarnEnabled();
  }

  /**
   * Determines if the error level is enabled for this logger.
   *
   * @return true if error enabled, false otherwise.
   */
  public boolean isErrorEnabled ()
  {
    return logger.isErrorEnabled();
  }
}
