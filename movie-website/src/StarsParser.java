import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class StarsParser {
	HashSet<Star> myStars;
	Document dom;
	final boolean debug = true;
	
	public StarsParser() {
		// create a list to hold the employee objects
		myStars = new HashSet<>();
	}

	public void runExample() {

		// parse the xml file and get the dom object
		parseXmlFile();

		// get each employee element and create a Employee object
		parseDocument();

		// Iterate through the list and print the data
		if(debug) {
			printData();
		}

	}

	private void parseXmlFile() {
		// get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {

			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			// parse using builder to get DOM representation of the XML file
			dom = db.parse("actors63.xml");

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private void parseDocument() {
		// get the root elememt
		Element docEle = dom.getDocumentElement();
		
		// get a nodelist of <employee> elements
		NodeList nl = docEle.getElementsByTagName("actor");
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {

				Element el = (Element) nl.item(i);
				
				String stageName = getTextValue(el, "stagename");
				String dob = getTextValue(el, "dob");
				try {
					//If integer is not a valid Integer insert null as dob instead
					Integer.parseInt(dob);
				}catch(Exception e) {
					if(debug) {
						System.out.println("attempted to insert "+dob+ " as year. failed at node: " + el.getNodeName());
					}
					dob = null;
				}
				myStars.add(new Star(stageName, dob));
				
			}

		}
	}


	/**
	 * I take a xml element and the tag name, look for the tag and get the text
	 * content i.e for <employee><name>John</name></employee> xml snippet if the
	 * Element points to employee node and tagName is name I will return John
	 * 
	 * @param ele
	 * @param tagName
	 * @return
	 */
	private String getTextValue(Element ele, String tagName) {
		String textVal = "";
		NodeList nl = ele.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0 && nl.item(0).getTextContent() != "") {
			Element el = (Element) nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}
		return textVal;
	}

	/**
	 * Iterate through the list and print the content to console
	 */
	private void printData() {

		System.out.println("No of stars '" + myStars.size() + "'.");

		Iterator<Star> it = myStars.iterator();
		while (it.hasNext()) {
			System.out.println(it.next().toString());
		}
	}

	public static void main(String[] args) {
		StarsParser dpe = new StarsParser();
		dpe.runExample();
	}
}
