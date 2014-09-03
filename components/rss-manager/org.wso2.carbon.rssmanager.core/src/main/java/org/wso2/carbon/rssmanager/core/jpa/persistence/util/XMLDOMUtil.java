package org.wso2.carbon.rssmanager.core.jpa.persistence.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XMLDOMUtil {
	
	private static final ThreadLocal<XMLDOMUtil> xmlUtil = new ThreadLocal<XMLDOMUtil>(){
		@Override protected XMLDOMUtil initialValue() {
            return new XMLDOMUtil();
		}
	};
	
	public static synchronized XMLDOMUtil getInstance(){
		return xmlUtil.get();
	}
	
	private DocumentBuilder domBuilder = null;
	
	private static final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	
	public DocumentBuilder getDOMBuilder() throws ParserConfigurationException{
		if(domBuilder == null){
			domBuilder = dbFactory.newDocumentBuilder();
		}

		return domBuilder;
	}
	
	public Document getDocument(final String fileName) throws SAXException, IOException, ParserConfigurationException{
		Document doc = getDOMBuilder().parse(new File(fileName));
		//optional, but recommended
		//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
		//doc.getDocumentElement().normalize();
		return doc;
	}
	
	public static String getElementText(final Element eElement, final String name){
        NodeList nodes = eElement.getElementsByTagName(name);
        if (nodes == null) {
            return null;
        }
        Node node = nodes.item(0);
        if (node == null) {
            return null;
        }
        String value = node.getTextContent();
        return (value != null) ? value.trim() : null;
	}
	
	public static Node getElement(final Element eElement, final String name){
		return eElement.getElementsByTagName(name).item(0);
	}
	
	
	public static List<String> getElementTextList(final Element eElement, final String name){
		 NodeList nodeList = eElement.getElementsByTagName(name);
		 if(nodeList != null && nodeList.getLength() > 0){
			 int length = nodeList.getLength();
			 List<String> nodeItems = new ArrayList<String>();
			 for(int index =0 ; index < length ; ++index){
				 nodeItems.add(nodeList.item(index).getTextContent().trim());				 
			 }
			 return nodeItems;
		 }
		 
		 
		 return Collections.EMPTY_LIST;
	}
	
	public static List<Node> getElementList(final Element eElement, final String name){
		 NodeList nodeList = eElement.getElementsByTagName(name);
		 if(nodeList != null && nodeList.getLength() > 0){
			 int length = nodeList.getLength();
			 List<Node> nodeItems = new ArrayList<Node>();
			 for(int index =0 ; index < length ; ++index){
				 nodeItems.add(nodeList.item(index));				 
			 }
			 return nodeItems;
		 }
		 
		 
		 return Collections.EMPTY_LIST;
	}
	

}
