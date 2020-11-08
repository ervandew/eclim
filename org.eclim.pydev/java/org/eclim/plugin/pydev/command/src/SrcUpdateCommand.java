/**
 * Copyright (C) 2012 - 2020  Eric Van Dewoestine
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
package org.eclim.plugin.pydev.command.src;

import java.io.File;

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.preference.Preferences;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jface.text.IDocument;

import org.python.pydev.ast.analysis.IAnalysisPreferences;

import org.python.pydev.ast.analysis.messages.AbstractMessage;
import org.python.pydev.ast.analysis.messages.IMessage;

import org.python.pydev.ast.codecompletion.revisited.modules.AbstractModule;
import org.python.pydev.ast.codecompletion.revisited.modules.SourceModule;
import org.python.pydev.ast.codecompletion.revisited.modules.SourceToken;

import org.python.pydev.ast.codecompletion.revisited.visitors.AbstractVisitor;

import org.python.pydev.core.IIndentPrefs;

import org.python.pydev.core.autoedit.DefaultIndentPrefs;

import org.python.pydev.parser.jython.ParseException;
import org.python.pydev.parser.jython.SimpleNode;

import org.python.pydev.parser.jython.ast.ImportFrom;
import org.python.pydev.parser.jython.ast.NameTok;

import org.python.pydev.plugin.nature.PythonNature;

import com.python.pydev.analysis.AnalysisPreferences;
import com.python.pydev.analysis.OccurrencesAnalyzer;

/**
 * Command that updates the requested python src file.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "python_src_update",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "OPTIONAL v validate NOARG," +
    "OPTIONAL b build NOARG"
)
public class SrcUpdateCommand
  extends AbstractCommand
{
  private static final Pattern NAME = Pattern.compile("^.*:\\s+(.*)$");

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String file = commandLine.getValue(Options.FILE_OPTION);
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    IProject project = ProjectUtils.getProject(projectName);
    PythonNature nature = PythonNature.getPythonNature(project);

    IFile ifile = ProjectUtils.getFile(project, file);

    // validate the src file.
    if(commandLine.hasOption(Options.VALIDATE_OPTION) && nature != null){
      String filePath = ProjectUtils.getFilePath(project, file);
      String moduleName = nature.resolveModule(filePath);

      IDocument document = ProjectUtils.getDocument(project, file);
      // NOTE: checkForPath is false to support python files w/ file extenstion
      // != .py (like twisted .tac files for example)
      SourceModule module = (SourceModule)AbstractModule.createModuleFromDoc(
          moduleName, new File(filePath), document, nature,
          false /* checkForPath */);

      // see com.python.pydev.analysis.builder.AnalysisBuilderRunnable.doAnalysis
      IAnalysisPreferences analysisPreferences = new AnalysisPreferences(ifile);
      IIndentPrefs indentPrefs = DefaultIndentPrefs.get(ifile);
      indentPrefs.regenerateIndentString(); // TAB_INDENT pref may have changd.
      OccurrencesAnalyzer analyzer = new OccurrencesAnalyzer();
      IMessage[] messages = analyzer.analyzeDocument(
        nature, module, analysisPreferences, document,
        new NullProgressMonitor(), indentPrefs);

      Preferences prefs = getPreferences();

      //Map<String,String> builtins = Preferences.getInstance()
      //  .getMapValue(project, "org.eclim.python.builtins");
      HashMap<Integer, Set<String>> ignores = new HashMap<Integer, Set<String>>();
      ignores.put(
          IAnalysisPreferences.TYPE_UNRESOLVED_IMPORT,
          prefs.getSetValue(project, "org.eclim.python.ignore.unresolved.imports"));
      ignores.put(
          IAnalysisPreferences.TYPE_ASSIGNMENT_TO_BUILT_IN_SYMBOL,
          prefs.getSetValue(project, "org.eclim.python.ignore.builtin.reserved"));

      ArrayList<Error> errors = new ArrayList<Error>();
      for (IMessage message : messages){
        // this results in a lot of false positives for runtime added attributes
        if (message.getType() ==
            IAnalysisPreferences.TYPE_UNDEFINED_IMPORT_VARIABLE)
        {
          continue;
        }

        int type = message.getType();
        if (type == IAnalysisPreferences.TYPE_UNRESOLVED_IMPORT ||
            type == IAnalysisPreferences.TYPE_ASSIGNMENT_TO_BUILT_IN_SYMBOL)
        {
          Set<String> ignore = ignores.get(type);
          if (ignore.contains("*")){
            continue;
          }

          Matcher matcher = NAME.matcher(message.getMessage());
          if (matcher.find()){
            String name = matcher.group(1);

            if (type == IAnalysisPreferences.TYPE_UNRESOLVED_IMPORT &&
                message instanceof AbstractMessage)
            {
              Field generator = AbstractMessage.class.getDeclaredField("generator");
              generator.setAccessible(true);
              SourceToken token = (SourceToken)generator.get(message);
              SimpleNode ast = token.getAst();
              if (ast instanceof ImportFrom) {
                ImportFrom imprt = (ImportFrom)ast;
                //if it is a wild import, it starts on the module name
                if (!AbstractVisitor.isWildImport(imprt) &&
                    imprt.module instanceof NameTok)
                {
                  name = ((NameTok)imprt.module).id + '.' + name;
                }
              }
            }
            if (ignore.contains(name)){
              continue;
            }
          }
        }

        // ignore undefined variable errors for user defined globals
        //if (message.getType() == IAnalysisPreferences.TYPE_UNDEFINED_VARIABLE){
        //  Matcher matcher = UNRESOLVED_NAME.matcher(message.getMessage());
        //  if (matcher.find() && builtins.containsKey(matcher.group(1))){
        //    continue;
        //  }
        //}

        errors.add(new Error(
            message.getMessage(),
            filePath,
            message.getStartLine(document),
            message.getStartCol(document),
            message.getSeverity() != IMarker.SEVERITY_ERROR));
      }

      if (module.parseError != null &&
          module.parseError instanceof ParseException)
      {
        // temporarily use verbose exception so we can get a meaningful message
        boolean saved = ParseException.verboseExceptions;
        ParseException.verboseExceptions = true;

        try{
          ParseException parseError = (ParseException)module.parseError;
          errors.add(new Error(
              parseError.getMessage(),
              filePath,
              parseError.currentToken.getBeginLine(),
              parseError.currentToken.getBeginCol(),
              false));
        }finally{
          ParseException.verboseExceptions = saved;
        }
      }

      if(commandLine.hasOption(Options.BUILD_OPTION)){
        project.build(
            IncrementalProjectBuilder.INCREMENTAL_BUILD,
            new NullProgressMonitor());
      }
      return errors;
    }

    return null;
  }
}
