package com.vectorcalculator;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

class SaveLoad {

    public static void main(String[] args) {
        Properties p = new Properties();
        convertToXML(p);
    }

    public static void convertToXML(Properties p) {
        try {
            JAXBContext jxbc = JAXBContext.newInstance(Properties.class);
            Marshaller m = jxbc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter stringWriter = new StringWriter();
            m.marshal(p, stringWriter);
            String xmlContent = stringWriter.toString();
            System.out.println(xmlContent);
        }
        catch (JAXBException ex) {
            Debug.println("XML Failed");
        }
    }

    public void save() {
        try {
            JAXBContext jxbc = JAXBContext.newInstance(Properties.class);
            Marshaller m = jxbc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            File f = new File("properties.xml");
            m.marshal(Properties.p, f);
        }
        catch (JAXBException ex) {
            Debug.println("XML Failed");
        }
    }

    public static void load(Properties p) {
        try {
            JAXBContext jxbc = JAXBContext.newInstance(Properties.class);
            Marshaller m = jxbc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            File f = new File("properties.xml");
            m.marshal(p, f);
        }
        catch (JAXBException ex) {
            Debug.println("XML Failed");
        }
    }

/*     public static void save() {
        Document d;
        Element e = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            d = db.newDocument();
            Element root = d.createElement("vector-calculator-state");
            Element s0 = d.createElement("initial-coords");
            s0.setAttribute("x", String.valueOf(VectorCalculator.x0));
            s0.setAttribute("y", String.valueOf(VectorCalculator.y0));
            s0.setAttribute("z", String.valueOf(VectorCalculator.z0));
            root.appendChild(s0);
            Element s1 = d.createElement("target-coords");
            s0.setAttribute("x", String.valueOf(VectorCalculator.x1));
            s0.setAttribute("y", String.valueOf(VectorCalculator.y1));
            s0.setAttribute("z", String.valueOf(VectorCalculator.z1));
            root.appendChild(s1);
            // e = d.createElement("x");
            // e.appendChild(d.createTextNode(String.valueOf(VectorCalculator.x0)));
            // s0.appendChild(d);

            System.out.println("Testing XML Output");
            printDocument(d, System.out);
        }
        catch (Exception ex) {
        }
    }

    public static void load() {
        
    }

    public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer transformer = tf.newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

    transformer.transform(new DOMSource(doc), 
    new StreamResult(new OutputStreamWriter(out, "UTF-8")));
    } */
}