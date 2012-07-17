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
package com.github.pukkaone.jsp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.VariableMapper;
import javax.servlet.jsp.JspContext;
import org.junit.Before;
import org.junit.Test;

public class EscapeXMLELResolverTest {

    private static final String BASE = "base";
    private static final String PROPERTY = "property";
    private static final String VALUE = "<h1>&'\"";

    private CompositeELResolver compositeResolver;
    
    private ELContext elContext = new ELContext() {
        @Override
        public VariableMapper getVariableMapper() {
            return null;
        }
        
        @Override
        public FunctionMapper getFunctionMapper() {
            return null;
        }
        
        @Override
        public ELResolver getELResolver() {
            return compositeResolver;
        }
    };
    
    private JspContext pageContext = new FakeJspContext();
    
    @Before
    public void beforeTest() {
        compositeResolver = new CompositeELResolver();
        compositeResolver.add(new EscapeXmlELResolver());
        compositeResolver.add(new ConstantELResolver(VALUE));
        
        elContext.putContext(JspContext.class, pageContext);
    }
    
    @Test
    public void getCommonPropertyType_should_return_null() {
        assertNull(compositeResolver.getCommonPropertyType(elContext, BASE));
    }

    @Test
    public void getFeatureDescriptors_should_return_empty() {
        assertFalse(compositeResolver.getFeatureDescriptors(elContext, BASE).hasNext());
    }

    @Test
    public void getType_should_return_null() {
        assertNull(compositeResolver.getType(elContext, BASE, PROPERTY));
    }

    @Test
    public void getValue_should_escape_value_when_enabled() {
        String value = (String) compositeResolver.getValue(elContext, BASE, PROPERTY);
        assertEquals("&lt;h1&gt;&amp;&#039;&#034;", value);
    }

    @Test
    public void getValue_should_not_escape_value_when_disabled() {
        JspContext pageContext = (JspContext) elContext.getContext(JspContext.class);
        pageContext.setAttribute(EscapeXmlELResolver.ESCAPE_XML_ATTRIBUTE, Boolean.FALSE);
        
        String value = (String) compositeResolver.getValue(elContext, BASE, PROPERTY);
        assertEquals(VALUE, value);
    }
    
    @Test
    public void isReadOnly_should_return_false() {
        assertFalse(compositeResolver.isReadOnly(elContext, BASE, PROPERTY));
    }

    @Test
    public void setValue_should_not_infinitely_recurse() {
        compositeResolver.setValue(elContext, BASE, PROPERTY, VALUE);
    }
}
