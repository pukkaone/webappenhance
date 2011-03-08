package com.github.pukkaone.jsp;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;

/**
 * ELResolver that always resolves a value regardless of the arguments passed to
 * the getValue method.
 */
class ConstantELResolver extends ELResolver {

    private String value;
    
    ConstantELResolver(String value) {
        this.value = value;
    }
    
    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object arg1) {
        return null;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(
            ELContext context, Object arg1)
    {
        return null;
    }

    @Override
    public Class<?> getType(ELContext context, Object arg1, Object arg2)
        throws NullPointerException, PropertyNotFoundException, ELException
    {
        return null;
    }

    @Override
    public Object getValue(ELContext context, Object arg1, Object arg2)
        throws NullPointerException, PropertyNotFoundException, ELException
    {
        context.setPropertyResolved(true);
        return value;
    }

    @Override
    public boolean isReadOnly(ELContext context, Object arg1, Object arg2)
        throws NullPointerException, PropertyNotFoundException, ELException
    {
        return false;
    }

    @Override
    public void setValue(ELContext context, Object arg1, Object arg2, Object arg3)
        throws NullPointerException, PropertyNotFoundException, PropertyNotWritableException, ELException
    {
    }
}
