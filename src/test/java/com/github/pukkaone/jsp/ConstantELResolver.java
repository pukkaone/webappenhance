package com.github.pukkaone.jsp;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import javax.el.ELContext;
import javax.el.ELResolver;

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

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
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
