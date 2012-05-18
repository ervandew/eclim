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
}
