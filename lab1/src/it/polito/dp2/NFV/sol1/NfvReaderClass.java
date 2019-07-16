package it.polito.dp2.NFV.sol1;
import it.polito.dp2.NFV.sol1.jaxb.Dp2NfvType;
import it.polito.dp2.NFV.sol1.jaxb.HostType;
import it.polito.dp2.NFV.sol1.jaxb.InType;
import it.polito.dp2.NFV.sol1.jaxb.NfFgType;
import it.polito.dp2.NFV.sol1.jaxb.VNFTType;
import it.polito.dp2.NFV.sol1.jaxb.ConnectionType;
import it.polito.dp2.NFV.NfvReader;
import it.polito.dp2.NFV.ConnectionPerformanceReader;
import it.polito.dp2.NFV.HostReader;
import it.polito.dp2.NFV.NffgReader;
import it.polito.dp2.NFV.VNFTypeReader;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;



public class NfvReaderClass implements NfvReader{
	private Dp2NfvType dp2_nfv;
	
	//constructor
	public NfvReaderClass(Dp2NfvType dp2_nfv){
		this.dp2_nfv=dp2_nfv;
	}

	@Override
	public ConnectionPerformanceReader getConnectionPerformance(HostReader host1, HostReader host2) {
		//host1 - the start host of the link for which the object has to be returned
		//host2 - the end host of the link for which the object has to be returned
		//Returns:the ConnectionPerformanceReader object for the link from host1 to host2, 
		//or null if no link exists between host1 and host2
		if(host1==null || host2==null ||dp2_nfv==null)
			return null;
		//from the dp2_nfv obj extract the in obj
		InType in = dp2_nfv.getIn();
		if(in != null){
			//extract the connections
			List<ConnectionType> conn_type = in.getConnection();
			if(conn_type!=null){
				for(ConnectionType conn:conn_type){
					if(conn!=null){
						if(conn.getHost1().equals(host1.getName()) &&conn.getHost2().equals(host2.getName())){
							return new ConnectionPerformanceReaderClass(conn);
						}
					}
				}
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
		Set<HostReader> host_set = this.getHosts();
		for (HostReader host : host_set){
			if(host.getName().equals(name))
				return host;
		}
		return null;
	}

	@Override
	public Set<HostReader> getHosts() {
		// Gives access to the set of known hosts, available in the IN.
		List<HostType> host_list = dp2_nfv.getIn().getHost(); 
		if(host_list==null)
			return null;
		Set<HostReader> host_set = new HashSet<HostReader>();
		for (HostType host : host_list){
			if(host!=null)
				host_set.add(new HostReaderClass(host, dp2_nfv));
		}
		if(host_set.isEmpty())
			return null;
		return host_set;
	}

	@Override
	public NffgReader getNffg(String name) {
		List<NfFgType> nffg_list = dp2_nfv.getNfFg();
		if(nffg_list==null)
			return null;
		for(NfFgType nffg:nffg_list){
			if(nffg.getName().equals(name)){
				return new NffgReaderClass(dp2_nfv, nffg);
			}
		}
		return null;
	}

	@Override
	public Set<NffgReader> getNffgs(Calendar since) {
		// Gives access to the set of known NF-FGs deployed since a given date.
		List<NfFgType> nffg_list = dp2_nfv.getNfFg();
		if(nffg_list==null)
			return null;
		Set<NffgReader> nffg_set = new HashSet<NffgReader>();
		if(since==null){ //return all the nffgs
			for(NfFgType nffg:nffg_list){
				if(nffg!=null)
					nffg_set.add(new NffgReaderClass(dp2_nfv, nffg));
			}
		}
		else {
			//Construct a Calendar based on the current time in the given time zone with the given locale.
			//getDefault() creates a TimeZone/Locale based on the time zone/locale where the program is running
			Calendar nffg_date = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
			for(NfFgType nffg:nffg_list){
				if(nffg!=null){
					nffg_date.clear();
					nffg_date = nffg.getDeployTime().toGregorianCalendar();
					if(since.after(nffg_date)){
						nffg_set.add(new NffgReaderClass(dp2_nfv, nffg));
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
		List<VNFTType> VNFTlist =  	dp2_nfv.getCatalog().getVNFT();
		Set<VNFTypeReader> VNFTset = new HashSet<VNFTypeReader>();
		
		if(VNFTlist==null)
			return null;
		for(VNFTType vnft:VNFTlist){
			VNFTset.add(new VNFTypeReaderClass(vnft));
		}
		if(VNFTset.isEmpty())
			return null;
		return VNFTset;
	}
	
}
