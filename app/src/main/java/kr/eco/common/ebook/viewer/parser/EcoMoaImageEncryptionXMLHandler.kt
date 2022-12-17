package kr.eco.common.ebook.viewer.parser

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

class EcoMoaImageEncryptionXMLHandler : DefaultHandler(){
    private var imageFileNames : ArrayList<String>? = null
    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?
    ) {
        if (localName.equals("encryption")){
            imageFileNames = ArrayList<String>()
        } else if (localName.equals("CipherReference")){
            //if(attributes.getValue("URI").contains(".png") || attributes.getValue("URI").contains(".jpg") || attributes.getValue("URI").contains(".jpeg")  || attributes.getValue("URI").contains(".css")){
            imageFileNames!!.add(attributes!!.getValue("URI"))
        }
    }

    fun getImagesPath() : ArrayList<String>? {
        return imageFileNames
    }
}