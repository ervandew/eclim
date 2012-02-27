<% if (((org_eclipse_jdt_core_compiler_source > "1.5" && implementof) || overrides) && !constructor) { %>
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
