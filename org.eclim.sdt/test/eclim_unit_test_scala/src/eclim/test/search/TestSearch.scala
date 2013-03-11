package eclim.test.search

import eclim.test.{TestJava, TestScala}

class TestComplete {

  def testScala(){
    val test = new TestScala
    test.scalaMethod1
  }

  def testJava(){
    val test = new TestJava
    test.javaMethod1
  }
}
