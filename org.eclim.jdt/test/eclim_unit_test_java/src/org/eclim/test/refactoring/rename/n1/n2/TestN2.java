package org.eclim.test.refactoring.rename.n1.n2;

import static org.eclim.test.refactoring.rename.n1.TestN1.FOO;

import org.eclim.test.refactoring.rename.n1.TestN1;

public class TestN2
{
  public static final void test(String[] args)
  {
    System.out.println(FOO);
    TestN1 test = new TestN1();
    test.testMethod();
  }
}
