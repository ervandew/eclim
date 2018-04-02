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

import org.eclim.util.file.Position;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import org.eclipse.jdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;

import org.eclipse.jdt.ui.JavaElementLabels;

/**
 * Utility methods for the OutlineCommand class.
 *
 * @author G0dj4ck4l
 */
public class OutlineUtil
{

	public static String getSignature(IMember member)
			throws JavaModelException
	{
		return new StringBuilder()
			.append(member instanceof IType ? getSignature((IType)member)
				: member instanceof IField ? getSignature((IField)member)
				: member instanceof IMethod ? getSignature((IMethod)member)
				: "")
			.toString();
	}

	public static Position getMemberFilePosition(IMember member)
			throws JavaModelException
	{
		return Position.fromOffset(
				member.getResource().getLocation().toOSString().replace('\\', '/'),
				null,
				member.getNameRange().getOffset(),
				member.getNameRange().getLength());
	}

	private static String getSignature(IType type)
			throws JavaModelException
	{
		return new StringBuilder()
			.append(getFlags(type))
			.append(" ")
			.append(OutlineUtil.getTypeOfMember(type))
			.append(" ")
			.append(type.getElementName())
			.toString();
	}

	private static String getSignature(IField field)
			throws JavaModelException
	{
		return new StringBuilder()
			.append(getFlags(field))
			.append(" ")
			.append(field.getElementName())
			.append(" : ")
			.append(Signature.toString(field.getTypeSignature()))
			.toString();
	}

	private static String getSignature(IMethod method)
			throws JavaModelException
	{
		return new StringBuilder()
			.append(getFlags(method))
			.append(" ")
			.append(JavaElementLabels
					.getTextLabel(
							method,
							AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS))
			.toString();
	}

	private static String getFlags(IMember member)
			throws JavaModelException
	{
		String flags = Flags.toString(member.getFlags());
		return new StringBuilder()
			.append(flags.contains("public") ? "+"
				: flags.contains("private") ? "-"
				: flags.contains("protected") ? "*"
				: "#")
			.append(flags.contains("static") ? " static" : "")
			.append(flags.contains("final") ? " final" : "")
			.append(flags.contains("abstract") ? " abstract" : "")
			.toString();
	}

	private static String getTypeOfMember(IType type)
			throws JavaModelException
	{
		return new StringBuilder()
			.append(type.isAnnotation() ? "annotation"
				: type.isInterface() ? "interface"
				: type.isEnum() ? "enum"
				: "class")
			.toString();
	}
}
