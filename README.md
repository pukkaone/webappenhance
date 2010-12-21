# JspTools

JSP utility classes for Web applications


## Compile JSPs on startup

In the `web.xml` file, define a listener:

    <listener>
      <listener-class>com.github.pukkaone.jsp.JspCompileListener</listener-class>
    </listener> 
