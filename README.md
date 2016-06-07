# JAXB XML Mapper

Type-safe XML marshaling and unmarshaling using JAXB with optional XSD schema validation.

## Features

- **Type-safe** conversion between XML and Java objects
- **Optional XSD validation** for strict schema enforcement
- **Formatted output** support for readable XML
- **Multiple input sources** - String, InputStream
- **Clean API** with builder-style configuration

## Installation

### Maven

```xml
<dependency>
    <groupId>com.github.larsderidder</groupId>
    <artifactId>jaxb-xml-mapper</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```gradle
implementation 'com.github.larsderidder:jaxb-xml-mapper:1.0.0'
```

## Usage

### Basic Unmarshaling

```java
// Create mapper for package containing JAXB classes
XmlMapper mapper = new XmlMapper("com.example.model");

// Convert XML to object
String xml = "<user><name>John</name><email>john@example.com</email></user>";
User user = mapper.fromXml(xml, User.class);
```

### Basic Marshaling

```java
User user = new User("John", "john@example.com");

// Convert object to XML
String xml = mapper.toXml(user);

// With formatting
String formattedXml = mapper.toXml(user, true);
```

### With XSD Validation

```java
// Create mapper with schema validation
XmlMapper mapper = new XmlMapper("com.example.model", "schema.xsd");

// Validate during unmarshaling
User user = mapper.fromXml(xml, User.class, true);

// Invalid XML will throw XmlMappingException
```

### Using Existing JAXBContext

```java
JAXBContext context = JAXBContext.newInstance(User.class, Order.class);
XmlMapper mapper = new XmlMapper(context);

User user = mapper.fromXml(xml, User.class);
```

### From InputStream

```java
InputStream stream = new FileInputStream("user.xml");
User user = mapper.fromXml(stream, User.class, false);
```

## Error Handling

All errors throw `XmlMappingException`:

```java
try {
    User user = mapper.fromXml(invalidXml, User.class);
} catch (XmlMappingException e) {
    System.err.println("XML parsing failed: " + e.getMessage());
}
```

## Requirements

- Java 8+ (JAXB included)
- JAXB-annotated classes

## Example JAXB Model

```java
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class User {
    private String name;
    private String email;

    // Constructors, getters, setters
}
```

## License

MIT License - see [LICENSE](LICENSE) file.
