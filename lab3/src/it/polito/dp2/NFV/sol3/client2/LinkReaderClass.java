package it.polito.dp2.NFV.sol3.client2;

import java.util.List;
import it.polito.dp2.NFV.LinkReader;
import it.polito.dp2.NFV.NodeReader;

public class LinkReaderClass implements LinkReader {
	private LinkType link;
	private NffgsType nffgs;
	private VnftsType vnfts;
	private HostsType hosts;
	
	
	public LinkReaderClass(LinkType link, NffgsType nffgs, VnftsType vnfts, HostsType hosts){
		this.link=link;
		this.nffgs = nffgs;
		this.vnfts = vnfts;
		this.hosts = hosts;
		
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
		if(link==null){
			return null;
		}
		String dest_node_name = link.getDestNode();
		if(dest_node_name.equals(null))
			return null;
		
		for(NffgType nffg:this.nffgs.getNffgType()){
			List<NodeType> node_list = nffg.getNodeType();
			if(node_list!=null){
				for(NodeType node:node_list){
					if(node!=null){
						if(node.getName().equals(dest_node_name)){
							return new NodeReaderClass(node, vnfts, hosts, nffgs);
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
		if(link==null){
			return null;
		}
		String source_node_name = link.getSourceNode();
		if(source_node_name.equals(null))
			return null;
		
		for(NffgType nffg:this.nffgs.getNffgType()){
			List<NodeType> node_list = nffg.getNodeType();
			if(node_list!=null){
				for(NodeType node:node_list){
					if(node!=null){
						if(node.getName().equals(source_node_name)){
							return new NodeReaderClass(node, vnfts, hosts, nffgs);
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
