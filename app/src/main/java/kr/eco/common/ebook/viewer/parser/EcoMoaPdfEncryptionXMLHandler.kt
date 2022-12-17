package kr.eco.common.ebook.viewer.parser

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

class EcoMoaPdfEncryptionXMLHandler : DefaultHandler() {

    private var pdfFileNames : ArrayList<String>? = null
    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?
    ) {
      if (localName.equals("encryption")){
          pdfFileNames = ArrayList<String>()
      } else if (localName.equals("CipherReference")){
          if (attributes?.getValue("URI")?.contains(".pdf")!!){
              pdfFileNames?.add(attributes.getValue("URI"))
          }
      }
    }

    fun getImagesPath() : ArrayList<String>? {
        return pdfFileNames
    }
}