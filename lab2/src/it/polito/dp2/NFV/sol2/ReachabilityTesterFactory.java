package it.polito.dp2.NFV.sol2;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;

import it.polito.dp2.NFV.FactoryConfigurationError;
import it.polito.dp2.NFV.NfvReader;
import it.polito.dp2.NFV.NfvReaderException;
import it.polito.dp2.NFV.NfvReaderFactory;
import it.polito.dp2.NFV.sol2.ReachabilityTester;
import it.polito.dp2.NFV.lab2.ReachabilityTesterException;

public class ReachabilityTesterFactory extends it.polito.dp2.NFV.lab2.ReachabilityTesterFactory{

	//the actual base URL used by the client class has to be read as value of the 
	//"it.polito.dp2.NFV.lab2.URL" system property
	private final static String sys_prop_uri = "it.polito.dp2.NFV.lab2.URL"; //TO ADD
	
	
	
	private WebTarget target;
	private NfvReader nfv;
	
	@Override
	public ReachabilityTester newReachabilityTester() throws ReachabilityTesterException {
		// this method has to create an instance of the concrete class that implements the
				//ReachabilityTester interface
			
				try{
					//create the client obj
					Client client = ClientBuilder.newClient();
					//create a web targte for the intended uri
					this.target = client.target(getBaseURI());
					
					//create a NfvReader 
					this.nfv= NfvReaderFactory.newInstance().newNfvReader();
				}catch(NfvReaderException nre){
					System.err.println("unable to instantiate a new NfvReader");
					throw new ReachabilityTesterException(nre.getMessage());
				}catch(NullPointerException npe){
					System.err.println("unable to instantiate a new NfvReader or a new WebTarget");
					throw new ReachabilityTesterException(npe.getMessage());
				}catch(FactoryConfigurationError fce){
					System.err.println("unable to instantiate a new NfvReader or a new WebTarget");
					throw new ReachabilityTesterException(fce.getMessage());
				}
				return new ReachabilityTester(target, nfv);
	}
	private static URI getBaseURI(){
		URI uri;
		try{
			String uri_name = System.getProperty(sys_prop_uri);
			uri = UriBuilder.fromUri(uri_name).build();
		}catch(SecurityException se){//because we are reading a system property, we can get some exception
			System.err.println("unable to access the system property to retrieve the uri");
			return null;
		}catch(NullPointerException npe){
			System.err.println("unable to retrieve the uri from the system property");
			return null;
		}catch(IllegalArgumentException iae){//because we are reading a system property, we can get some exception
			System.err.println("unable to retrieve the uri from the system property");
			return null;
		}
		System.out.println("BASE URL: "+uri); //TO DELETE
		return uri;
	}
}
