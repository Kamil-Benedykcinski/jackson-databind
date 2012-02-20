package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;
import java.lang.annotation.Annotation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;

/**
 * Specialized {@link SettableBeanProperty} implementation used
 * for virtual property that represents Object Id that is used
 * for some POJO types (or properties).
 */
public final class ObjectIdProperty
	extends SettableBeanProperty
{
    protected final ObjectIdReader _objectIdReader;
    
    public ObjectIdProperty(ObjectIdReader objectIdReader)
    {
        super(objectIdReader.propertyName, objectIdReader.idType, null, null);
        _objectIdReader = objectIdReader;
        _valueDeserializer = objectIdReader.deserializer;
    }

    protected ObjectIdProperty(ObjectIdProperty src, JsonDeserializer<?> deser)
    {
        super(src, deser);
        _objectIdReader = src._objectIdReader;
    }

    protected ObjectIdProperty(ObjectIdProperty src, String newName) {
        super(src, newName);
        _objectIdReader = src._objectIdReader;
    }

    @Override
    public ObjectIdProperty withName(String newName) {
        return new ObjectIdProperty(this, newName);
    }

    @Override
    public ObjectIdProperty withValueDeserializer(JsonDeserializer<?> deser) {
        return new ObjectIdProperty(this, deser);
    }
    
    // // // BeanProperty impl
    
    @Override
    public <A extends Annotation> A getAnnotation(Class<A> acls) {
        return null;
    }

    @Override public AnnotatedMember getMember() {  return null; }

    /*
    /**********************************************************
    /* Deserialization methods
    /**********************************************************
     */

    @Override
    public void deserializeAndSet(JsonParser jp, DeserializationContext ctxt,
        Object instance)
            throws IOException, JsonProcessingException
    {
    	deserializeSetAndReturn(jp, ctxt, instance);
    }

    @Override
    public Object deserializeSetAndReturn(JsonParser jp,
    		DeserializationContext ctxt, Object instance)
        throws IOException, JsonProcessingException
    {
        // note: no null checks (unlike usually); deserializer should fail if one found
        Object id = _valueDeserializer.deserialize(jp, ctxt);
        ReadableObjectId roid = ctxt.findObjectId(id, _objectIdReader.generator);
        roid.bindItem(instance);
        // also: may need to set a property value as well
        SettableBeanProperty idProp = _objectIdReader.idProperty;
        if (idProp != null) {
            return idProp.setAndReturn(instance, id);
        }
        return instance;
    }
    
    
    @Override
    public void set(Object instance, Object value) throws IOException {
    	setAndReturn(instance, value);
    }

    @Override
    public Object setAndReturn(Object instance, Object value)
   		throws IOException
	{
        SettableBeanProperty idProp = _objectIdReader.idProperty;
        if (idProp == null) {
        	throw new UnsupportedOperationException(
        			"Should not call set() on ObjectIdProperty that has no SettableBeanProperty");
        }
        return idProp.setAndReturn(instance, value);
	}
}