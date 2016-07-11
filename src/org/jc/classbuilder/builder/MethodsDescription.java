/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jc.classbuilder.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author cespedjo
 */
public class MethodsDescription {
    
    private static HashMap<String, ArrayList<String>> methodsInfo;
    
    public static void addNewMethod(String fqcn, String methodBodyAsString) {
        if (MethodsDescription.methodsInfo == null) {
            MethodsDescription.methodsInfo = new HashMap<>();
        }
        ArrayList<String> methodsFromClass = MethodsDescription.methodsInfo.remove(fqcn);
        if (methodsFromClass == null ) {
            MethodsDescription.methodsInfo.put(fqcn, new ArrayList<>(Arrays.asList(methodBodyAsString)));
        } else {
            methodsFromClass.add(methodBodyAsString);
            MethodsDescription.methodsInfo.put(fqcn, methodsFromClass);
        }
    }
    
    public static List<String> allMethodsFromClass(String fqcn) {
        if (MethodsDescription.methodsInfo == null || MethodsDescription.methodsInfo.isEmpty()) {
            return null;
        }
        
        return MethodsDescription.methodsInfo.get(fqcn);
    }
}
