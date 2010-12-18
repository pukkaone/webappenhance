/*
Copyright (c) 2010, Chin Huang
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.github.pukkaone.jspcompile;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Compiles JSPs on startup.
 */
public class JspCompileListener implements ServletContextListener {
    private static final long serialVersionUID = 1L;
    
    private ServletContext servletContext;
    private HttpServletRequest request = createHttpServletRequest();
    private HttpServletResponse response = createHttpServletResponse();
    
    private HttpServletRequest createHttpServletRequest() {
        InvocationHandler handler = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("getQueryString")) {
                    return "jsp_precompile";
                }
                return null;
            }
        };
        
        return (HttpServletRequest) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[] { HttpServletRequest.class },
                handler);
    }

    private HttpServletResponse createHttpServletResponse() {
        InvocationHandler handler = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return null;
            }
        };
        
        return (HttpServletResponse) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[] { HttpServletResponse.class },
                handler);
    }
    
    @SuppressWarnings("unchecked")
    private void compileJspsInDirectory(String dirPath) {
        Set<String> paths = servletContext.getResourcePaths(dirPath);
        for (String path : paths) {
            if (path.endsWith(".jsp")) {
                RequestDispatcher requestDispatcher =
                        servletContext.getRequestDispatcher(path);
                if (requestDispatcher == null) {
                    // Should have gotten a RequestDispatcher for the path
                    // because the path came from the getResourcePaths() method.
                    throw new Error(path + " not found");
                }

                try {
                    servletContext.log("Compiling " + path);
                    requestDispatcher.include(request, response);
                } catch (Exception e) {
                    servletContext.log("include", e);
                }
            } else if (path.endsWith("/")) {
                // Recursively process subdirectories.
                compileJspsInDirectory(path);
            }
        }
    }

    public void contextInitialized(ServletContextEvent event) {
        servletContext = event.getServletContext();
        
        compileJspsInDirectory("/");
    }

    public void contextDestroyed(ServletContextEvent event) {
    }
}
