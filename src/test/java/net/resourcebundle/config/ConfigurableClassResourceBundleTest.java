package net.resourcebundle.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.resoucebundle.config.ConfigurableClassResourceBundle;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConfigurableClassResourceBundleTest {
    static class Person {
        private String name;
        private int age;
        private Person spouse;
        Person() { }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public Person getSpouse() { return spouse; }
        public void setSpouse(Person spouse) { this.spouse = spouse; }
    }
    @Test
    void personTestWithJson() {
        ClassLoader classLoader = getClass().getClassLoader();
        ObjectMapper objectMapper = new ObjectMapper();;
        Function<InputStream, Person> converter = (inputStream) -> {
            try {
                return objectMapper.readValue(inputStream, Person.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        ConfigurableClassResourceBundle.Control<Person> control =
                new ConfigurableClassResourceBundle.Control<>(converter, "json");
        ConfigurableClassResourceBundle<Person> bundle = ConfigurableClassResourceBundle.getBundle(
                "person", Locale.US, classLoader, control);
        Person person = bundle.getValue();
        assertEquals(person.getName(), "John Smith");
        assertEquals(person.getAge(), 30);
        assertEquals(person.getSpouse().getName(), "Jane Smith");
        assertEquals(person.getSpouse().getAge(), 28);
        assertNull(person.getSpouse().getSpouse());
    }
    @Test
    void personTestWithYaml() {
        ClassLoader classLoader = getClass().getClassLoader();
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        Function<InputStream, Person> converter = (inputStream) -> {
            try {
                return yamlMapper.readValue(inputStream, Person.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        ConfigurableClassResourceBundle.Control<Person> control =
                new ConfigurableClassResourceBundle.Control<>(converter, "yaml");
        ConfigurableClassResourceBundle<Person> bundle = ConfigurableClassResourceBundle.getBundle(
            "person", Locale.UK, classLoader, control);
        Person person = bundle.getValue();
        assertEquals(person.getName(), "John Smith");
        assertEquals(person.getAge(), 30);
        assertEquals(person.getSpouse().getName(), "Jane Smith");
        assertEquals(person.getSpouse().getAge(), 28);
        assertNull(person.getSpouse().getSpouse());
    }
}
