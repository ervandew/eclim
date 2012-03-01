<% if (delegate){ %>
/**
 * @see ${superType}#${methodSignature}
 */
<% } else if (org_eclipse_jdt_core_compiler_source < "1.5" && (overrides || implementof)) { %>
/**
 * {@inheritDoc}
 * @see ${superType}#${methodSignature}
 */
<% } %>
<% if (org_eclipse_jdt_core_compiler_source >= "1.5" && (overrides || implementof) && !constructor && !delegate) { %>
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
