package com.github.pukkaone.jsp;

import java.util.Enumeration;
import java.util.HashMap;
import javax.el.ELContext;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

@SuppressWarnings("deprecation")
class FakeJspContext extends JspContext {
    
    HashMap<String, Object> attributes = new HashMap<String, Object>();

    @Override
    public Object findAttribute(String arg0) {
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Object getAttribute(String arg0, int arg1) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNamesInScope(int arg0) {
        return null;
    }

    @Override
    public int getAttributesScope(String arg0) {
        return 0;
    }

    @Override
    public ELContext getELContext() {
        return null;
    }

    @Override
    public ExpressionEvaluator getExpressionEvaluator() {
        return null;
    }

    @Override
    public JspWriter getOut() {
        return null;
    }

    @Override
    public VariableResolver getVariableResolver() {
        return null;
    }

    @Override
    public void removeAttribute(String arg0) {
    }

    @Override
    public void removeAttribute(String arg0, int arg1) {
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    public void setAttribute(String arg0, Object arg1, int arg2) {
    }
}