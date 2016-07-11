/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jc.classbuilder.builder;

import java.nio.file.Path;
import java.util.List;
import java.util.Stack;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.Modifier;

/**
 *
 * @author cespedjo
 * @param <S> Class representing a Schema (e.g. avro schema, parquet schema).
 * This schema must represent every attribute's name, type and in case of Maps,
 * arrays, lists, schema must also have a way to tell elements types.
 * @param <F> Class representing a Field (e.g. avro schema would be Schema.Field)
 */
public abstract class ClassBuilder<S, F> implements QuerySchema<S, F>{
    
    protected Stack<List<F>> fieldsStack; 
    
    protected Stack<Integer> indexStack;
    
    private static final Object sharedLock = new Object();
    
    protected Path absoluteClassPath;
    
    protected Path absoluteSrcPath;
    
    protected String packageName;
    
    protected S fullSchema;
    
    public void init(S fullSchema, Path absoluteClassPath, Path absoluteSrcPath, String packageName) {
        this.absoluteClassPath = absoluteClassPath;
        this.absoluteSrcPath = absoluteSrcPath;
        this.packageName = packageName;
        this.fieldsStack = new Stack<>();
        this.indexStack = new Stack<>();
        this.fullSchema = fullSchema;
    }
    
    protected boolean hasSubTypes(S fieldSchema) throws Exception {
        return true;
    }
    
    protected CtClass handleComplexTypeWithoutSubTypes(F field, S fieldSchema) 
            throws Exception {
        return null;
    }
    
    protected List<F> getFieldList(S schema) throws Exception{
        throw new Exception("Method must be overriden.");
    }
    
    protected final void pushNewComplexType(S schema) throws Exception {
        this.fieldsStack.push(this.getFieldList(schema));
        this.indexStack.push(0);
    }
    
    protected void addMethods(CtField field, CtClass fullClass) throws Exception{
        throw new Exception("Method not implemented: addMethods(CtClass c).");
    }

    @Override
    public String getSchemaName(S schema) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public F peekNextField() throws Exception {
        if (this.indexStack.peek() + 1 < this.fieldsStack.peek().size()) {
            int pos = this.indexStack.pop();
            return this.fieldsStack.peek().get(pos + 1);
        }
        
        return null;
    }
    
    /**
     * Auxiliar method in charge of actually generating the class. The first time
     * it is called, that is, when the call does not correspond to a recursive
     * call, field might be null since we´re talking about the full schema.
     * @param schema field schema. The first time this is the full schema.
     * @param field the field itself, from which a schema may be derived.
     * @param level an int indicating whether it is the whole class or a class 
     * inside the whole class.
     * @return CtClass object which may be written to a file as src or to class
     * file.
     * @throws Exception When anything goes wrong in the process of creating class.
     */
    protected final CtClass builder(S schema, F field, int level) throws Exception {
        synchronized(ClassBuilder.sharedLock) {
            ClassPool cPool = ClassPool.getDefault();
            
            String complexName = field == null ? 
                    this.getSchemaName(schema) : this.complexTypeClassName(field, level);
            //full schema provided the first time the method is invoked, so field
            //is null. Retrieve next field to add to class.
            S type;
            if (field == null) {
                field = this.getNextField();
                type = this.getFieldTypeAsSchema(field);
            } else {
                F peekNextField = this.peekNextField();
                type = this.getFieldTypeAsSchema(field);
                if (peekNextField == null) {
                    CtClass complex = this.handleComplexTypeWithoutSubTypes(field, type);
                    return complex;
                }
            }
            
            CtClass retVal = cPool
                    .makeClass(this.packageName + "." + complexName);
            
            
            while (type != null) {
                CtClass attr;
                if (this.isComplexType(type)) {
                    if (this.hasSubTypes(type)) {
                        this.pushNewComplexType(type);
                    }
                    
                    attr = builder(type, field, level + 1);
                } else {
                    try {
                        String fqcnJavaType = this.getFieldType(field);
                        attr = cPool.get(fqcnJavaType);
                        

                    } catch (Exception ex) {
                        throw new Exception("Invalid datatype. " + ex.getMessage());
                    }
                }
                
                CtField ctField = new CtField(attr, this.getFieldName(field), retVal); 
                retVal.addField(ctField);
                addMethods(ctField, retVal);

                field = this.getNextField();
                type = field == null? null : this.getFieldTypeAsSchema(field);
            }
            
            return retVal;
        }
    }

    @Override
    public String complexTypeClassName(F fieldSchema, int level) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Invoke this method to generate the class for a given schema. This class 
     * will be generated and written to the package passed as argument. The class
     * name is the name of the schema and cannot be overridden.
     * @param extendsClassFullyQualifiedName fully qualified class name from which
     * the newly created class will extend. Use null to extend from java.lang.Object
     * @param fqArgsClassName Fully qualified args class name as String array. 
     * @param bodyOfClassFile body of constructor where arg1, arg2,...,argn is represented by
     * $1, $2,..., $n respectively. Example: {super($1,$2);this.fieldWhatever = $2;}
     * @param bodyOfJavaFile body of constructor for source java file. Example:
     * public ClassName(String arg1, ... , Object argn) {super(arg1,arg2); this.fieldWhatever = arg2;}
     * @return the created class.
     * @throws Exception If class generation fails.
     */
    public final Class generateClass(
            String extendsClassFullyQualifiedName, 
            String[] fqArgsClassName, 
            String bodyOfClassFile,
            String bodyOfJavaFile) throws Exception{
        try {
            this.pushNewComplexType(this.fullSchema);
            CtClass fullClass = 
                    this.builder(this.fullSchema, null, 0);
            
            CtClass[] args = null;
            if (fqArgsClassName != null && fqArgsClassName.length > 0) {
                args = new CtClass[fqArgsClassName.length];
                for (int i = 0; i < fqArgsClassName.length; ++i) {
                    args[i] = ClassPool.getDefault().get(fqArgsClassName[i]);
                }
            }
            
            if (extendsClassFullyQualifiedName == null) {
                extendsClassFullyQualifiedName = "java.lang.Object";
            }
            
            fullClass.setSuperclass(ClassPool.getDefault().get(extendsClassFullyQualifiedName));
            
            CtConstructor defaultConstructor = new CtConstructor(args, fullClass);
            defaultConstructor.setBody(bodyOfClassFile);
            defaultConstructor.setModifiers(Modifier.PUBLIC);
            MethodsDescription.addNewMethod(fullClass.getName(), bodyOfJavaFile);
            fullClass.addConstructor(defaultConstructor);
            
            //fullClass.writeFile();
            //System.out.println(fullClass.toString());
            fullClass.defrost();
            Utils.writeJavaFile(fullClass, this.packageName, this.absoluteSrcPath);
            fullClass.writeFile(absoluteClassPath.toString());
            
            return fullClass.toClass();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception("Error while creating class from schema.");
        }
    }   
    
    @Override
    public final F getNextField() throws Exception {
        if (this.indexStack.peek() < this.fieldsStack.peek().size()) {
            int pos = this.indexStack.pop();
            this.indexStack.push(pos + 1);
            return this.fieldsStack.peek().get(pos);
        }
        
        return null;
    }

    @Override
    public String getFieldType(F fieldSchema) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getFieldName(F fieldSchema) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public S getElementsType(S fieldSchema) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isComplexType(S fieldSchema) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public abstract S getFieldTypeAsSchema(F fieldSchema) throws Exception;
    
}
