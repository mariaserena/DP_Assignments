package it.polito.dp2.NFV.sol3.client2;
import it.polito.dp2.NFV.HostReader;
import it.polito.dp2.NFV.NodeReader;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HostReaderClass implements HostReader{
	private HostType host;
	private NffgsType nffgs;
	private VnftsType vnfts;
	private HostsType hosts;

	
	public HostReaderClass(HostType host, NffgsType nffgs, VnftsType vnfts, HostsType hosts){
		this.host=host;
		this.nffgs=nffgs;
		this.vnfts = vnfts;
		this.hosts=hosts;
	}

	@Override
	public String getName() {
		if(host==null){
			return null;
		}
		return host.getName();
	}

	@Override
	public int getAvailableMemory() {
		return host.getMemory();
	}

	@Override
	public int getAvailableStorage() {
		return host.getDiskStorage();
	}

	@Override
	public int getMaxVNFs() {
		return host.getMaxVnf().intValue();
	}

	@Override
	public Set<NodeReader> getNodes() {
		if(host==null){
			return null;
		}		
		//retrieve the list of node_refs
		List<String> node_refs = host.getNode();
		
		//if the node_refs list is empty, return null
		if(node_refs==null){
			return null;
		}
		//else, create a set of <NodeReader> and extract the nffgs
		Set<NodeReader> node_set = new HashSet<NodeReader>();
		List<NffgType> nffg_list = nffgs.getNffgType();
		if(nffg_list==null){
			return null;
		}
		
		//scan the node_refs list and search for the node by name
		for(String node_name : node_refs){
			//for all the nffgs 
			for(NffgType nffg: nffg_list){
				//retrieve the list of nodes in the nffg
				for(NodeType node : nffg.getNodeType()){
					if (node.getName().equals(node_name))
						node_set.add(new NodeReaderClass(node, vnfts, hosts,nffgs));
				}
			}
		}
		return node_set;
	}

}
