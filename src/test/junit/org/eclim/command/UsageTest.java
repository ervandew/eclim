package org.eclim.command;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Set;

import org.eclim.Services;
import org.eclim.annotation.Command;
import org.eclim.plugin.jdt.PluginResources;
import org.junit.Before;
import org.junit.Test;
import org.reflections.Reflections;

public class UsageTest {

  @Before
  public void registerCommands() {
    PluginResources resources = new PluginResources();
    Services.addPluginResources(resources);
    loadCommands(resources);
  }

  @SuppressWarnings("unchecked")
  private void loadCommands(PluginResources resources) {
    Reflections reflections = new Reflections("org.eclim");
    Set<Class<?>> commands = reflections.getTypesAnnotatedWith(Command.class);
    assertFalse(commands.isEmpty());
    for (Class<?> class1 : commands) {
      System.out.println("Registering command:" + class1.getName());
      if (org.eclim.command.Command.class.isAssignableFrom(class1)) {
        resources
            .registerCommand((Class<? extends org.eclim.command.Command>) class1);
      }
    }
  }

  @Test
  public void numerousCommands() throws Exception {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    PrintStream printer = new PrintStream(stream);
    Main.usage(printer);
    printer.close();

    String result = stream.toString("UTF-8");
    String[] lines = result.split("\r\n|\n|\r");
    assertTrue("Not enough lines (" + lines.length + ")", lines.length > 10);
  }
}
