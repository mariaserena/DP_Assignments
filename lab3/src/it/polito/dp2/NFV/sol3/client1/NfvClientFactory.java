package it.polito.dp2.NFV.sol3.client1;

import java.net.URI;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;

import it.polito.dp2.NFV.FactoryConfigurationError;
import it.polito.dp2.NFV.lab3.NfvClient;
import it.polito.dp2.NFV.lab3.NfvClientException;

public class NfvClientFactory extends it.polito.dp2.NFV.lab3.NfvClientFactory{
	private final static String nfvUriSysProp = "it.polito.dp2.NFV.lab3.URL";
	private final static String defaultUri = "http://localhost:8080/NfvDeployer/rest";
	private WebTarget target;
	private MyConverter converter;
	@Override
	public NfvClient newNfvClient() throws NfvClientException {
		try {
			target = ClientBuilder.newClient().target(getBaseURI());
			converter = new MyConverter();
			return new it.polito.dp2.NFV.sol3.client1.NfvClient(target, converter);
		} catch (FactoryConfigurationError e) {
			throw new NfvClientException("Could not instantiate NfvClient");
		} catch(Exception e){
			throw new NfvClientException("Could not instantiate Web Target");
		}
	}
	
	private static URI getBaseURI() {
		String uri=null;
		URI Uri;
		uri = System.getProperty(nfvUriSysProp);
		if(uri == null){
			uri = defaultUri;
		}
		Uri = UriBuilder.fromUri(uri).build();
		return Uri;
	}
}