/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jc.classbuilder.builder;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.avro.Schema;
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
public class TestCreation {
    
    private ClassBuilder instance;
    
    private Schema avroSchemaSimple;
    
    private Schema avroSchemaOneComplexType;
    
    private Schema avroSchemaArrayOfComplexType;
    
    private Schema avroSchemaGenerateSubClass;
    
    public TestCreation() {
        this.instance = new ClassFromAvroFactory().getInstance();
        String schema = "{";
        schema += "\"name\":\"deviceids\",";
        schema += "\"type\": \"record\",";
        schema += "\"fields\": [";
        schema += "{\"name\": \"deviceId\",";
        schema += "\"type\": \"string\"},";
        schema += "{\"name\": \"deviceName\",";
        schema += "\"type\": \"string\"},";
        schema += "]}";
        
        this.avroSchemaSimple = new Schema.Parser().parse(schema);
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
    public void generateClass() {
        /*try {
            this.instance.generateClass(this.avroSchemaSimple, "org.jc.classbuilder.entity");
        } catch (Exception ex) {
            Logger.getLogger(TestCreation.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }
}
