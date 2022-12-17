package kr.co.smartandwise.eco_epub3_module.Drm.yes24.parser;

import java.util.ArrayList;
import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class OpfXMLHandler extends DefaultHandler {

	private HashMap<String, OpfData> parseOpfdata = null; 
	private ArrayList<String> pageOrder = null;
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		if(localName.equals("metadata")){
			parseOpfdata = new HashMap<String, OpfData>();
			
		}else if(localName.equals("item")){
			
			OpfData opfData = new OpfData();
			opfData.setId(attributes.getValue("id"));
			opfData.setHref(attributes.getValue("href"));
			opfData.setMediaType(attributes.getValue("media-type"));
			
			if(parseOpfdata != null){
				parseOpfdata.put(attributes.getValue("id"), opfData);
			}
		}else if(localName.equals("spine")){
			pageOrder = new ArrayList<String>();
			
			OpfData opfData = new OpfData();
			opfData.setId("spine");
			opfData.setHref(attributes.getValue("toc"));
			opfData.setMediaType("");
			
			if(parseOpfdata != null){
				parseOpfdata.put("spine", opfData);
			}
			
		}else if(localName.equals("itemref")){
			if(pageOrder != null){
				pageOrder.add(attributes.getValue("idref"));
			}
		}
	}

	public HashMap<String, OpfData> getOpfData(){
		return this.parseOpfdata;
	}
	
	public ArrayList<String> getPageOrder(){
		return this.pageOrder;
	}
}
