# JspCompile

Compiles JSPs in a Web application on startup.

## How to use

In the `web.xml` file, define a listener:

    <listener>
      <listener-class>com.github.pukkaone.jspcompile.JspCompileListener</listener-class>
    </listener> 
