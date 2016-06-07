package com.github.larsderidder.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Utility for XML marshaling and unmarshaling using JAXB with optional XSD validation.
 * Provides type-safe conversion between XML strings and Java objects.
 */
public class XmlMapper {

    private static final Logger LOG = LoggerFactory.getLogger(XmlMapper.class);

    private final JAXBContext context;
    private final Schema schema;

    /**
     * Creates an XmlMapper for the given package without schema validation.
     *
     * @param packageName the package containing JAXB-annotated classes
     * @throws XmlMappingException if JAXBContext creation fails
     */
    public XmlMapper(String packageName) {
        this(packageName, null);
    }

    /**
     * Creates an XmlMapper for the given package with optional XSD schema validation.
     *
     * @param packageName the package containing JAXB-annotated classes
     * @param schemaLocation resource path to XSD schema file, or null to disable validation
     * @throws XmlMappingException if JAXBContext or Schema creation fails
     */
    public XmlMapper(String packageName, String schemaLocation) {
        try {
            this.context = JAXBContext.newInstance(packageName);
            this.schema = schemaLocation != null ? loadSchema(schemaLocation) : null;
        } catch (JAXBException e) {
            throw new XmlMappingException("Failed to create JAXB context for package: " + packageName, e);
        }
    }

    /**
     * Creates an XmlMapper using an existing JAXBContext.
     *
     * @param context the JAXBContext to use
     */
    public XmlMapper(JAXBContext context) {
        this(context, null);
    }

    /**
     * Creates an XmlMapper using an existing JAXBContext with optional schema validation.
     *
     * @param context the JAXBContext to use
     * @param schemaLocation resource path to XSD schema file, or null to disable validation
     */
    public XmlMapper(JAXBContext context, String schemaLocation) {
        this.context = context;
        this.schema = schemaLocation != null ? loadSchema(schemaLocation) : null;
    }

    /**
     * Converts XML string to object of the specified type without validation.
     *
     * @param <T> the expected type
     * @param xml the XML string
     * @param clazz the target class
     * @return the unmarshaled object
     * @throws XmlMappingException if unmarshaling fails or type doesn't match
     */
    public <T> T fromXml(String xml, Class<T> clazz) {
        return fromXml(xml, clazz, false);
    }

    /**
     * Converts XML string to object of the specified type with optional validation.
     *
     * @param <T> the expected type
     * @param xml the XML string
     * @param clazz the target class
     * @param validate whether to validate against the schema (requires schema to be configured)
     * @return the unmarshaled object
     * @throws XmlMappingException if unmarshaling fails or type doesn't match
     */
    @SuppressWarnings("unchecked")
    public <T> T fromXml(String xml, Class<T> clazz, boolean validate) {
        Object object = fromXml(xml, validate);

        if (object != null && clazz.isInstance(object)) {
            return (T) object;
        }

        throw new XmlMappingException("XML does not match expected type: " + clazz.getName());
    }

    /**
     * Converts XML string to object without type checking.
     *
     * @param xml the XML string
     * @param validate whether to validate against the schema
     * @return the unmarshaled object
     * @throws XmlMappingException if unmarshaling fails
     */
    public Object fromXml(String xml, boolean validate) {
        try {
            Unmarshaller unmarshaller = context.createUnmarshaller();

            if (validate) {
                if (schema == null) {
                    throw new XmlMappingException("Schema validation requested but no schema configured");
                }
                unmarshaller.setSchema(schema);
            }

            return unmarshaller.unmarshal(new StringReader(xml));

        } catch (JAXBException e) {
            LOG.error("Failed to unmarshal XML: {}", e.getMessage(), e);
            throw new XmlMappingException("Failed to unmarshal XML: " + e.getMessage(), e);
        }
    }

    /**
     * Converts XML input stream to object of the specified type.
     *
     * @param <T> the expected type
     * @param inputStream the XML input stream
     * @param clazz the target class
     * @param validate whether to validate against the schema
     * @return the unmarshaled object
     * @throws XmlMappingException if unmarshaling fails
     */
    @SuppressWarnings("unchecked")
    public <T> T fromXml(InputStream inputStream, Class<T> clazz, boolean validate) {
        try {
            Unmarshaller unmarshaller = context.createUnmarshaller();

            if (validate && schema != null) {
                unmarshaller.setSchema(schema);
            }

            Object object = unmarshaller.unmarshal(inputStream);

            if (object != null && clazz.isInstance(object)) {
                return (T) object;
            }

            throw new XmlMappingException("XML does not match expected type: " + clazz.getName());

        } catch (JAXBException e) {
            LOG.error("Failed to unmarshal XML: {}", e.getMessage(), e);
            throw new XmlMappingException("Failed to unmarshal XML: " + e.getMessage(), e);
        }
    }

    /**
     * Converts object to XML string.
     *
     * @param object the object to marshal
     * @return the XML string
     * @throws XmlMappingException if marshaling fails
     */
    public String toXml(Object object) {
        return toXml(object, false);
    }

    /**
     * Converts object to XML string with optional formatting.
     *
     * @param object the object to marshal
     * @param formatted whether to format the output with indentation
     * @return the XML string
     * @throws XmlMappingException if marshaling fails
     */
    public String toXml(Object object, boolean formatted) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            Marshaller marshaller = context.createMarshaller();

            if (formatted) {
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            }

            marshaller.marshal(object, outputStream);
            return new String(outputStream.toByteArray(), UTF_8);

        } catch (JAXBException e) {
            LOG.error("Failed to marshal object to XML: {}", e.getMessage(), e);
            throw new XmlMappingException("Failed to marshal object to XML: " + e.getMessage(), e);
        }
    }

    private Schema loadSchema(String schemaLocation) {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            URL schemaUrl = getClass().getClassLoader().getResource(schemaLocation);

            if (schemaUrl == null) {
                throw new XmlMappingException("Schema file not found: " + schemaLocation);
            }

            return schemaFactory.newSchema(schemaUrl);

        } catch (Exception e) {
            throw new XmlMappingException("Failed to load schema: " + schemaLocation, e);
        }
    }
}
