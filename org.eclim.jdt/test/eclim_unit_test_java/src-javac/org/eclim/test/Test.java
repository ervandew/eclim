/**
 * Copyright (C) 2005 - 2010  Eric Van Dewoestine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.eclim.test;

/**
 * Test class to run javac against.
 *
 * @author Eric Van Dewoestine
 */
public class Test
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
