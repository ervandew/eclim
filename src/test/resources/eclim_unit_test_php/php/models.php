<?php

function functionA () {
}

class TestA
{
  var $variable1 = 0;
  public function methodA1 ($str) {
    echo "<p>from methodA1</p>";
  }

  public function methodA2 () {
    echo "<p>from methodA2</p>";
  }
}

class TestB
{
  function methodB1 () {
    echo "<p>from methodB1</p>";
  }

  function methodB2 () {
    echo "<p>from methodB2</p>";
  }
}

define ('CONSTANT1', 'test constant');

?>
