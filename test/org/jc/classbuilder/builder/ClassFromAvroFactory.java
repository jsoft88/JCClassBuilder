/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jc.classbuilder.builder;

/**
 *
 * @author cespedjo
 */
public class ClassFromAvroFactory implements FactoryClassBuilder {

    private static ClassFromAvro instance;
    
    @Override
    public synchronized ClassBuilder getInstance() {
        if (ClassFromAvroFactory.instance == null) {
            ClassFromAvroFactory.instance = new ClassFromAvro();
        }
        
        return ClassFromAvroFactory.instance;
    }
    
}
