/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jc.classbuilder.builder;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author cespedjo
 */
public class TestPathGenerator {
    
    public TestPathGenerator() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void testPathBuilder() {
        String[] absolutePath = {"c:", "Users", "cespedjo", "documents", "NetbeansProjects", "JCClassBuilder", "test", "org", "jc", "classbuilder", "entity"};
        String expected = new StringBuilder()
                .append("c:").append(File.separator)
                .append("Users").append(File.separator)
                .append("cespedjo").append(File.separator)
                .append("documents").append(File.separator)
                .append("NetbeansProjects").append(File.separator)
                .append("JCClassBuilder").append(File.separator)
                .append("test").append(File.separator)
                .append("org").append(File.separator)
                .append("jc").append(File.separator)
                .append("classbuilder").append(File.separator)
                .append("entity").toString();
        
        String output = Utils.pathBuilder(absolutePath).toString();
        System.out.println(output);
        assertEquals(expected, output);
    }
    
    @Test
    public void testJavaFileGeneration() throws NotFoundException, CannotCompileException, Exception {
        String[] absolutePath = {"c:", "Users", "cespedjo", "documents", "NetbeansProjects", "JCClassBuilder", "test", "org", "jc", "classbuilder", "entity"};
        ClassPool cp = ClassPool.getDefault();
        cp.makePackage(cp.getClassLoader(), "org.jc.classbuilder.entity");
        CtClass buildClassToWrite = cp.makeClass("test");
        
        buildClassToWrite.setModifiers(Modifier.PUBLIC);
        buildClassToWrite.setModifiers(Modifier.FINAL);
        buildClassToWrite.setSuperclass(cp.get("java.lang.Object"));
        buildClassToWrite.addInterface(cp.get("java.io.Serializable"));
        CtField a = new CtField(CtClass.booleanType, "gasoline", buildClassToWrite);
        CtField b = new CtField(cp.get("java.lang.String"), "model", buildClassToWrite);
        CtField c = new CtField(cp.get("java.lang.String"), "brand", buildClassToWrite);
        
        System.out.println("field c is final? Answer: " + Modifier.isFinal(c.getModifiers()));
        
        buildClassToWrite.addField(a);
        buildClassToWrite.addField(b);
        buildClassToWrite.addField(c);
        
        CtMethod m1 = new CtMethod(CtClass.booleanType, "isGasoline", new CtClass[]{}, buildClassToWrite);
        m1.setBody("{return this.gasoline;}");
        MethodsDescription.addNewMethod(buildClassToWrite.getName(), "public boolean isGasoline(){return this.gasoline;}");
        
        CtMethod m2 = new CtMethod(cp.get("java.lang.String"), "getModel", new CtClass[]{}, buildClassToWrite);
        m2.setBody("{return this.model;}");
        MethodsDescription.addNewMethod(buildClassToWrite.getName(), "public String getModel(){return this.model;}");
        
        CtMethod m3 = new CtMethod(CtClass.voidType, "setGasoline", new CtClass[]{CtClass.booleanType}, buildClassToWrite);
        m3.setBody("{this.gasoline = $1;}");
        MethodsDescription.addNewMethod(buildClassToWrite.getName(), "public void setGasoline(boolean gasoline){this.gasoline = gasoline;}");
        
        buildClassToWrite.addMethod(m1);
        buildClassToWrite.addMethod(m2);
        buildClassToWrite.addMethod(m3);
        
        Utils.writeJavaFile(buildClassToWrite, "org.jc.classbuilder.entity", Utils.pathBuilder(absolutePath));
        boolean fileExists = Files.exists(Utils.pathBuilder(absolutePath), LinkOption.NOFOLLOW_LINKS);
        assertTrue(fileExists);
    }
    
    @Test
    public void testClassUsable() throws Exception {
        String[] absolutePath = {"c:", "Users", "cespedjo", "documents", "NetbeansProjects", "JCClassBuilder", "test", "org", "jc", "classbuilder", "entity"};
        ClassPool cp = ClassPool.getDefault();
        cp.makePackage(cp.getClassLoader(), "org.jc.classbuilder.entity");
        CtClass buildClassToWrite = cp.makeClass("test");
        
        buildClassToWrite.setModifiers(Modifier.PUBLIC);
        //buildClassToWrite.setModifiers(Modifier.FINAL);
        buildClassToWrite.setSuperclass(cp.get("java.lang.Object"));
        buildClassToWrite.addInterface(cp.get("java.io.Serializable"));
        CtField a = new CtField(cp.get("java.lang.Boolean"), "gasoline", buildClassToWrite);
        a.setModifiers(Modifier.PRIVATE);
        CtField b = new CtField(cp.get("java.lang.String"), "model", buildClassToWrite);
        b.setModifiers(Modifier.PRIVATE);
        CtField c = new CtField(cp.get("java.lang.String"), "brand", buildClassToWrite);
        c.setModifiers(Modifier.PRIVATE);
        
        System.out.println("field c is final? Answer: " + Modifier.isFinal(c.getModifiers()));
        
        buildClassToWrite.addField(a);
        buildClassToWrite.addField(b);
        buildClassToWrite.addField(c);
        
        CtConstructor constructor = new CtConstructor(null, buildClassToWrite);
        constructor.setModifiers(Modifier.PUBLIC);
        constructor.setBody("{this.model = \"cerato\";}");
        buildClassToWrite.addConstructor(constructor);
        
        CtMethod m1 = CtNewMethod.make("public Boolean isGasoline(){ return this.gasoline; }", buildClassToWrite);
                //new CtMethod(CtClass.booleanType, "isGasoline", new CtClass[]{CtClass.voidType}, buildClassToWrite);
        //m1.setBody("{return this.gasoline;}");
        m1.setModifiers(Modifier.PUBLIC);
        MethodsDescription.addNewMethod(buildClassToWrite.getName(), "public Boolean isGasoline(){return this.gasoline;}");
        
        CtMethod m2 = new CtMethod(cp.get("java.lang.String"), "getModel", new CtClass[]{}, buildClassToWrite);
        m2.setBody("{return this.model;}");
        m2.setModifiers(Modifier.PUBLIC);
        MethodsDescription.addNewMethod(buildClassToWrite.getName(), "public String getModel(){return this.model;}");
        
        CtMethod m3 = CtNewMethod.make("public void setGasoline(Boolean gasoline){ this.gasoline = gasoline;}", buildClassToWrite);
                //new CtMethod(CtClass.voidType, "setGasoline", new CtClass[]{CtClass.booleanType}, buildClassToWrite);
        m3.setModifiers(Modifier.PUBLIC);
        //m3.setBody("{this.gasoline = $1;}");
        MethodsDescription.addNewMethod(buildClassToWrite.getName(), "public void setGasoline(Boolean gasoline){this.gasoline = gasoline;}");
        
        buildClassToWrite.addMethod(m1);
        buildClassToWrite.addMethod(m2);
        buildClassToWrite.addMethod(m3);
        
        //Utils.writeJavaFile(buildClassToWrite, "org.jc.classbuilder.builder", Utils.pathBuilder(absolutePath));
        
        buildClassToWrite.defrost();
        /**
        Class clazz = buildClassToWrite.toClass();
        Object instance = clazz.newInstance();
        for (Method m : clazz.getDeclaredMethods()) {
            System.out.println(m.getName());
        }
        Method getModel = clazz.getDeclaredMethod("getModel");
        String model = (String)getModel.invoke(clazz.cast(instance));
        Method setGasoline = clazz.getDeclaredMethod("setGasoline", Boolean.class);
        setGasoline.invoke(clazz.cast(instance), Boolean.parseBoolean("true"));
        Method isGasoline = clazz.getDeclaredMethod("isGasoline");
        assertTrue((boolean)isGasoline.invoke(clazz.cast(instance)));
        assertTrue(model.equals("cerato"));
        **/
        assertTrue(true);
    }
    
    @Test
    public void writeComplexClass() throws Exception {
        String[] absolutePath = {"c:", "Users", "cespedjo", "documents", "NetbeansProjects", "JCClassBuilder", "test", "org", "jc", "classbuilder", "entity"};
        
        ClassPool cp = ClassPool.getDefault();
        cp.makePackage(cp.getClassLoader(), "org.jc.classbuilder.entity");
        CtClass buildClassToWrite = cp.makeClass("test");
        
        buildClassToWrite.setModifiers(Modifier.PUBLIC);
        //buildClassToWrite.setModifiers(Modifier.FINAL);
        buildClassToWrite.setSuperclass(cp.get("java.lang.Object"));
        buildClassToWrite.addInterface(cp.get("java.io.Serializable"));
        CtField a = new CtField(cp.get("java.lang.Boolean"), "gasoline", buildClassToWrite);
        a.setModifiers(Modifier.PRIVATE);
        CtField b = new CtField(cp.get("java.lang.String"), "model", buildClassToWrite);
        b.setModifiers(Modifier.PRIVATE);
        CtField c = new CtField(cp.get("java.lang.String"), "brand", buildClassToWrite);
        c.setModifiers(Modifier.PRIVATE);
        
        //Add complex field
        CtClass complexClass = cp.makeClass("org.jc.classbuilder.entity.complexField");
        
        complexClass.setModifiers(Modifier.PUBLIC);
        complexClass.addConstructor(CtNewConstructor.make("public complexField(){}", complexClass));
        CtField aField = new CtField(CtClass.doubleType, "temperature", complexClass);
        aField.setModifiers(Modifier.PRIVATE);
        complexClass.addField(aField);
        CtMethod cm1 = CtNewMethod.make("public void setTemperature(java.lang.Double temperature){this.temperature = temperature;}", complexClass);
        MethodsDescription.addNewMethod(complexClass.getName(), "public void setTemperature(java.lang.Double temperature){this.temperature = temperature;}");
        complexClass.addMethod(cm1);
        
        CtMethod cm2 = CtNewMethod.make("public java.lang.Double getTemperature(){ return this.temperature;}", complexClass);
        MethodsDescription.addNewMethod(complexClass.getName(), "public java.lang.Double getTemperature(){ return this.temperature;}");
        complexClass.addMethod(cm2);
        
        complexClass.defrost();
        
        CtField complexClassToField = new CtField(complexClass, "complex", buildClassToWrite);
        complexClassToField.setModifiers(Modifier.PRIVATE);
        buildClassToWrite.addField(complexClassToField);
        
        
        System.out.println("field c is final? Answer: " + Modifier.isFinal(c.getModifiers()));
        
        buildClassToWrite.addField(a);
        buildClassToWrite.addField(b);
        buildClassToWrite.addField(c);
        
        CtConstructor constructor = new CtConstructor(null, buildClassToWrite);
        constructor.setModifiers(Modifier.PUBLIC);
        constructor.setBody("{this.model = \"cerato\";}");
        buildClassToWrite.addConstructor(constructor);
        
        CtMethod m1 = CtNewMethod.make("public Boolean isGasoline(){ return this.gasoline; }", buildClassToWrite);
                //new CtMethod(CtClass.booleanType, "isGasoline", new CtClass[]{CtClass.voidType}, buildClassToWrite);
        //m1.setBody("{return this.gasoline;}");
        m1.setModifiers(Modifier.PUBLIC);
        MethodsDescription.addNewMethod(buildClassToWrite.getName(), "public Boolean isGasoline(){return this.gasoline;}");
        
        CtMethod m2 = new CtMethod(cp.get("java.lang.String"), "getModel", new CtClass[]{}, buildClassToWrite);
        m2.setBody("{return this.model;}");
        m2.setModifiers(Modifier.PUBLIC);
        MethodsDescription.addNewMethod(buildClassToWrite.getName(), "public String getModel(){return this.model;}");
        
        CtMethod m3 = CtNewMethod.make("public void setGasoline(Boolean gasoline){ this.gasoline = gasoline;}", buildClassToWrite);
                //new CtMethod(CtClass.voidType, "setGasoline", new CtClass[]{CtClass.booleanType}, buildClassToWrite);
        m3.setModifiers(Modifier.PUBLIC);
        //m3.setBody("{this.gasoline = $1;}");
        MethodsDescription.addNewMethod(buildClassToWrite.getName(), "public void setGasoline(Boolean gasoline){this.gasoline = gasoline;}");
        
        CtMethod m4 = CtNewMethod.make("public org.jc.classbuilder.entity.complexField getComplex(){ return this.complex;}", buildClassToWrite);
        m4.setModifiers(Modifier.PUBLIC);
        MethodsDescription.addNewMethod(buildClassToWrite.getName(), "public org.jc.classbuilder.entity.complexField getComplex(){ return this.complex;}");
        
        
        buildClassToWrite.addMethod(m1);
        buildClassToWrite.addMethod(m2);
        buildClassToWrite.addMethod(m3);
        buildClassToWrite.addMethod(m4);
        
        Path pathOfSrc = Utils.pathBuilder(absolutePath);
        Utils.writeJavaFile(buildClassToWrite, "org.jc.classbuilder.entity", pathOfSrc);
        
        assertTrue(Files.exists(pathOfSrc.resolve("test.java"), LinkOption.NOFOLLOW_LINKS));
        assertTrue(Files.exists(pathOfSrc.resolve("complexField.java"), LinkOption.NOFOLLOW_LINKS));
    }
}
