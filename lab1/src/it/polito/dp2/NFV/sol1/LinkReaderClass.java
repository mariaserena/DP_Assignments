package it.polito.dp2.NFV.sol1;

import java.util.List;
import it.polito.dp2.NFV.LinkReader;
import it.polito.dp2.NFV.NodeReader;

import it.polito.dp2.NFV.sol1.jaxb.LinkType;
import it.polito.dp2.NFV.sol1.jaxb.Dp2NfvType;
import it.polito.dp2.NFV.sol1.jaxb.NfFgType;
import it.polito.dp2.NFV.sol1.jaxb.NodeType;


public class LinkReaderClass implements LinkReader {
	private LinkType link;
	private Dp2NfvType dp2_nfv;
	
	public LinkReaderClass(LinkType link,Dp2NfvType dp2_nfv){
		this.link=link;
		this.dp2_nfv=dp2_nfv;
	}
	
	@Override
	public String getName() {
		if(link==null){
			return null;
		}
		return link.getName();
	}

	@Override
	public NodeReader getDestinationNode() {
		if(link==null || dp2_nfv==null){
			return null;
		}
		String dest_node_name = link.getDestNode();
		if(dest_node_name.equals(null))
			return null;
		List<NfFgType> nffg_list = dp2_nfv.getNfFg();
		if(nffg_list==null)
			return null;
		for(NfFgType nffg:nffg_list){
			List<NodeType> node_list = nffg.getNode();
			if(node_list!=null){
				for(NodeType node:node_list){
					if(node!=null){
						if(node.getName().equals(dest_node_name)){
							return new NodeReaderClass(node, dp2_nfv);
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public int getLatency() {
		if(link==null){
			return 0;
		}
		return link.getMaxLatency();
	}

	@Override
	public NodeReader getSourceNode() {
		if(link==null|| dp2_nfv==null){
			return null;
		}
		String source_node_name = link.getSourceNode();
		if(source_node_name.equals(null))
			return null;
		List<NfFgType> nffg_list = dp2_nfv.getNfFg();
		if(nffg_list==null)
			return null;
		for(NfFgType nffg:nffg_list){
			List<NodeType> node_list = nffg.getNode();
			if(node_list!=null){
				for(NodeType node:node_list){
					if(node!=null){
						if(node.getName().equals(source_node_name)){
							return new NodeReaderClass(node, dp2_nfv);
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public float getThroughput() {
		if(link==null){
			return 0;
		}
		return link.getMinThrough();
	}

}
