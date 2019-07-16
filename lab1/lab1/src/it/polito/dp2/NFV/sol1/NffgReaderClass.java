package it.polito.dp2.NFV.sol1;
import it.polito.dp2.NFV.sol1.jaxb.Dp2NfvType;
import it.polito.dp2.NFV.sol1.jaxb.NfFgType;
import it.polito.dp2.NFV.sol1.jaxb.NodeType;
import it.polito.dp2.NFV.NffgReader;
import it.polito.dp2.NFV.NodeReader;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class NffgReaderClass implements NffgReader {
	private Dp2NfvType dp2_nfv;
	private NfFgType nffg;
	
	public NffgReaderClass(Dp2NfvType dp2_nfv, NfFgType nffg){
		this.dp2_nfv=dp2_nfv;
		this.nffg=nffg;
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

	
	//the node has to be searched in the whole dp2_nfv, because it has a unique name in the whole dp2_nfv, not in the nffg
	@Override
	public NodeReader getNode(String name) {
		// Gives access to a single network node given its name (a node is uniquely identified in the DP2-NFV system by its name)
		if(nffg==null|| dp2_nfv==null || name==null)
			return null;
		List<NfFgType> nffg_list = dp2_nfv.getNfFg();
		if(nffg_list==null)
			return null;
		for(NfFgType nffg:nffg_list){
			List<NodeType> node_list = nffg.getNode();
			if(node_list!=null){
				for(NodeType node:node_list){
					if(node!=null){
						if(node.getName().equals(name)){
							return new NodeReaderClass(node, dp2_nfv);
						}
					}
				}
			}
		}
		
		return null;
	}

	@Override
	public Set<NodeReader> getNodes() {
		//Gives access to the set of network nodes of this NF-FG.
		if(nffg==null||dp2_nfv==null)
			return null;
		List<NodeType>node_list = nffg.getNode();
		if(node_list==null)
			return null;
		Set<NodeReader> node_set = new HashSet<NodeReader>();
		for(NodeType node:node_list){
			if(node!=null)
				node_set.add(new NodeReaderClass(node, dp2_nfv));
		}
		return node_set;
	}

}
