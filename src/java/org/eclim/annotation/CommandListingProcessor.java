/**
 * Copyright (C) 2005 - 2009  Eric Van Dewoestine
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
package org.eclim.annotation;

import java.util.Collection;
import java.util.Set;

import java.util.regex.Pattern;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.apache.commons.cli.Option;

import org.eclim.command.Options;

/**
 * Annotation processor for listing all eclim commands.
 *
 * @author Eric Van Dewoestine
 */
@SupportedAnnotationTypes({"org.eclim.annotation.Command"})
public class CommandListingProcessor
  extends AbstractProcessor
{
  public boolean process(
      Set<? extends TypeElement> annotations,
      RoundEnvironment env)
  {
    Options options = new Options();
    Pattern pattern = null;
    String filter = this.processingEnv.getOptions().get("filter");
    if (filter != null){
      pattern = Pattern.compile(filter);
    }
    for(TypeElement element : annotations){
      for(Element e: env.getElementsAnnotatedWith(element)){
        Command command = e.getAnnotation(Command.class);
        if (pattern == null || pattern.matcher(command.name()).matches()){
          Collection<Option> opts = options.parseOptions(command.options());
          System.out.print(command.name());
          for (Option opt : opts){
            String display = "-" + opt.getOpt();
            if (opt.hasArg()){
              display += " " + opt.getLongOpt();
            }
            if (opt.isRequired()){
              System.out.print(" " + display);
            }else{
              System.out.print(" [" + display + "]");
            }
          }
          System.out.println("\n\tclass: " + e);
        }
      }
    }
    return true;
  }
}
