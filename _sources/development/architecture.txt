.. Copyright (C) 2005 - 2013  Eric Van Dewoestine

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.

Architecture
============

The eclim architecture is pretty straight forward.  Commands issued by a user
in vim are relayed via nailgun_ to the running eclim daemon and the proper
command implementation is then located and executed.

Here is a diagram showing the sequence in a bit more detail:

.. uml::

   skinparam monochrome true
   hide footbox
   box "Vim"
     participant "<command>.vim"
     participant eclim.vim
   end box
   participant nailgun
   box "Eclipse / Eclimd"
     participant Main
     participant Services
     participant "<Command>"
   end box

   "<command>.vim" -> eclim.vim : eclim#Execute
   activate eclim.vim

   eclim.vim -> nailgun
   activate nailgun

   nailgun -> Main : main
   activate Main

   Main -> Services : getCommand
   Services --> Main
   Main -> "<Command>" : execute
   "<Command>" --> Main
   Main --> nailgun
   nailgun --> eclim.vim
   eclim.vim --> "<command>.vim"

   deactivate Main
   deactivate nailgun
   deactivate eclim.vim


The commands which are executed on the eclimd side are also fairly simple.
They accept an object containing the command line parameters passed into the
eclim invocation and then return an object (String, Collection, etc) which is
converted to a json response.  Below is a simple class diagram showing the
hierarchy of a couple typical commands.

.. uml::

   skinparam monochrome true
   skinparam circled {
     CharacterFontSize 0
     CharacterRadius 0
   }

   Command <|.. AbstractCommand
   AbstractCommand <|-- PingCommand
   AbstractCommand <|-- ShutdownCommand

   class Command <<Interface>> {
     Object execute(CommandLine)
   }
   AbstractCommand : Preferences getPreferences()
   PingCommand : Object execute(CommandLine)
   ShutdownCommand : Object execute(CommandLine)


Another important aspect of eclim's architecture is support for plugins.
Plugins for eclim are bundled as eclipse plugins with their auto start
attribute set to false.  When the eclim daemon starts it will locate and load
any eclipse plugin with an 'org.eclim.' prefix.

When a plugin is loaded, eclim will locate the plugin's required resources
provider and invoke its initialize method which will then inject its resources
(messages, command options, etc) into eclim and register any new commands.

Here is graphical representation of this process:

.. uml::

   skinparam monochrome true
   hide footbox

   participant "<Eclipse>"
   participant EclimApplication
   participant PluginResources
   participant Services

   "<Eclipse>" -> EclimApplication : start
   activate EclimApplication
   EclimApplication -> EclimApplication : loadPlugins
   activate EclimApplication
   EclimApplication -> PluginResources : initialize
   activate PluginResources
   PluginResources -> Services : addPluginResources
   PluginResources -> Services : registerCommand
   PluginResources --> EclimApplication
   deactivate PluginResources

   deactivate EclimApplication
   deactivate EclimApplication
   EclimApplication --> "<Eclipse>"

.. _nailgun: http://www.martiansoftware.com/nailgun/
