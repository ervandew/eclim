/**
 * Constructs a new instance.
<% if(fields){ %>
 *
<% for (field in fields) { %>
 * @param ${field} The ${field} for this instance.
<% } %>
<% } %>
 */
public ${type}(${params})
{
<% for (field in fields) { %>
	this.${field} = ${field};
<% } %>
}
