package org.eclim.plugin.core.command.file;

public class FileListCommandException extends Exception
{

  public FileListCommandException(String message, Exception exception)
  {
    super(message, exception);
  }

  public FileListCommandException(String message)
  {
    super(message);
  }
}
