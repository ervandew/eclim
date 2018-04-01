/**
 * Copyright (C) 2005 - 2018  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.outline;

import java.util.List;

import org.eclim.util.file.Position;

/**
 * Represents a node element in the outline of a Java file.
 *
 * @author G0dj4ck4l
 */
public class OutlineNode
{
	private String name;
	private Position position;
	private List<OutlineNode> childrens;

	public OutlineNode(String name, Position position, List<OutlineNode> childrens)
	{
		this.name = name;
		this.position = position;
		this.childrens = childrens;
	}

	public String getName()
	{
		return name;
	}

	public Position getPosition()
	{
		return position;
	}

	public List<OutlineNode> getChildrens()
	{
		return childrens;
	}
}
