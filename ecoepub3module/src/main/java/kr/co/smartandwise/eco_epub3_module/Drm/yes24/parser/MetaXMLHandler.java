package kr.co.smartandwise.eco_epub3_module.Drm.yes24.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MetaXMLHandler extends DefaultHandler {

	private String opfPath = null;
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if(localName.equals("rootfile")){
			opfPath = attributes.getValue("full-path");
		}
	}

	public String getOpfPath(){
		return opfPath;
	}
}
