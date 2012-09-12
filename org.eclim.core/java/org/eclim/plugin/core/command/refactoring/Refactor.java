package org.eclim.plugin.core.command.refactoring;

import org.eclipse.ltk.core.refactoring.Refactoring;

/**
 * Small wrapper around a Refactoring allowing a more descriptive name to be
 * used.
 *
 * @author Eric Van Dewoestine
 */
public class Refactor
{
  public String name;
  public Refactoring refactoring;

  public Refactor(String name, Refactoring refactoring)
  {
    this.name = name;
    this.refactoring = refactoring;
  }

  public Refactor(Refactoring refactoring)
  {
    this.name = refactoring.getName();
    this.refactoring = refactoring;
  }
}
