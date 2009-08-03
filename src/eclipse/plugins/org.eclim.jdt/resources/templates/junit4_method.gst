/**
 * Tests '${methodName}'.
 *
<% for (methodSignature in methodSignatures) { %>
 * @see ${superType}#${methodSignature}
<% } %>
 */
@Test
public void ${name}()
	throws Exception
{
<% if (methodBody) { %>
	${methodBody}
<% } %>
}
