package eclim.test.complete

import eclim.test.{TestJava, TestScala}

class TestComplete {

  def testScala(){
    val test = new TestScala
    test.sc 
  }

  def testJava(){
    val test = new TestJava
    test.j 
  }
}
