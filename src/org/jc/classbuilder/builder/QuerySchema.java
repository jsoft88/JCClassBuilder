/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jc.classbuilder.builder;

import org.apache.avro.Schema;

/**
 *
 * @author cespedjo
 * @param <S> Class of schema.
 * @param <F> Class of Field. Example: In avro this would be Schema.Field
 */
public interface QuerySchema<S, F> {
    
    /**
     * Check schemas in data structure, find next one.
     * @return A schema representing the field.
     * @throws java.lang.Exception throw an exception if anything goes wrong.
     */
    F getNextField() throws Exception;
    
    /**
     * Returns next field without altering fields in stack.
     * @return Next field in stack without removing it.
     * @throws Exception if anything goes wrong while peeking next field.
     */
    F peekNextField() throws Exception;
    
    /**
     * Given a field's schema, return the java type associated with it as string
     * If the field is a complex datatype, that is, it requires a new class to
     * be created to be added as attribute inside the class, then the method
     * should return null.
     * 
     * @param fieldSchema schema associated to a given field.
     * @return the java FQCN as String or null if the type is complex and requires,
     * for example, the creation of another class.
     * @throws Exception when there isn't a java type for field's schema.
     */
    String getFieldType(F fieldSchema) throws Exception;
    
    /**
     * Unlike getFieldType, this method returns the type as a schema.
     * @param fieldSchema the class representing the field, from which a type
     * schema can be derived.
     * @return the schema of the field's type.
     * @throws Exception if there's a problem while extracting type schema.
     */
    S getFieldTypeAsSchema(F fieldSchema) throws Exception;
    
    /**
     * Given a field's schema, return its name.
     * @param fieldSchema field's schema.
     * @return String representing the name of the field.
     * @throws java.lang.Exception throw an exception if anything goes wrong.
     */
    String getFieldName(F fieldSchema) throws Exception;
    
    /**
     * Returns the name of a schema.
     * @param schema the schema from which we wish to obtain a name.
     * @return String representing the name of schema.
     * @throws Exception if anything goes wrong while trying to extract name from
     * schema.
     */
    String getSchemaName(S schema) throws Exception;
    
    /**
     * If there's a field like a Map, return the java type associated with its
     * key.
     * @param fieldSchema field's schema.
     * @return Schema representing type of key.
     * @throws java.lang.Exception throw an exception if anything goes wrong.
     */
    Schema getKeyType(S fieldSchema) throws Exception;
    
    /**
     * If there's a field like a Map, return the java type associated with its
     * value
     * @param fieldSchema field's schema.
     * @return Schema representing type of values.
     * @throws java.lang.Exception throw an exception if anything goes wrong.
     */
    Schema getValueType(S fieldSchema) throws Exception;
    
    /**
     * If there's a field like an array or list, this method returns elements'
     * type.
     * @param fieldSchema field's schema.
     * @return Schema representing type of elements.
     * @throws java.lang.Exception throw an exception if anything goes wrong.
     */
    S getElementsType(S fieldSchema) throws Exception;
    
    /**
     * Tells whether a field is a complex type (i.e. not a primitive)
     * @param fieldSchema field's schema.
     * @return true if field has a complex type. Otherwise, it returns false.
     * @throws java.lang.Exception throw an exception if anything goes wrong.
     */
    boolean isComplexType(S fieldSchema) throws Exception;
    
    /**
     * Name of the class that will be generated for the complex type. Note that
     * the full schema (not field schema) itself is a complex type containing 
     * different fields, so this method will be invoked the first time as well.
     * A level parameter is provided for the class implementing this interface
     * to identify which name must be returned.
     * 
     * @param fieldSchema field's schema (could be full schema as well).
     * @param level when level == 0, the method is being invoked for getting the
     * name of the full class. When level != 0, the method must return a name
     * for the class to be generated for a complex type.
     * @return the name for the class (not a FQCN).
     * @throws java.lang.Exception throw an exception if anything goes wrong.
     */
    String complexTypeClassName(F fieldSchema, int level) throws Exception;
}
