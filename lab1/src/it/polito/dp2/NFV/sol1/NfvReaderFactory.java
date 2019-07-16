package it.polito.dp2.NFV.sol1;
import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import it.polito.dp2.NFV.NfvReader;
import it.polito.dp2.NFV.NfvReaderException;
import it.polito.dp2.NFV.sol1.jaxb.Dp2NfvType;

public class NfvReaderFactory extends it.polito.dp2.NFV.NfvReaderFactory{
	
	private final static String sys_prop_xml_file = "it.polito.dp2.NFV.sol1.NfvInfo.file";
	private final static String schema_file = "xsd/nfvInfo.xsd";
	private final static String jaxb_context = "it.polito.dp2.NFV.sol1.jaxb";
	@Override
	public NfvReader newNfvReader() throws NfvReaderException {
		try{
			//retrieve the XML file
			String xml_file = System.getProperty(sys_prop_xml_file);

			if (!xml_file.endsWith(".xml")) {
				throw new NfvReaderException("Error: wrong file extension. (xml required)");
			}
			
			//create a JAXBContext 
			JAXBContext jc = JAXBContext.newInstance(jaxb_context);
			
			//create an unmarshaller
			Unmarshaller u = jc.createUnmarshaller();
			try{
				SchemaFactory sf = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
				Schema schema = sf.newSchema(new File(schema_file));
				u.setSchema(schema);
			}catch(SAXException se){ //JAXPException related to the schema
				System.err.println("Unable to validate the schema");
				se.printStackTrace();
				throw new NfvReaderException(se);
			}catch(NullPointerException npe){
				System.err.println("Unable find the schema");
				throw new NfvReaderException(npe);
			}catch(UnsupportedOperationException uoe){
				System.err.println("Unmarshaller created from a JAXBContext referencing JAXB 1.0 mapped classes. JAXB 2.0 needed");
				throw new NfvReaderException(uoe);
			}
			
			@SuppressWarnings({ "rawtypes", "unchecked" })
			JAXBElement<Dp2NfvType> j = (JAXBElement)u.unmarshal(new File(xml_file));
			
			//additional checks (that are not possible inside the xml schema) are performed
			
			
			//let's now extract the Dp2NfvType element
			Dp2NfvType dp2_nfv = j.getValue();
			NfvReaderClass nfv_r = new NfvReaderClass(dp2_nfv);
			return nfv_r;
		}catch(UnmarshalException ue){
			System.err.println("error in unmarshalling the xml file");
			System.err.println(ue.getCause().getLocalizedMessage());
			throw new NfvReaderException(ue);	
		}catch(JAXBException je){
			System.err.println("JAXB error");
			System.err.println(je.getCause().getLocalizedMessage());
			throw new NfvReaderException(je);
		}catch(ClassCastException cce){
			System.err.println("error in xml schema root element cast");
			throw new NfvReaderException(cce);
		}catch(NullPointerException npe){
			System.err.println("unable to find the xml file");
			throw new NfvReaderException(npe);
		}catch(IllegalArgumentException iae){ //system properties exceptions
			System.err.println("passed illegal xml file");
			throw new NfvReaderException(iae);
		}catch(SecurityException se){
			System.err.println("unable to access system properties");
			throw new NfvReaderException(se);
		}
	}
}
