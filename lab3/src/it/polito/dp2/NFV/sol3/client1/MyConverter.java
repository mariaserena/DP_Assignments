package it.polito.dp2.NFV.sol3.client1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.polito.dp2.NFV.NodeReader;
import it.polito.dp2.NFV.VNFTypeReader;
import it.polito.dp2.NFV.lab3.LinkDescriptor;
import it.polito.dp2.NFV.lab3.NffgDescriptor;
import it.polito.dp2.NFV.lab3.NodeDescriptor;


//class that converts from Descriptors to Types
public class MyConverter {
	Map<NodeDescriptor, String> nodeNameMap = new HashMap<NodeDescriptor, String>();
	long nextNode = 0;
	long nextLink = 0;
	
	public NffgType buildNffgType (NffgDescriptor nffg_d){
		NffgType nffg_t = new NffgType();
		if(nffg_d == null)
			return null;
		Set<NodeDescriptor> node_d_list = nffg_d.getNodes();
		if(node_d_list == null)
			return null;
		List<NodeType> node_t_list = nffg_t.getNodeType();
		
		for (NodeDescriptor nd: node_d_list){
			NodeType nt = buildNodeType(nd);
			node_t_list.add(nt);
			nodeNameMap.put(nd, nt.getName());
		}
		for (NodeDescriptor nd: node_d_list){
			//if this node has links -> create them
			if(nd.getLinks()!=null){
				for(LinkDescriptor ld: nd.getLinks()){
					LinkType lt = buildLinkType(ld);
					
					for(NodeType nty: nffg_t.getNodeType()){
						if(nty.getName().equals(lt.getSourceNode())){
							nty.getLinkType().add(lt);
						}
					}
				}
			}
		}
		nffg_t.setName("Nffg");
		return nffg_t;
	}

	public NodeType buildNodeType (NodeDescriptor node_d){
		if(node_d == null)
			return null;
		NodeType node_t = new NodeType();
		node_t.setHost(node_d.getHostName());
		node_t.setVNFT(node_d.getFuncType().getName());
		node_t.setName("n"+nextNode);
		nextNode=nextNode+1;
		return node_t;
	}
	
	public LinkType buildLinkType (LinkDescriptor link_d){
		if(link_d == null)
			return null;
		LinkType link_t = new LinkType();
		link_t.setMinThrough(link_d.getThroughput());
		link_t.setMaxLatency(link_d.getLatency());
		link_t.setSourceNode(nodeNameMap.get(link_d.getSourceNode()));
		link_t.setDestNode(nodeNameMap.get(link_d.getDestinationNode()));
		link_t.setName("l"+nextLink);
		nextLink=nextLink+1;
		return link_t;
	}
	
	public NodeType buildNodeType (VNFTypeReader type, String hostName, String nffgName){
		NodeType node_t = new NodeType();
		if(type==null || nffgName==null)
			return null;
		node_t.setHost(hostName);
		node_t.setNffgName(nffgName);
		node_t.setVNFT(type.getName());
		node_t.setName("n"+nextNode);
		nextNode=nextNode+1;
		return node_t;
		
	}
	
	public LinkType buildLinkType(NodeReader srcNode, NodeReader dstNode){
		LinkType link_t = new LinkType();
		if(srcNode == null || dstNode == null)
			return null;
		link_t.setDestNode(dstNode.getName());
		link_t.setSourceNode(srcNode.getName());
		link_t.setMinThrough(Float.valueOf(0));
		link_t.setMaxLatency(Integer.valueOf(0));
		link_t.setName("l"+nextLink);
		nextLink=nextLink+1;
		return link_t;	
	}
}
