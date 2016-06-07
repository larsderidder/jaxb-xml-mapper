package com.github.larsderidder.xml;

import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import static org.junit.Assert.*;

public class XmlMapperTest {

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TestUser {
        private String name;
        private String email;

        public TestUser() {}

        public TestUser(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() { return name; }
        public String getEmail() { return email; }
    }

    @Test
    public void testMarshaling() throws Exception {
        JAXBContext context = JAXBContext.newInstance(TestUser.class);
        XmlMapper mapper = new XmlMapper(context);

        TestUser user = new TestUser("John", "john@example.com");
        String xml = mapper.toXml(user);

        assertTrue(xml.contains("<name>John</name>"));
        assertTrue(xml.contains("<email>john@example.com</email>"));
    }

    @Test
    public void testUnmarshaling() throws Exception {
        JAXBContext context = JAXBContext.newInstance(TestUser.class);
        XmlMapper mapper = new XmlMapper(context);

        String xml = "<?xml version=\"1.0\"?><testUser><name>Jane</name><email>jane@example.com</email></testUser>";
        TestUser user = mapper.fromXml(xml, TestUser.class);

        assertEquals("Jane", user.getName());
        assertEquals("jane@example.com", user.getEmail());
    }

    @Test
    public void testFormattedOutput() throws Exception {
        JAXBContext context = JAXBContext.newInstance(TestUser.class);
        XmlMapper mapper = new XmlMapper(context);

        TestUser user = new TestUser("John", "john@example.com");
        String xml = mapper.toXml(user, true);

        assertTrue(xml.contains("\n"));
    }

    @Test(expected = XmlMappingException.class)
    public void testInvalidXml() throws Exception {
        JAXBContext context = JAXBContext.newInstance(TestUser.class);
        XmlMapper mapper = new XmlMapper(context);

        mapper.fromXml("invalid xml", TestUser.class);
    }
}
