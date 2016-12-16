package org.eclim.http;

public class InvalidCommandException extends CommandCallerException
{

  public InvalidCommandException(String message, Exception cause)
  {
    super(message, cause);
  }

  public InvalidCommandException(String message)
  {
    super(message);
  }
}
