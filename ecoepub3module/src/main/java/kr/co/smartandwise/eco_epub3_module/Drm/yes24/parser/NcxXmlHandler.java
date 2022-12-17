package kr.co.smartandwise.eco_epub3_module.Drm.yes24.parser;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class NcxXmlHandler extends DefaultHandler {

	private ArrayList<EbookIndexVO> parseEbookIndexdata = null; 

	private EbookIndexVO ebookIndexData = null;

	private boolean textTag 	= false;
	private boolean	navLabel 	= false;

	private String currentValue = null;


	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		if(localName.equals("navMap")){
			parseEbookIndexdata = new ArrayList<EbookIndexVO>();

		}else if(localName.equals("navPoint")){
				if(ebookIndexData != null){
					parseEbookIndexdata.add(ebookIndexData);
					ebookIndexData = null;
				}else{
					ebookIndexData = new EbookIndexVO();
					ebookIndexData.setId(attributes.getValue("id"));
					ebookIndexData.setPlayOrder(attributes.getValue("playOrder"));					
				}
		}else if(localName.equals("content")){
			if(ebookIndexData != null){
				ebookIndexData.setContentSrc(attributes.getValue("src"));	
			}
		}else if(localName.equals("navLabel")){
			this.navLabel = true;
		}else if(localName.equals("text")){
			this.textTag = true;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
	throws SAXException {
		// TODO Auto-generated method stub

		if (navLabel == true && textTag == true) {
			if(ebookIndexData != null){
				currentValue = new String(ch, start, length);
			}
			textTag = false;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
	throws SAXException {
		// TODO Auto-generated method stub

		if(localName.equals("navPoint")){
			if(ebookIndexData != null){
				parseEbookIndexdata.add(ebookIndexData);
				ebookIndexData = null;
			}
		}else if(localName.equals("text")){
			if(ebookIndexData != null){
				ebookIndexData.setText(currentValue);
				currentValue = null;
			}
		}else if(localName.equals("navLabel")){
			this.navLabel = false;
		}
	}


	public ArrayList<EbookIndexVO> getNcxList(){
		return parseEbookIndexdata;
	}
}
