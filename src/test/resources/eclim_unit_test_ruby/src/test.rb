module TestModule
  ID = 12
  def TestModule.moduleMethodA()
    puts "moduleMethodA"
  end
  def methodA()
    puts "methodA"
  end
end

class TestClass
  CONSTANT = 3
  def testA()
    puts "testA"
  end
  def testB()
    puts "testB"
  end
end
