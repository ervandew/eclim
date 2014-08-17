/**
 * Copyright (C) 2005 - 2014  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.debug;

import java.util.Collection;
import java.util.Map;

public class TestDebugger {
  public static void main(String[] args) {
    System.out.println("Starting to build family ...");

    FamilyService familyService = new FamilyService();
    familyService.submitChore();

    Adult bob = new Adult("Bob", Sex.MALE);
    Adult alice = new Adult("Alice", Sex.FEMALE);

    boolean isMarried = alice.isMarried();

    bob.gotMarried(alice);
    alice.gotMarried(bob);
    isMarried = alice.isMarried();

    Collection<Child> children = alice.getChildren();

    Child tom = new Child("Tom", Sex.MALE);
    bob.addChild(tom);
    alice.addChild(tom);

    children = alice.getChildren();

    tom.addToys();
    Map<String, Integer> tomToys = tom.getToysMap();

    Child lisa = new Child("Lisa", Sex.FEMALE);
    bob.addChild(lisa);
    alice.addChild(lisa);

    children = alice.getChildren();

    Map<String, Integer> lisaToys = lisa.getToysMap();

    familyService.shutdown();

    System.out.println("Done ...");
  }
}
