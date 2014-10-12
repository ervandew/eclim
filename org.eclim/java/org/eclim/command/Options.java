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
package org.eclim.command;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import org.apache.commons.lang.StringUtils;

import org.eclim.Services;

/**
 * Class for defining and working with all the eclim command
 * line options.
 *
 * @author Eric Van Dewoestine
 */
@SuppressWarnings("static-access")
public class Options
{
  public static final String COMMAND_OPTION = "command";
  public static final String PRETTY_OPTION = "pretty";
  public static final String EDITOR_OPTION = "editor";

  public static final String ACTION_OPTION = "a";
  public static final String APPLY_OPTION = "a";
  public static final String ARGS_OPTION = "a";
  public static final String BASEDIR_OPTION = "b";
  public static final String BUILD_OPTION = "b";
  public static final String BUILD_FILE_OPTION = "b";
  public static final String CASE_INSENSITIVE_OPTION = "i";
  public static final String CLASSNAME_OPTION = "c";
  public static final String CONTEXT_OPTION = "x";
  public static final String DEBUG_OPTION = "d";
  public static final String DELIMETER_OPTION = "d";
  public static final String DEPENDS_OPTION = "d";
  public static final String DEST_OPTION = "d";
  public static final String DIR_OPTION = "d";
  public static final String ENCODING_OPTION = "e";
  public static final String EXCLUDES_OPTION = "e";
  public static final String ERRORS_OPTION = "e";
  public static final String FAMILY_OPTION = "f";
  public static final String FILE_OPTION = "f";
  public static final String FOLDER_OPTION = "f";
  public static final String HELP = "help";
  public static final String HALT_OPTION = "h";
  public static final String HOST_OPTION = "h";
  public static final String INDENT_OPTION = "i";
  public static final String INDEXED_OPTION = "i";
  public static final String JARS_OPTION = "j";
  public static final String LANG_OPTION = "l";
  public static final String LAYOUT_OPTION = "l";
  public static final String LENGTH_OPTION = "l";
  public static final String LINE_OPTION = "l";
  public static final String LINE_WIDTH_OPTION = "w";
  public static final String METHOD_OPTION = "m";
  public static final String NAME_OPTION = "n";
  public static final String NATURE_OPTION = "n";
  public static final String OFFSET_OPTION = "o";
  public static final String PATH_OPTION = "p";
  public static final String PATTERN_OPTION = "p";
  public static final String PEEK_OPTION = "p";
  public static final String PORT_NUMBER_OPTION = "n";
  public static final String PROJECT_OPTION = "p";
  public static final String PROPERTIES_OPTION = "r";
  public static final String REVISION_OPTION = "r";
  public static final String ROOT_OPTION = "r";
  public static final String SCHEMA_OPTION = "s";
  public static final String SCOPE_OPTION = "s";
  public static final String SEARCH_OPTION = "s";
  public static final String SETTINGS_OPTION = "s";
  public static final String SETTING_OPTION = "s";
  public static final String SOURCE_OPTION = "s";
  public static final String SUPERTYPE_OPTION = "s";
  public static final String TEMPLATE_OPTION = "t";
  public static final String TEST_OPTION = "t";
  public static final String THREAD_ID_OPTION = "t";
  public static final String TYPE_OPTION = "t";
  public static final String URL_OPTION = "u";
  public static final String VALIDATE_OPTION = "v";
  public static final String VALUE_OPTION = "v";
  public static final String VALUES_OPTION = "v";
  public static final String VARIABLE_OPTION = "v";
  public static final String VARIABLE_VALUE_ID_OPTION = "v";
  public static final String VERSION_OPTION = "v";
  public static final String VIM_INSTANCE_OPTION = "v";

  private static final String ANY = "ANY";
  private static final String ARG = "ARG";
  private static final String REQUIRED = "REQUIRED";

  private static org.apache.commons.cli.Options coreOptions =
      new org.apache.commons.cli.Options();
  static {
    coreOptions.addOption(OptionBuilder.withArgName(COMMAND_OPTION)
        .isRequired(true)
        .hasArg()
        .withDescription(Services.getMessage("command.description"))
        .create(COMMAND_OPTION));
    coreOptions.addOption(OptionBuilder.withArgName(PRETTY_OPTION)
        .withDescription(Services.getMessage("pretty.description"))
        .create(PRETTY_OPTION));
    coreOptions.addOption(OptionBuilder.withArgName(EDITOR_OPTION)
        .hasArg()
        .withDescription(Services.getMessage("editor.description"))
        .create(EDITOR_OPTION));
  }

  protected org.apache.commons.cli.Options options =
    new org.apache.commons.cli.Options();

  /**
   * The user supplied command line args.
   */
  protected CommandLine commandLine;

  /**
   * Creates and initializes the jmpc command line options.
   */
  public Options()
  {
    @SuppressWarnings("unchecked")
    Collection<Option> opts = coreOptions.getOptions();
    for(Option option : opts){
      options.addOption(option);
    }
  }

  /**
   * Parses the supplied command line options.
   *
   * @param args The arguments.
   *
   * @return The command line.
   */
  public CommandLine parse(String[] args)
    throws Exception
  {
    // manually parse out the command option value so that the command specific
    // options can be added before running the automated parse.
    String commandName = null;
    for (int ii = 0; ii < args.length; ii++){
      if(args[ii].equals('-' + COMMAND_OPTION)){
        if(args.length > ii + 1){
          commandName = args[ii + 1].trim();
        }
        break;
      }
    }
    Command command = null;
    if(commandName != null){
      command = Services.getCommand(commandName);
      if (command == null){
        throw new RuntimeException(
            Services.getMessage("command.not.found", commandName));
      }
      org.eclim.annotation.Command info = (org.eclim.annotation.Command)
        command.getClass().getAnnotation(org.eclim.annotation.Command.class);
      Collection<Option> commandOptions = parseOptions(info.options());
      for(Option option : commandOptions){
        options.addOption(option);
      }
    }

    CommandLineParser parser = new GnuParser();
    return new CommandLine(command, parser.parse(options, args), args);
  }

  /**
   * Parses the String representation of the options to a Collection of Options.
   *
   * @param optionsString The options String.
   * @return The Collection of Option instances.
   */
  public Collection<Option> parseOptions(String optionsString)
  {
    ArrayList<Option> options = new ArrayList<Option>();
    if(optionsString != null && optionsString.trim().length() > 0){
      String[] lines = StringUtils.split(optionsString, ',');
      for(int ii = 0; ii < lines.length; ii++){
        if(lines[ii].trim().length() > 0){
          options.add(parseOption(lines[ii].trim()));
        }
      }
    }

    return options;
  }

  /**
   * Parses the String representation of an Option to an Option instance.
   *
   * @param option The option String.
   * @return The Option.
   */
  public Option parseOption(String option)
  {
    String[] parts = StringUtils.split(option);

    // command can have any additional arguments.
    //if(parts.length == 1 && ANY.equals(parts[0])){
    //}

    if(REQUIRED.equals(parts[0])){
      OptionBuilder.isRequired();
    }
    if(ARG.equals(parts[3])){
      OptionBuilder.hasArg();
      //OptionBuilder.withArgName(parts[2]);
    }else if(ANY.equals(parts[3])){
      OptionBuilder.hasOptionalArgs();
    }
    OptionBuilder.withLongOpt(parts[2]);
    return OptionBuilder.create(parts[1]);
  }
}
