# Java Web Application Enhancements Library

Utility library for Java web applications


## Compile JSPs on startup

In the `web.xml` file, add a listener:

    <listener>
      <listener-class>com.github.pukkaone.jsp.JspCompileListener</listener-class>
    </listener> 


## Escape EL expression values to prevent cross-site scripting

In the `web.xml` file, add a listener:

    <listener>
      <listener-class>com.github.pukkaone.jsp.EscapeXmlELResolverListener</listener-class>
    </listener> 


### Disable escaping

Use a custom tag to surround JSP code in which you do not want EL expression
values to be escaped:

    <%@ taglib prefix="enhance" uri="http://pukkaone.github.com/jsp" %>

    <enhance:out escapeXml="false">
      I hope this expression returns safe HTML: ${user.name}
    </enhance:out>
