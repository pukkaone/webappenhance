# JspTools

JSP utility classes for Web applications


## Compile JSPs on startup

In the `web.xml` file, add a listener:

    <listener>
      <listener-class>com.github.pukkaone.jsp.JspCompileListener</listener-class>
    </listener> 


## Prevent cross-site scripting by escaping EL expression values

In the `web.xml` file, add a listener:

    <listener>
      <listener-class>com.github.pukkaone.jsp.EscapeXmlELResolverListener</listener-class>
    </listener> 
