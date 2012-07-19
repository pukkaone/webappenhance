/*
Copyright (c) 2012, Chin Huang
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

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;

/**
 * {@link ELResolver} that attempts to resolve a name to a property of the
 * request attribute named "it", which Jersey set to the model of a Viewable..
 */
public class ViewableModelELResolver extends ELResolver {

    // name of request attribute which Jersey set to the model
    private static final String VIEWABLE_MODEL_ATTRIBUTE = "it";

    // name of pageContext attribute holding property value resolver
    private static final String VALUE_RESOLVER_ATTRIBUTE =
            ViewableModelELResolver.class.getName() + ".valueResolver";

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        return null;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(
            ELContext context, Object base)
    {
        return null;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        return null;
    }

    private ValueResolver getValueResolver(PageContext pageContext, Object model) {
        ValueResolver valueResolver =
                (ValueResolver) pageContext.getAttribute(VALUE_RESOLVER_ATTRIBUTE);
        if (valueResolver == null) {
            valueResolver = new ValueResolver(model);
            pageContext.setAttribute(VALUE_RESOLVER_ATTRIBUTE, valueResolver);
        }

        return valueResolver;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        if (base != null) {
            return null;
        }

        PageContext pageContext = (PageContext) context.getContext(JspContext.class);
        Object model = pageContext.getRequest().getAttribute(VIEWABLE_MODEL_ATTRIBUTE);
        if (model == null) {
            return null;
        }

        ValueResolver valueResolver = getValueResolver(pageContext, model);
        Object value = valueResolver.getValue((String) property);
        if (value == ValueResolver.NOT_FOUND) {
            return null;
        }

        context.setPropertyResolved(true);
        return value;
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        return false;
    }

    @Override
    public void setValue(
            ELContext context, Object base, Object property, Object value)
    {
    }
}
