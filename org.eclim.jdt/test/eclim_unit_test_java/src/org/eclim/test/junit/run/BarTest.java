package org.eclim.test.junit.run;

import static org.junit.Assert.*;

import org.junit.Test;

public class BarTest
{
  @Test
  public void bar()
  {
    Bar bar = new Bar();
    assertEquals("bar called", bar.bar());
  }
}
