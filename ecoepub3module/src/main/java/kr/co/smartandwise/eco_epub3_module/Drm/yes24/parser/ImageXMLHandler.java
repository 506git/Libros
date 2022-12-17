package kr.co.smartandwise.eco_epub3_module.Drm.yes24.parser;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ImageXMLHandler extends DefaultHandler {

	private ArrayList<String> imageFileNames = null;
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		if(localName.equals("manifest")){
			imageFileNames = new ArrayList<String>();
			
		}else if(localName.equals("item")){
//			if(attributes.getValue("media-type").contains("image")){
				imageFileNames.add(attributes.getValue("href"));
//			}
		}
	}

	public ArrayList<String> getImagesPath(){
		return imageFileNames;
	}
}
