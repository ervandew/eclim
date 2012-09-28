package org.eclim.test.junit.run;

import static org.junit.Assert.*;

import org.junit.Test;

public class FooTest
{
  @Test
  public void foo()
  {
    Foo foo = new Foo();
    assertEquals("foo called with name: test", foo.foo("test"));
  }

  @Test
  public void fooString()
  {
    Foo foo = new Foo();
    assertEquals("foo called with name: test", foo.foo("test"));
  }

  @Test
  public void bar()
  {
    Foo foo = new Foo();
    assertEquals(42, foo.bar());
  }
}
