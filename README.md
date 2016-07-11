# JCClassBuilder
Library for generating classes from schema. It provides interfaces for making process easy.
It is intended to generate a class on runtime, by taking as argument some schema as json. Implement interfaces adjusting each method to query your own schema. For example, this could be used to generate a class from an avro schema just as avro-tools does, however, this can be used while a program is running.

Implement <b>FactoryClassBuilder</b> for obtaining an instance of class extending ClassBuilder.
FactoryClassBuilder#getInstance() must be thread-safe.

Extend <b>ClassBuilder</b> for generating a class according to your schema. Most methods are documented inside java file, plus under tests a full example for generating a class based on an avro schema can be found.

