
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

public class MoviesAndGenresParser {

    public HashSet<Movie> myMovies;
    Document dom;

    public MoviesAndGenresParser() {
        //create a list to hold the employee objects
        myMovies = new HashSet<Movie>();
    }

    public void runExample() {

        //parse the xml file and get the dom object

        parseXmlFile();

        //get each employee element and create a Employee object
        parseDocument();

        //Iterate through the list and print the data
        printData();

    }

    private void parseXmlFile() {
        //get the factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            //Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            //parse using builder to get DOM representation of the XML file
            dom = db.parse("mains243.xml");

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void parseDocument() {
        //get the root elememt
        Element docEle = dom.getDocumentElement();

        //get a nodelist of <directorfilms> elements
        NodeList nl = docEle.getElementsByTagName("directorfilms");
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                Element el = (Element) nl.item(i);
                String director_name = getTextValue(el, "dirname");
 
                NodeList films_nl = el.getElementsByTagName("film"); // <film> elements within <directorfilms>
                if( films_nl != null && films_nl.getLength() > 0) {
                	for(int j=0; j < films_nl.getLength(); ++j) {
                		Element films_e = (Element) films_nl.item(j); 
                		Movie m = getMovie(films_e, director_name); // create Movie object
                		NodeList cats_nl = films_e.getElementsByTagName("cat"); // <cat> elements within <film> 
                		if( cats_nl != null && cats_nl.getLength() > 0) {
                			for( int k=0; k < cats_nl.getLength(); ++k) {
                				Element cat_e = (Element) cats_nl.item(k);
                				if( cat_e.getFirstChild() != null) {
                					String cat = cat_e.getFirstChild().getNodeValue();
                					if(m.getGenres().contains(cat)) {
                						System.out.println(m.getTitle() + " already contains " + cat + " genre. Did not add this genre");
                					}
                					else {
                						m.insertGenre(cat);
                					}
                				}
                			}
                		}
                		
                		if( m.getYear() == 9999) {
                			System.out.println(m.getTitle() + " has invalid year. No changes made to database.");
                		}
                		else if ( m.getId().equals("EMPTY")) {
                			System.out.println("Movie id was empty. No changes made to database.");
                		}
                		else if ( m.getTitle().equals("EMPTY")) {
                			System.out.println("Movie title was empty. No changes made to database.");
                		}
                		else if ( m.getDirector().equals("EMPTY")) {
                			System.out.println("Movie director was empty. No changes made to database.");
                		}
                		else {
                			myMovies.add(m);
                		}
                	}
                }
            }
        }
    }

    
    private Movie getMovie(Element films_e, String director) {
    	String fid = getTextValue(films_e, "fid");
		String title = getTextValue(films_e, "t");
		String str_year = getTextValue(films_e, "year");
		
		int year = 9999;
		if( isNumeric(str_year)) {
			year = Integer.parseInt(str_year);
		}
		else {
			System.out.println("Inconsistent data. Element: <year>    Value: " + str_year);
		}
		
		if(director==null) {
			director = getTextValue(films_e, "dirn");
		}
		
		if(fid == null || fid.equals("")) {
			fid = "EMPTY";
			System.out.println("Empty data. Element: <fid>");
		}
		
		if(title == null || title.equals("")) {
			title = "EMPTY";
			System.out.println("Empty data. Element: <t>");
		}
		
		if(director == null || director.equals("")) {
			director = "EMPTY";
			System.out.println("Empty data. Element: <dirname>");
		}
		
		Movie m = new Movie(fid, title, director, year);
		return m;
    }
    
    /**
     * I take a xml element and the tag name, look for the tag and get
     * the text content
     * i.e for <employee><name>John</name></employee> xml snippet if
     * the Element points to employee node and tagName is name I will return John
     * 
     * @param ele
     * @param tagName
     * @return
     */
    private String getTextValue(Element ele, String tagName) {
        String textVal = null;
        NodeList nl = ele.getElementsByTagName(tagName);
        if (nl != null && nl.getLength() > 0) {
            Element el = (Element) nl.item(0);
            if( el.getFirstChild() != null) {
            	textVal = el.getFirstChild().getNodeValue();
            }
        }

        return textVal;
    }

    /**
     * Calls getTextValue and returns a int value
     * 
     * @param ele
     * @param tagName
     * @return
     */
    private int getIntValue(Element ele, String tagName) {
        //in production application you would catch the exception
        return Integer.parseInt(getTextValue(ele, tagName));
    }

    private boolean isNumeric(String s) {
    	try {
    		Integer.parseInt(s);
    		return true;
    	}
    	catch(Exception e){
    		return false;
    	}
    }
    
    /**
     * Iterate through the list and print the
     * content to console
     */
    private void printData() {
        Iterator<Movie> it = myMovies.iterator();
        while (it.hasNext()) {
            System.out.println(it.next().toString());
        }
        
        System.out.println("No of Movies '" + myMovies.size() + "'.");

    }

    public static void main(String[] args) {
        //create an instance
        MoviesAndGenresParser dpe = new MoviesAndGenresParser();

        //call run example
        dpe.runExample();
    }

}
