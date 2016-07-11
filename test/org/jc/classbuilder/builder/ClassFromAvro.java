/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jc.classbuilder.builder;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import org.apache.avro.Schema;

/**
 *
 * @author cespedjo
 */
public class ClassFromAvro extends ClassBuilder<Schema, Schema.Field>{
    
    public static final Schema NULL_SCHEMA = Schema.create(Schema.Type.NULL);
    public static final String ENUM_IDENTIFIER_FIELD_NAME = "FIELD_ENUM_HIDDEN";
    public static final String FIXED_IDENTIFIER_FIELD_NAME = "FIELD_FIXED_HIDDEN";

    @Override
    public Schema getFieldTypeAsSchema(Schema.Field fieldSchema) throws Exception {
        return fieldSchema.schema();
    }

    @Override
    public void init(Schema fullSchema, Path absoluteClassPath, Path absoluteSrcPath, String packageName) {
        super.init(fullSchema, absoluteClassPath, absoluteSrcPath, packageName);
    }

    @Override
    protected CtClass handleComplexTypeWithoutSubTypes(Schema.Field field, Schema fieldSchema) 
            throws Exception {

        ClassPool cp = ClassPool.getDefault();
        CtClass ret = null;
        String className = this.complexTypeClassName(field, 0);
        
        if (fieldSchema.getType() == Schema.Type.ENUM) {
            ret = cp.makeClass(this.packageName + "." + className);
            ret.setModifiers(Modifier.PUBLIC);
            ret.setModifiers(Modifier.FINAL);
            
            ret.addConstructor(CtNewConstructor.make("private " + className + "(){}", ret));
            
            //Javassist makes it amazingly hard to build an Enum, so I'm converting
            //Enum to a regular class with static final attributes.
            for (String enumObject : fieldSchema.getEnumSymbols()) {
                CtField staticEnumField = 
                        new CtField(ClassPool.getDefault().get("java.lang.String"), enumObject, ret);
                staticEnumField.setModifiers(Modifier.PUBLIC);
                staticEnumField.setModifiers(Modifier.FINAL);
                staticEnumField.setModifiers(Modifier.STATIC);
                CtField.Initializer init = CtField.Initializer.constant(enumObject);
                
                ret.addField(staticEnumField, init);
            }
            
            CtField enumIdentifierField = 
                    new CtField(CtClass.voidType, ClassFromAvro.ENUM_IDENTIFIER_FIELD_NAME, ret);
            enumIdentifierField.setModifiers(Modifier.PRIVATE);
            enumIdentifierField.setModifiers(Modifier.STATIC);
            
            ret.addField(enumIdentifierField);
            
        } else if (fieldSchema.getType() == Schema.Type.FIXED) {
            ret = cp.makeClass(className);
            AnnotationsAttribute anAtt1 = 
                    new AnnotationsAttribute(
                            ret.getClassFile().getConstPool(), 
                            AnnotationsAttribute.visibleTag);
            Annotation a = 
                    new Annotation("org.apache.avro.specific.FixedSize(" + fieldSchema.getFixedSize() + ")", 
                                    ret.getClassFile().getConstPool());
            anAtt1.addAnnotation(a);
            ret.getClassFile().addAttribute(anAtt1);
            
            CtConstructor ctConst = CtNewConstructor.make("public " + className + "(){super();}", ret);
                    //new CtConstructor(new CtClass[]{CtClass.voidType}, ret);
            //ctConst.setBody("super();");
            ret.addConstructor(ctConst);
            CtConstructor constWithArg = CtNewConstructor.make("public " + className + "(bytes[] bytes){ super(bytes); }", ret);
            ret.addConstructor(constWithArg);
        }
        
        return ret;
    }

    @Override
    protected void addMethods(CtField field, CtClass fullClass) throws Exception {
        if (field.getName().equals(ENUM_IDENTIFIER_FIELD_NAME)) {
            return;
        }
        if (field.getType().subtypeOf(ClassPool.getDefault().get("org.apache.avro.specific.SpecificFixed"))) {
            return;
        }
         
        String methodName
                = ("" + field.getName().charAt(0)).toUpperCase()
                + field.getName().substring(1, field.getName().length());

        try {
            MethodsDescription.addNewMethod(fullClass.getName(), "public " + field.getType().getName() + " get" + methodName +"(){return this." + field.getName() + ";}");
            MethodsDescription.addNewMethod(fullClass.getName(), "public void get" + methodName +"(" + field.getType().getName() + " " + field.getName() + "){ this." + field.getName() + " = " + field.getName() + ";}");
            CtMethod getter = CtNewMethod
                    .make("public " + field.getType().getName() + " get" + methodName +"(){return this." + field.getName() + ";}", 
                            fullClass);

            CtMethod setter = CtNewMethod
                    .make("public void get" + methodName +"(" + field.getType().getName() + " " + field.getName() + "){ this." + field.getName() + " = " + field.getName() + ";}", 
                            fullClass);

            fullClass.addMethod(setter);
            fullClass.addMethod(getter);
        } catch (CannotCompileException e1) {
            Logger.getLogger(ClassFromAvro.class.getName()).log(Level.SEVERE, null, e1);
            throw e1;
        }
    }
    
    
    @Override
    protected boolean hasSubTypes(Schema fieldSchema) throws Exception {
        return fieldSchema.getType() == Schema.Type.RECORD;
    }
    
    @Override
    public boolean isComplexType(Schema fieldSchema) throws Exception {
        boolean isComplex = (fieldSchema.getType() == Schema.Type.RECORD ||
                fieldSchema.getType() == Schema.Type.ENUM || 
                fieldSchema.getType() == Schema.Type.FIXED);
        
        return isComplex;
    }

    @Override
    protected List<Schema.Field> getFieldList(Schema schema) throws Exception {
        List<Schema.Field> fields = new ArrayList<>();
        for (Schema.Field aField : schema.getFields()) {
            fields.add(aField);
        }
        
        return fields;
    }

    @Override
    public Schema getElementsType(Schema fieldSchema) {
        return fieldSchema.getElementType();
    }

    @Override
    public Schema getValueType(Schema fieldSchema) {
        return fieldSchema.getValueType();
    }

    @Override
    public Schema getKeyType(Schema fieldSchema) {
        //In avro every key is a String
        return Schema.create(Schema.Type.STRING);
    }

    @Override
    public String getFieldName(Schema.Field fieldSchema) {
        return fieldSchema.name();
    }

    @Override
    public String getSchemaName(Schema schema) throws Exception {
        return schema.getName();
    }
    
    private String javaType(Schema.Type type, Schema schema) throws Exception {
        /*if (this.isComplexType(schema)) {
            CtClass cl = this.builder(schema, 1);
            return cl.getName();
        }*/
        if (type == Schema.Type.INT) {
            return "int";
        } else if (type == Schema.Type.LONG) {
            return "long";
        } else if (type == Schema.Type.STRING) {
            return "java.lang.CharSequence";
        } else if (type == Schema.Type.FLOAT) {
            return "float";
        } else if (type == Schema.Type.BOOLEAN) {
            return "boolean";
        } else if (type == Schema.Type.ARRAY) {
            return "java.util.List<" + this.javaType(schema.getElementType().getType(), schema.getElementType()) + ">";
        } else if (type == Schema.Type.DOUBLE) {
            return "double";
        } else if (type == Schema.Type.BYTES) {
            return "java.nio.ByteBuffer";
        } else if (type == Schema.Type.MAP) {
            return "Map<java.lang.CharSequence, " + 
                    this.javaType(schema.getValueType().getType(), schema.getValueType()) +
                    ">";
        } else if (type == Schema.Type.UNION) {
            List<Schema> types = schema.getTypes();
            if (types.size() == 2 && types.contains(NULL_SCHEMA)) {
                return this.javaType(
                        types.get(types.get(0).equals(NULL_SCHEMA) ? 1 : 0).getType(),
                        types.get(types.get(0).equals(NULL_SCHEMA) ? 1 : 0));
            } else {
                return "java.lang.Object";
            }
        } /*else if (type == Schema.Type.ENUM || type == Schema.Type.FIXED) {
            if (RESERVED_WORDS.contains(schema.getFullName())) {
                return schema.getFullName() + "$";
            } else {
                return schema.getFullName();
            }
        }*/
        
        return null;
    }

    @Override
    public String getFieldType(Schema.Field fieldSchema) throws Exception {
        return this.javaType(fieldSchema.schema().getType(), fieldSchema.schema());
    }

    @Override
    public String complexTypeClassName(Schema.Field fieldSchema, int level) 
            throws Exception {
        return fieldSchema.name();
    }
}
