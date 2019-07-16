package it.polito.dp2.NFV.sol3.client2;
import it.polito.dp2.NFV.NfvReader;
import it.polito.dp2.NFV.ConnectionPerformanceReader;
import it.polito.dp2.NFV.HostReader;
import it.polito.dp2.NFV.NffgReader;
import it.polito.dp2.NFV.VNFTypeReader;
import it.polito.dp2.NFV.sol3.service.Neo4jService;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;



public class NfvReaderClass implements NfvReader{
	private WebTarget target;
	private HostsType hosts;
	private NffgsType nffgs;
	private VnftsType vnfts;
	private PerformancesType perfs;
	
	private final static String nffgsPath = "/resources/nffgs";
	private final static String vnftsPath = "/resources/vnfts";
	private final static String perfsPath = "/resources/performances";
	private final static String hostsPath = "/resources/hosts";
	
	private static Logger logger = Logger.getLogger(NfvReaderClass.class.getName());

	//constructor
	public NfvReaderClass(WebTarget target){
		this.target = target;
		//retrieve the nffgs
		Response res;
		try{
			res = target.path(nffgsPath).request().get();
			if(res == null || res.getStatus() != 200){
				this.nffgs = null;
			}
			else{
				NffgsType nffgs = (NffgsType) res.readEntity(NffgsType.class);
				if(nffgs == null){
					logger.log(Level.SEVERE, "null nffgs retrieved");
				}
				this.nffgs = nffgs;
				if(nffgs.getNffgType()==null){
					logger.log(Level.SEVERE, "no nffg in nffgs retrieved");
				}
			}
		} catch(Exception e){
			this.nffgs = null;
		}
		//retrieve the connections
		try{
			res = target.path(perfsPath).request().get();
			if(res == null || res.getStatus() != 200){
				this.perfs = null;
			}
			PerformancesType perfs = (PerformancesType) res.readEntity(PerformancesType.class);
			this.perfs = perfs;
		} catch(Exception e){
			this.perfs = null;
		}
		//retrieve the vnfts
		try{
			res = target.path(vnftsPath).request().get();
			if(res == null || res.getStatus() != 200){
				this.vnfts = null;
			}
			VnftsType vnfts = (VnftsType) res.readEntity(VnftsType.class);
			this.vnfts = vnfts;		
		} catch(Exception e){
			this.vnfts = null;
		}
		//retrieve the hosts
		try{
			res = target.path(hostsPath).request().get();
			if(res == null || res.getStatus() != 200){
				logger.log(Level.SEVERE, "null hosts retrieved");
				this.hosts = null;
			}
			HostsType hosts = (HostsType) res.readEntity(HostsType.class);
			this.hosts = hosts;				
		} catch(Exception e){
			this.hosts= null;
		}
	}

	@Override
	public ConnectionPerformanceReader getConnectionPerformance(HostReader host1, HostReader host2) {
		//host1 - the start host of the link for which the object has to be returned
		//host2 - the end host of the link for which the object has to be returned
		//Returns:the ConnectionPerformanceReader object for the link from host1 to host2, 
		//or null if no link exists between host1 and host2
		if(host1==null || host2==null){
			return null;
		}
			
		//from the dp2_nfv obj extract the in obj
		for(PerformanceType p: perfs.getPerformanceType()){
			if(p.getHost1().equals(host1.getName()) && p.getHost2().equals(host2.getName())){
				ConnectionPerformanceReader to_ret =new ConnectionPerformanceReaderClass(p);
				return to_ret;
			}
		}
		return null;
	}

	@Override
	public HostReader getHost(String name) {
		// get an HostReader by name
		if(name==null){
			return null;
		}
		for(HostType h: hosts.getHostType()){
			if(h.getName().equals(name)){
				 HostReader to_ret	= new HostReaderClass(h, nffgs, vnfts, hosts);
				 return to_ret;
			}
		}
		return null;
	}

	@Override
	public Set<HostReader> getHosts() {
		// Gives access to the set of known hosts, available in the IN.
		Set<HostReader> host_readers = new HashSet<HostReader>();
		for(HostType h: hosts.getHostType()){
			HostReader host_r = new HostReaderClass(h, nffgs, vnfts, hosts);
			host_readers.add(host_r);
		}
		return host_readers;
	}

	@Override
	public NffgReader getNffg(String name) {
		for(NffgType nffg:this.nffgs.getNffgType()){
			if(nffg.getName().equals(name)){
				
				NffgReader to_ret = new NffgReaderClass(nffg, nffgs,vnfts, hosts);
				 return to_ret;
			}
		}
		return null;
	}

	@Override
	//Gives access to the set of known NF-FGs deployed since a given date.
	public Set<NffgReader> getNffgs(Calendar since) {
		// Gives access to the set of known NF-FGs deployed since a given date.
		List<NffgType> nffg_list = nffgs.getNffgType();
		if(nffg_list==null)
			return null;
		Set<NffgReader> nffg_set = new HashSet<NffgReader>();
		if(since==null){ //return all the nffgs
			for(NffgType nffg:nffg_list){
				if(nffg!=null){
					nffg_set.add(new NffgReaderClass(nffg, nffgs, vnfts, hosts));
					
				}
			}
		}
		else {
			//Construct a Calendar based on the current time in the given time zone with the given locale.
			//getDefault() creates a TimeZone/Locale based on the time zone/locale where the program is running
			Calendar nffg_date = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
			for(NffgType nffg:nffg_list){
				if(nffg!=null){
					nffg_date.clear();
					nffg_date = nffg.getDeployTime().toGregorianCalendar();
					if(since.after(nffg_date)){
						nffg_set.add(new NffgReaderClass(nffg, nffgs, vnfts,hosts));
					}
				}
			}
		}
		if(nffg_set.isEmpty())
			return null;
		return nffg_set;
	}

	@Override
	public Set<VNFTypeReader> getVNFCatalog() {
		// Gives access to the catalog of supported network function types
		
		Set<VNFTypeReader> VNFTset = new HashSet<VNFTypeReader>();
		for(VnftType vnft:vnfts.getVnftType()){
			VNFTset.add(new VNFTypeReaderClass(vnft));
		}
		if(VNFTset.isEmpty())
			return null;
		return VNFTset;
	}
	
}
