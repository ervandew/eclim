package org.vimplugin;

import java.util.HashMap;

import org.vimplugin.handlers.EclipseCommand;
import org.vimplugin.handlers.IHandler;
import org.vimplugin.handlers.Undefined;
import org.vimplugin.listeners.KeyAtPosition;

public class KeyAtPositionRegistry {

  KeyAtPosition[] currentcommands;
  IHandler[] currenthandlers;

  final HashMap<String, IHandler> handlers;
  final HashMap<String, KeyAtPosition> commands;

  public KeyAtPositionRegistry() {
    handlers = new HashMap<String, IHandler>();
    commands = new HashMap<String, KeyAtPosition>();
    currentcommands = new KeyAtPosition[5];
    currenthandlers = new IHandler[5];
  }

  public void setEclipseCommandHandler(VimConnection vc, String key, String eclipseCommandId, int number) {
    //lookup existing handler or create a new one
    currenthandlers[number] = handlers.get(eclipseCommandId);
    if (currenthandlers[number] == null) {
      currenthandlers[number] = new EclipseCommand(eclipseCommandId);
      handlers.put(eclipseCommandId,currenthandlers[number]);
    }

    //we have already something in this command.
    if (currentcommands[number]!=null) {
      currentcommands[number].setHandler(new Undefined("This key is registered, but has currently no handler! "+ currentcommands[number].getKey()));
    }

    //lookup existing command or create a new one
    currentcommands[number] = commands.get(key);
    if (currentcommands[number] == null) {
      currentcommands[number] = new KeyAtPosition(key);
      commands.put(key,currentcommands[number]);
      //also add listener and inform vim about new key
      vc.listeners.add(currentcommands[number]);
      vc.command(vc.getVimID(), "specialKeys", "\""+key+"\"");
    }

    currentcommands[number].setHandler(currenthandlers[number]);
  }
}
