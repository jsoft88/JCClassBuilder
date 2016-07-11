/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jc.classbuilder.builder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;
import javassist.bytecode.SourceFileAttribute;

/**
 *
 * @author cespedjo
 */
public class Utils {
    
    public static Path pathBuilder(String[] elements) {
        StringBuilder sb = new StringBuilder();
        for (String element : elements) {
            if (sb.length() > 0) {
                sb.append(File.separator);
            }
            sb.append(element);
        }
        
        return Paths.get(sb.toString());
    }
    
    private static void modifierAppender(int modifiers, StringBuilder sb) {
        if (Modifier.isPrivate(modifiers)) {
            sb.append("private").append(" ");
        } else if (Modifier.isProtected(modifiers)) {
            sb.append("protected").append(" ");
        } else {
            sb.append("public").append(" ");
        }

        if (Modifier.isFinal(modifiers)) {
            sb.append("final").append(" ");
        }

        if (Modifier.isStatic(modifiers)) {
            sb.append("static").append(" ");
        }
        
        if (Modifier.isAbstract(modifiers)) {
            sb.append("abstract").append(" ");
        }
    }
    
    private static boolean isAnotherClass(CtClass ctClassField, String packageName) {
        String pn = ctClassField.getPackageName();
        return pn != null && pn.startsWith(packageName);
    }
    
    public static synchronized void writeJavaFile(CtClass ctClass, String packageName, Path fileAbsoluteSrcPath) 
            throws Exception {
        StringBuilder sb = new StringBuilder();
        if (!Files.exists(fileAbsoluteSrcPath, LinkOption.NOFOLLOW_LINKS)) {
            Files.createDirectories(fileAbsoluteSrcPath);
        }
        
        String packageInfo = packageName.concat(";");
        sb.append("package").append(" ").append(packageInfo).append("\n");
        for (Object attr : ctClass.getClassFile().getAttributes()) {
            SourceFileAttribute sa = (SourceFileAttribute)attr;
            sb.append(new String(sa.get())).append("\n");
        }
        
        Utils.modifierAppender(ctClass.getModifiers(), sb);
        
        if (Modifier.isInterface(ctClass.getModifiers())) {
            sb.append("interface").append(" ");
        } else if (Modifier.isEnum(ctClass.getModifiers())) {
            sb.append("enum").append(" ");
        } else {
            sb.append("class").append(" ");
        }
        
        sb.append(ctClass.getSimpleName()).append(" ");
        
        sb.append("extends").append(" ").append(ctClass.getSuperclass().getName());
        sb.append(" ");
        CtClass[] interfaces = ctClass.getInterfaces();
        if (interfaces != null && interfaces.length != 0) {
            sb.append("implements").append(" ");
            boolean addComma = false;
            for (CtClass impl : interfaces) {
                if (addComma) {
                    sb.append(",").append(" ");
                }
                sb.append(impl.getName());
                addComma = true;
            }
        }
        
        sb.append("{").append("\n");
        boolean addNewLine = false;
        
        for (CtField classAttribute : ctClass.getDeclaredFields()) {
            sb.append("\t");
            Utils.modifierAppender(classAttribute.getModifiers(), sb);
            if (Utils.isAnotherClass(classAttribute.getType(), packageName)) {
                Utils.writeJavaFile(classAttribute.getType(), packageName, fileAbsoluteSrcPath);
                sb.append(packageName).append(".").append(classAttribute.getName()).append(" ");
            } else {
                sb.append(classAttribute.getType().getName()).append(" ");
            }
            sb.append(classAttribute.getName()).append(";");
            sb.append("\n");
        }
        
        List<String> declaredMethods = MethodsDescription.allMethodsFromClass(ctClass.getName());
        if (declaredMethods != null && !declaredMethods.isEmpty()) {
            for (String aMethod : declaredMethods) {
                if (addNewLine) {
                    sb.append("\n");
                }
                sb.append("\t").append(aMethod);
                addNewLine = true;
            }
        }
        
        sb.append("\n").append("}");
        Path pathWhereJavaFileReside = fileAbsoluteSrcPath.resolve(ctClass.getSimpleName() + ".java");
        if (!Files.exists(pathWhereJavaFileReside, LinkOption.NOFOLLOW_LINKS)) {
            Files.createFile(pathWhereJavaFileReside);
        }
        Files.write(pathWhereJavaFileReside, sb.toString().getBytes(), StandardOpenOption.WRITE);
    }
}
