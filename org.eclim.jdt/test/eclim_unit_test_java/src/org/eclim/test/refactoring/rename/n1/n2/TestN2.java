package org.eclim.test.refactoring.rename.n1.n2;

import org.eclim.test.refactoring.rename.n1.TestN1;

import static org.eclim.test.refactoring.rename.n1.TestN1.FOO;

public class TestN2
{
  public static final void test(String[] args)
  {
    System.out.println(FOO);
    TestN1 test = new TestN1();
    test.testMethod();
  }
}
