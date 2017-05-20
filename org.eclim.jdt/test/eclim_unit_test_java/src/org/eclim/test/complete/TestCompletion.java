package org.eclim.test.complete;

import java.util.ArrayList;
import java.util.List;

public class TestCompletion
{
  public void test ()
  {
    List list = new ArrayList();
    list.
  }

  public void testAnother ()
  {
    List list = new ArrayList();
    list.a();
  }

  public void testMissingImport ()
  {
    List list = new ArrayList();
    ((Map)list.get(0)).p
  }

  public void testMissingImportStatic ()
  {
    Component.
  }

  public void testVisibilityOfProposal ()
  {
    ClassWithPrivateMethod c = new ClassWithPrivateMethod();
    c.p
  }
}

public class ClassWithPrivateMethod
{
  private void privateMethod() {}
  public void publicMethod() {}
}
