/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jc.classbuilder.builder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
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
public class TestEnumCreator {
    
    public TestEnumCreator() {
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
    /*
    @Test
    public void testEnum() {
        String[] absolutePath = {"c:", "Users", "cespedjo", "documents", "NetbeansProjects", "JCClassBuilder", "test", "org", "jc", "classbuilder", "entity"};
        String schema = "{";
        schema += "\"name\":\"deviceids\",";
        schema += "\"type\": \"record\",";
        schema += "\"fields\": [";
        schema += "{\"name\": \"color\",";
        schema += "\"type\": {";
        schema += "\"type\": \"enum\",";
        schema += "\"name\": \"colors\",";
        schema += "\"symbols\": [\"red\",\"blue\",\"green\"]";
        schema += "}}]}";
        
        Schema sc = new Schema.Parser().parse(schema);
        ClassBuilder generate = new ClassFromAvroFactory().getInstance();
        generate.init(sc, Utils.pathBuilder(absolutePath), Utils.pathBuilder(absolutePath), "org.jc.classbuilder.entity");
        try {
            Class clazz = generate.generateClass(null);
            assertEquals("org.jc.classbuilder.entity.deviceids", clazz.getName());
        } catch (Exception ex) {
            Logger.getLogger(TestEnumCreator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }*/
    
    @Test
    public void testingForOrd() throws IOException, Exception {
        String[] absolutePath = {"/", "home", "cloudera", "NetbeansProjects", "JCClassBuilder", "test", "org", "jc", "classbuilder", "entity"};
        String avroJsonSchema = Files.readAllLines(
                Paths.get("/home/cloudera/json_schema.json"), 
                Charset.forName("UTF-8")).get(0);
        
        Schema avroSc = new Schema.Parser().parse(avroJsonSchema);
        ClassBuilder generate = new ClassFromAvroFactory().getInstance();
        generate.init(avroSc, Utils.pathBuilder(absolutePath), Utils.pathBuilder(absolutePath), "org.jc.classbuilder.entity");
        Class clazz = generate.generateClass(null, null, "{super();}", "public DBADMIN_ORD_MODIF{super();}");
        assertTrue(clazz != null);
    }
}
