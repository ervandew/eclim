package org.eclim.test.refactoring.rename.vn1.vn2;

import org.eclim.test.refactoring.rename.vn1.TestN1VUnit;

import static org.eclim.test.refactoring.rename.vn1.TestN1VUnit.FOO;

public class TestN2VUnit
{
  public static final void test(String[] args)
  {
    System.out.println(FOO);
    TestN1VUnit test = new TestN1VUnit();
    test.testMethod();
  }
}
