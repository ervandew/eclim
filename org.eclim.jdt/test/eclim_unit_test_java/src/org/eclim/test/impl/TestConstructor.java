package org.eclim.test.impl;

import java.util.Comparator;
import java.util.Set;

public class TestConstructor
{
  private int id;
  private String name;

  private Comparator c = new Comparator(){

  };

  public class TestInner {
    private String subName;
  }
}
