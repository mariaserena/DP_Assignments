package it.polito.dp2.NFV.sol3.client1;

import java.util.HashSet;
import it.polito.dp2.NFV.NodeReader;
import it.polito.dp2.NFV.LinkReader;
import it.polito.dp2.NFV.HostReader;
import it.polito.dp2.NFV.NffgReader;
import java.util.List;
import java.util.Set;
import it.polito.dp2.NFV.VNFTypeReader;

public class NodeReaderClass implements NodeReader{
	private NodeType node;
	private VnftsType vnfts;
	private HostsType hosts;
	private NffgsType nffgs;
	
	public NodeReaderClass(NodeType node, VnftsType vnfts, HostsType hosts,NffgsType nffgs){
		this.node = node;
		this.vnfts = vnfts;
		this.hosts = hosts;
		this.nffgs = nffgs;
	
	}
	
	@Override
	public String getName() {
		if(node==null){
			return null;
		}
		return node.getName();
	}

	@Override
	public VNFTypeReader getFuncType() {
		if(node==null){
			return null;
		}
		String vnft_name = node.getVNFT();
		if(vnft_name.equals(null))
			return null;
		for(VnftType vnft: this.vnfts.getVnftType()){
			if(vnft.getName().equals(vnft_name)){
				return new VNFTypeReaderClass(vnft);
			}
		}
		return null;
	}

	@Override
	public HostReader getHost() {
		// Gives access to the Host this node is allocated on.
		if(node==null){
			return null;
		}
		String host_name = node.getHost();
		for(HostType host:this.hosts.getHostType()){
			if(host.getName().equals(host_name)){
				return new HostReaderClass(host, nffgs, vnfts, hosts);
			}
		}
		return null;
	}

	@Override
	public Set<LinkReader> getLinks() {
		// Gives the known links that connect this node 
		//(i.e. have this node as the source node) in the network topology.
		if(node==null){
			return null;
		}
		List<LinkType> link_list = node.getLinkType();
		Set<LinkReader> link_set = new HashSet<LinkReader>();
		if(link_list==null){
			return null;
		}
		for(LinkType link:link_list){
			link_set.add(new LinkReaderClass(link,nffgs,vnfts,hosts));
		}
		return link_set;
	}

	@Override
	public NffgReader getNffg() {
		if(node==null){
			return null;
		}
		
		for(NffgType nffg:this.nffgs.getNffgType()){
			if(nffg.getName().equals(node.getNffgName()))
				return new NffgReaderClass(nffg, nffgs, vnfts, hosts);
		}
		return null;
		
	}

}
