/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.testgrid.reporting.reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.testgrid.common.util.StringUtil;
import org.wso2.carbon.testgrid.reporting.ReportingException;
import org.wso2.carbon.testgrid.reporting.result.TestResultable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

/**
 * This class is responsible for reading XML files and producing an output based on a given model.
 *
 * @since 1.0.0
 */
public class XMLResultReader implements ResultReadable {

    public static final String XML_START_ELEMENT_NAME = "XML_START_ELEMENT_NAME";
    private static final Log log = LogFactory.getLog(XMLResultReader.class);
    private final Map<String, Object> args;

    /**
     * Constructs an instance of {@link XMLResultReader}
     *
     * @param args arguments required for reading an XML result file
     */
    public XMLResultReader(Map<String, Object> args) {
        this.args = args;
    }

    @Override
    public <T extends TestResultable> List<T> readFile(Path path, Class<T> type)
            throws ReportingException {

        if (path == null) {
            throw new ReportingException("File path is null.");
        }

        if (type == null) {
            throw new ReportingException("Type cannot be null.");
        }

        if (!args.containsKey(XML_START_ELEMENT_NAME)) {
            throw new ReportingException("XML element to start reading results is not passed as an argument.");
        }

        String xmlStartElementName = String.class.cast(args.get(XML_START_ELEMENT_NAME));
        if (StringUtil.isStringNullOrEmpty(xmlStartElementName)) {
            throw new ReportingException("XML element to start reading results is null or empty.");
        }

        String filePath = path.toAbsolutePath().toString();
        List<T> results = new ArrayList<>();
        XMLStreamReader xmlStreamReader = createXMLStreamReader(filePath);

        try {
            while (xmlStreamReader.hasNext()) {
                if (xmlStreamReader.isStartElement() && xmlStreamReader.getLocalName().equals(xmlStartElementName)) {
                    JAXBContext jaxbContext = createJAXBContext(type);
                    Unmarshaller jaxbUnmarshaller = createJAXBUnmarshaller(jaxbContext);
                    T result = unmarshalResult(jaxbUnmarshaller, xmlStreamReader, type);
                    results.add(result);
                }
                xmlStreamReader.next();
            }
        } catch (XMLStreamException e) {
            throw new ReportingException("Error occurred when reading XML stream", e);
        }

        closeXMLStreamReader(xmlStreamReader);
        return results;
    }

    /**
     * Closes the given {@link XMLStreamReader} instance.
     *
     * @param xmlStreamReader {@link XMLStreamReader} instance to close
     * @throws ReportingException thrown when error occurs in closing the {@link XMLStreamReader} instance
     */
    private void closeXMLStreamReader(XMLStreamReader xmlStreamReader) throws ReportingException {
        try {
            xmlStreamReader.close();
        } catch (XMLStreamException e) {
            throw new ReportingException("Error occurred when closing XML stream", e);
        }
    }

    /**
     * Returns the result from the XML segment.
     *
     * @param unmarshaller    {@link Unmarshaller} instance to unmarshal XML segment
     * @param xmlStreamReader {@link XMLStreamReader} instance to provide XML segment
     * @param type            return type of the expected object
     * @param <T>             return type of the expected object
     * @return returns the object for the XML segment
     * @throws ReportingException thrown when error of unmarshalling the XML segment
     */
    private <T> T unmarshalResult(Unmarshaller unmarshaller, XMLStreamReader xmlStreamReader, Class<T> type)
            throws ReportingException {
        try {
            JAXBElement<T> jaxbElement = unmarshaller.unmarshal(xmlStreamReader, type);
            return jaxbElement.getValue();
        } catch (JAXBException e) {
            throw new ReportingException("Error in unmarshalling XML file", e);
        }
    }

    /**
     * Creates and returns an {@link XMLStreamReader} for the given file path.
     *
     * @param filePath file location of the XML file
     * @return {@link XMLStreamReader} for the given path
     * @throws ReportingException thrown when error on creating an {@link XMLStreamReader} instance
     */
    private XMLStreamReader createXMLStreamReader(String filePath) throws ReportingException {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        StreamSource streamSource = new StreamSource(filePath);
        try {
            return xmlInputFactory.createXMLStreamReader(streamSource);
        } catch (XMLStreamException e) {
            throw new ReportingException("Error occurred when reading XML stream", e);
        }
    }

    /**
     * Creates and returns a {@link JAXBContext} for the given type.
     *
     * @param type type to create the {@link JAXBContext}
     * @param <T>  type to create the {@link JAXBContext}
     * @return {@link JAXBContext} for the given type
     * @throws ReportingException thrown when error on creating an instance of {@link JAXBContext}
     */
    private <T> JAXBContext createJAXBContext(Class<T> type) throws ReportingException {
        try {
            return JAXBContext.newInstance(type);
        } catch (JAXBException e) {
            throw new ReportingException(String
                    .format(Locale.ENGLISH, "Error occurred when creating a JAXB context for type %s", type), e);
        }
    }

    /**
     * Creates and returns an instance of {@link Unmarshaller} for the given {@link JAXBContext}.
     *
     * @param jaxbContext {@link JAXBContext} for creating the {@link Unmarshaller}
     * @return {@link Unmarshaller} instance for the given {@link JAXBContext} instance
     * @throws ReportingException thrown when error on creating an instance of {@link Unmarshaller}
     */
    private Unmarshaller createJAXBUnmarshaller(JAXBContext jaxbContext) throws ReportingException {
        try {
            return jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            throw new ReportingException("Error occurred when creating a JAXB unmarshaller", e);
        }
    }
}
