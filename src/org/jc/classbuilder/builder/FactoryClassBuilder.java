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
public interface FactoryClassBuilder {
    
    /**
     * Get an instance of ClassBuilder. Must be thread safe.
     * @return an instance of ClassBuilder.
     */
    ClassBuilder getInstance();
}
