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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves a property name to a value by reading from a model object. Given a
 * property name <var>key</var>, the following mechanisms are tried in this
 * order:
 * <ul>
 * <li>If the object is a {@link Map}, then call {@link Map#get} to get the
 * value using the string <var>key</var> as the key.
 * <li>If the object has a method named <var>key</var> with non-void return
 * type, then call the method.
 * <li>If the object has a method named {@code get}<var>key</var> with the first
 * letter of <var>key</var> capitalized and non-void return type, then call the
 * method.
 * <li>If the object has a field named <var>key</var>, then return the field
 * value.
 * </ul>
 * If the value acquired from the previous steps is an object implementing
 * {@link Callable}, then the return value from invoking it will be used.
 */
public class ValueResolver {

    /** special value indicating property was not found */
    public static final Object NOT_FOUND = new Object();

    /**
     * (model class, property name) combination used to find value fetchers
     * from cache
     */
    protected static class Key {
        public final Class<?> modelClass;
        public final String name;

        public Key (Class<?> modelClass, String name) {
            this.modelClass = modelClass;
            this.name = name;
        }

        @Override
        public int hashCode() {
            return modelClass.hashCode() + name.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Key)) {
                return false;
            }
            Key key = (Key) other;
            return modelClass == key.modelClass && name.equals(key.name);
        }
    }

    protected static interface ValueFetcher {
        Object get(Object data, String name);
    }

    protected static final ValueFetcher MAP_FETCHER = new ValueFetcher() {
        public Object get(Object model, String name) {
            Map<?,?> map = (Map<?,?>) model;
            Object value = map.get(name);
            if (value == null && !map.containsKey(name)) {
                // The get method returned null because the key was not found.
                value = NOT_FOUND;
            }
            return value;
        }
    };

    protected static Map<Key, ValueFetcher> fetcherCache =
            new ConcurrentHashMap<Key, ValueFetcher>();

    private Object model;

    /**
     * Constructor
     *
     * @param model
     *            object to read values from
     */
    public ValueResolver(Object model) {
        this.model = model;
    }

    protected static Method getMethod(Class<?> clazz, String name) {
        Method method;
        try {
            method = clazz.getDeclaredMethod(name);
            if (!method.getReturnType().equals(void.class)) {
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                return method;
            }
        } catch (NoSuchMethodException e) {
            // fall through
        }

        try {
            String getter =
                    "get" +
                    Character.toUpperCase(name.charAt(0)) + name.substring(1);
            method = clazz.getDeclaredMethod(getter);
            if (!method.getReturnType().equals(void.class)) {
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                return method;
            }
        } catch (NoSuchMethodException e) {
            // fall through
        }

        Class<?> superClass = clazz.getSuperclass();
        if (superClass != Object.class && superClass != null) {
            return getMethod(superClass, name);
        }
        return null;
    }

    protected static Field getField(Class<?> clazz, String name) {
        Field field;
        try {
            field = clazz.getDeclaredField(name);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return field;
        } catch (NoSuchFieldException e) {
            // fall through
        }

        Class<?> superClass = clazz.getSuperclass();
        if (superClass != Object.class && superClass != null) {
            return getField(superClass, name);
        }
        return null;
    }

    protected static ValueFetcher createFetcher(Key key) {
        if (Map.class.isAssignableFrom(key.modelClass)) {
            return MAP_FETCHER;
        }

        final Method method = getMethod(key.modelClass, key.name);
        if (method != null) {
            return new ValueFetcher() {
                public Object get(Object data, String name) {
                    try {
                        return method.invoke(data);
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("method.invoke", e);
                    } catch (InvocationTargetException e) {
                        throw new IllegalStateException("method.invoke", e);
                    }
                }
            };
        }

        final Field field = getField(key.modelClass, key.name);
        if (field != null) {
            return new ValueFetcher() {
                public Object get(Object data, String name) {
                    try {
                        return field.get(data);
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("field.get", e);
                    }
                }
            };
        }

        return null;
    }

    protected ValueFetcher getFetcher(Key key) {
        ValueFetcher fetcher = fetcherCache.get(key);
        if (fetcher == null) {
            fetcher = createFetcher(key);
            if (fetcher != null) {
                fetcherCache.put(key, fetcher);
            }
        }
        return fetcher;
    }

    private Object getValueInternal(String name) {
        Key key = new Key(model.getClass(), name);
        ValueFetcher fetcher = getFetcher(key);
        if (fetcher == null) {
            return NOT_FOUND;
        }

        try {
            return fetcher.get(model, name);
        } catch (IllegalStateException e) {
            // The method or field was changed out from under us.
            // Update the cache and try again
            fetcher = createFetcher(key);
            if (fetcher != null) {
                fetcherCache.put(key, fetcher);
            }
        }

        if (fetcher == null) {
            return NOT_FOUND;
        }

        return fetcher.get(model, name);
    }

    /**
     * Resolves name to value from the object.
     *
     * @param name
     *            name to resolve
     * @return
     */
    public Object getValue(String name) {
        Object value = getValueInternal(name);
        if (value instanceof Callable<?>) {
            try {
                value = ((Callable<?>) value).call();
            } catch (Exception e) {
                throw new RuntimeException("call", e);
            }
        }
        return value;
    }
}
