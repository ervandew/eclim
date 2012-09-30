package org.eclim.test;

public class TestMain
{
  public static final void main(String[] args)
  {
    /*System.out.print("Enter your name: ");
    java.util.Scanner scanner = new java.util.Scanner(System.in);
    String name = scanner.nextLine();
    System.out.println("Hello " + name);*/

    System.out.println("Hello Unit Testing World.");
    if (args.length > 0){
      System.out.println("----- args -----");
      for(String arg : args){
        System.out.println(arg);
      }
      System.out.println("----- end  -----");
    }

    /*javax.swing.JFrame frame = new javax.swing.JFrame("HelloWorldSwing");
    frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
    javax.swing.JLabel label = new javax.swing.JLabel("Hello World");
    frame.getContentPane().add(label);
    frame.pack();
    frame.setVisible(true);*/
  }
}
