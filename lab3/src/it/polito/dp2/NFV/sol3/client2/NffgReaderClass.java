package it.polito.dp2.NFV.sol3.client2;

import it.polito.dp2.NFV.NffgReader;
import it.polito.dp2.NFV.NodeReader;


import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class NffgReaderClass implements NffgReader {

	private NffgType nffg;
	private NffgsType nffgs;
	private VnftsType vnfts;
	private HostsType hosts;
	
	public NffgReaderClass(NffgType nffg, NffgsType nffgs, VnftsType vnfts, HostsType hosts){
		this.nffg=nffg;
		this.nffgs = nffgs;
		this.hosts = hosts;
		this.vnfts = vnfts;
	}

	@Override
	public String getName() {
		if(nffg==null)
			return null;
		return nffg.getName();
	}

	@Override
	public Calendar getDeployTime() {
		// Gives the date and time of the time when the NF-FG was deployed in the system (with precision no worse than 1 second).
		Calendar dep_time = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
		dep_time.clear();
		dep_time = nffg.getDeployTime().toGregorianCalendar();
		return dep_time;
	}

	
	@Override
	public NodeReader getNode(String name) {
		// Gives access to a single network node given its name (a node is uniquely identified in the DP2-NFV system by its name)
		if(nffg==null||  name==null)
			return null;
		
		for(NodeType node:nffg.getNodeType()){
			if(node!=null){
				if(node.getName().equals(name)){
					return new NodeReaderClass(node, vnfts, hosts, nffgs);
				}
			}
		}		
		return null;
	}

	@Override
	public Set<NodeReader> getNodes() {
		//Gives access to the set of network nodes of this NF-FG.
		if(nffg==null)
			return null;
		List<NodeType>node_list = nffg.getNodeType();
		if(node_list==null)
			return null;
		Set<NodeReader> node_set = new HashSet<NodeReader>();
		for(NodeType node:node_list){
			if(node!=null)
				node_set.add(new NodeReaderClass(node, vnfts, hosts, nffgs));
		}
		return node_set;
	}

}
