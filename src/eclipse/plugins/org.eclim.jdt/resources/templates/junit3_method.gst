/**
 * Tests '${methodName}'.
 *
<% for (methodSignature in methodSignatures) { %>
 * @see ${superType}#${methodSignature}
<% } %>
 */
public void ${name} ()
	throws Exception
{
<% if (methodBody) { %>
	${methodBody}
<% } %>
}
