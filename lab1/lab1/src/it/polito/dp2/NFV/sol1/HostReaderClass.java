package it.polito.dp2.NFV.sol1;
import it.polito.dp2.NFV.sol1.jaxb.HostType;
import it.polito.dp2.NFV.sol1.jaxb.Dp2NfvType;
import it.polito.dp2.NFV.sol1.jaxb.NfFgType;
import it.polito.dp2.NFV.sol1.jaxb.NodeType;
import it.polito.dp2.NFV.HostReader;
import it.polito.dp2.NFV.NodeReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HostReaderClass implements HostReader{
	private HostType host;
	private Dp2NfvType dp2_nfv;
	
	public HostReaderClass(HostType host, Dp2NfvType dp2_nfv){
		this.host=host;
		this.dp2_nfv=dp2_nfv;
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
		if(host==null || dp2_nfv==null){
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
		List<NfFgType> nffg_list = dp2_nfv.getNfFg();
		if(nffg_list==null){
			return null;
		}
		
		//scan the node_refs list and search for the node by name
		for(String node_name : node_refs){
			//for all the nffgs 
			for(NfFgType nffg: nffg_list){
				List<NodeType> nodes = nffg.getNode();
				for(NodeType node : nodes){
					if(nodes!=null){
						if (node.getName().equals(node_name))
							node_set.add(new NodeReaderClass(node, dp2_nfv));
					}
				}
			}
		}
		return node_set;
	}

}
