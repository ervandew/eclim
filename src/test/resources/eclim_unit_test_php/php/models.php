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

/**
 * @property mixed $regular regular read/write property
 * @property-read int $foo the foo prop
 * @property-write string $bar the bar prop
 */
class TestB
{
  private $_thingy;
  private $_bar;

  function methodB1 () {
    echo "<p>from methodB1</p>";
  }

  function methodB2 () {
    echo "<p>from methodB2</p>";
  }

  function __get($var)
  {
    switch ($var) {
      case 'foo' :
        return 45;
      case 'regular' :
        return $this->_thingy;
    }
  }

  function __set($var, $val)
  {
    switch ($var) {
      case 'bar' :
        $this->_bar = $val;
        break;
      case 'regular' :
        if (is_string($val)) {
          $this->_thingy = $val;
        }
    }
  }
}

define ('CONSTANT1', 'test constant');

?>
