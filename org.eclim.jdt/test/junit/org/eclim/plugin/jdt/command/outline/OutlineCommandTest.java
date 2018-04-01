/**
 * Copyright (C) 2012 - 2018  Eric Van Dewoestine
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

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;
import org.eclim.plugin.jdt.Jdt;
import org.junit.Test;

/**
 * Test case for OutlineCommand.
 *
 * @author G0dj4ck4l
 */
public class OutlineCommandTest
{
	private static final String TEST_PROJECT = Jdt.TEST_PROJECT;
	private static final String TEST_FILE = "src/org/eclim/test/outline/OutlineCommandExample.java";
	private static final String expectedOutput = "+ class OutlineCommandExample\n";
	
	@Test
	@SuppressWarnings("unchecked")
	public void execute()
			throws Exception
	{
		String[] arguments = {"java_outline", "-p", TEST_PROJECT, "-f", TEST_FILE};
		List<Map<String, Object>> nodes = (List<Map<String, Object>>)Eclim.execute(arguments);
		assertEquals(expectedOutput, resultOutput(nodes, ""));
	}
	
	@SuppressWarnings("unchecked")
	private String resultOutput(List<Map<String, Object>> nodes, String indent)
	{
		StringBuilder result = new StringBuilder();
		for(Map<String, Object> node : nodes)
		{
			result
			.append(indent + node.get("name") + "\n")
			.append(resultOutput((List<Map<String, Object>>)node.get("childrens"), indent + "\t"));
		}
		return result.toString();
	}
	
}
