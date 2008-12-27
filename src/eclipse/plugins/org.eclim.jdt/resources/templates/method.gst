/**
<% if ((overrides || implementof) && !delegate && !constructor) { %>
 * {@inheritDoc}
<% } %>
<% if (overrides || implementof) { %>
 * @see ${superType}#${methodSignature}
<% } %>
 */
<% if (org_eclipse_jdt_core_compiler_source == "1.5" && overrides && !constructor) { %>
@Override
<% } %>
<% if(modifier) { %>${modifier} <% } %><% if(returnType) { %>${returnType} <% } %>${name}(${params})
<% if(throwsType) { %>

	throws ${throwsType}
<% } else { %>

<% } %>
{
<% if(methodBody) { %>
	${methodBody}
<% } %>
}
