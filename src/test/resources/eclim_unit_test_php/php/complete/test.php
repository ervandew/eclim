<html>
  <body>
    <?php
    include 'models.php';

    echo "Hello World.";

    $testA = new TestA();
    $testA->methodA2();
    $testA->variable1;

    $testB = new TestB();
    $testB->methodB1();
    ?>
  </body>
</html>
