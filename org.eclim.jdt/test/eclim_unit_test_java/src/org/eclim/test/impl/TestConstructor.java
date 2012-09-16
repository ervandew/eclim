package org.eclim.test.impl;

import java.util.Comparator;
import java.util.Set;

public class TestConstructor
  extends TestConstructorSuper
{
  private Set<String> names;

  private Comparator c = new Comparator(){

  };

  public class TestInner {
    private String subName;
  }
}
