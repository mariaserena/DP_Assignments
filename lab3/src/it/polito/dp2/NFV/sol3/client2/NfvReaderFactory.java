package it.polito.dp2.NFV.sol3.client2;
import java.io.File;
import java.net.URI;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;
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


public class NfvReaderFactory extends it.polito.dp2.NFV.NfvReaderFactory{
	
	private final static String nffgUriSysProp = "it.polito.dp2.NFV.lab3.URL";
	private final static String defaultNfvUri = "http://localhost:7475/NfvDeployer/rest";
	private WebTarget target;
	@Override
	public NfvReader newNfvReader() throws NfvReaderException {
		try{
			target = ClientBuilder.newClient().target(getBaseURI());
			return new NfvReaderClass(target);
		} catch (Exception e){
			throw new NfvReaderException("Could not instantiate NfvReader.");
		}
	}
	
	// get URI of service from system properties
	private static URI getBaseURI() {
		String uri = null;
		URI Uri;
		uri = System.getProperty(nffgUriSysProp);
		if(uri == null){
			uri = defaultNfvUri;
		}
		Uri = UriBuilder.fromUri(uri).build();		
		return Uri;
  }
}
