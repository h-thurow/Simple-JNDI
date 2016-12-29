/*
 * Copyright (c) 2003, Henri Yandell, Eric Alexander
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or 
 * without modification, are permitted provided that the 
 * following conditions are met:
 * 
 * + Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * 
 * + Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 * 
 * + Neither the name of GenJava nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.osjava.sj.loader.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Loads properties using the DOM API from an InputStream containing XML
 */
public class XmlProperties extends AbstractProperties {

    public XmlProperties() {
        super();
    }

    public XmlProperties(Properties props) {
        super(props);
    }

    public void load(InputStream in) throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        Document document = null;
        
        try {
            builder = factory.newDocumentBuilder();
            document = builder.parse(in);            
        } catch (ParserConfigurationException pce) {
            throw new IOException("Unable to get DocumentBuilder from factory. " + pce.getMessage());
        } 
        catch (SAXException se) {
            throw new IOException("Unable to parse document. " + se.getMessage());
        }
        
        if (document != null) {
            loadDocument(document);
        }
    }

    private void loadDocument(Document document) {
        Element root = document.getDocumentElement();
        String level = root.getNodeName();
        processChildren(level, root);
    }
    
    private void processChildren(String level, Node node) {
        NodeList children = node.getChildNodes();
        for(int i=0;i<children.getLength();i++) {
            Node child = children.item(i);
            addNode(level, child);
            if (child.hasAttributes()) {
                String attributeLevel = level + getDelimiter() + child.getNodeName();
                addAttributes(attributeLevel, child.getAttributes());
            }
        }
    }
    
    private void addNode(String level, Node node) {            
        switch (node.getNodeType()) {
            case Node.ELEMENT_NODE :
                level = level + getDelimiter() + node.getNodeName();
                break;
            case Node.TEXT_NODE :
                store(level, node.getNodeValue());
                break;
        }
                     
        processChildren(level, node);        
    }
        
    private void addAttributes(String level, NamedNodeMap map) {       
        for(int i=0;i<map.getLength();i++) {
            Node attribute = map.item(i);
            String attributeLevel = level + getDelimiter() + attribute.getNodeName();
            store(attributeLevel, attribute.getNodeValue());
        }          
    }
    
    private void store(String name, String value) {
        if (value.trim().length() > 0) {
            setProperty(name, value);
        }
    }
}