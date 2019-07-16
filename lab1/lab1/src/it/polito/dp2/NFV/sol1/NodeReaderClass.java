package it.polito.dp2.NFV.sol1;

import java.util.HashSet;
import it.polito.dp2.NFV.NodeReader;
import it.polito.dp2.NFV.LinkReader;
import it.polito.dp2.NFV.HostReader;
import it.polito.dp2.NFV.NffgReader;
import java.util.List;
import java.util.Set;
import it.polito.dp2.NFV.VNFTypeReader;
import it.polito.dp2.NFV.sol1.jaxb.NodeType;
import it.polito.dp2.NFV.sol1.jaxb.Dp2NfvType;
import it.polito.dp2.NFV.sol1.jaxb.LinkType;
import it.polito.dp2.NFV.sol1.jaxb.NfFgType;
import it.polito.dp2.NFV.sol1.jaxb.HostType;
import it.polito.dp2.NFV.sol1.jaxb.VNFTType;

public class NodeReaderClass implements NodeReader{
	private NodeType node;
	private Dp2NfvType dp2_nfv;
	
	public NodeReaderClass(NodeType node, Dp2NfvType dp2_nfv){
		this.node = node;
		this.dp2_nfv = dp2_nfv;
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
		List<VNFTType> vnft_list = dp2_nfv.getCatalog().getVNFT();
		for(VNFTType vnft:vnft_list){
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
		List<HostType> host_list = dp2_nfv.getIn().getHost();
		if(host_list==null)
			return null;
		for(HostType host:host_list){
			if(host.getName().equals(host_name)){
				return new HostReaderClass(host, dp2_nfv);
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
		List<LinkType> link_list = node.getLink();
		Set<LinkReader> link_set = new HashSet<LinkReader>();
		if(link_list==null){
			return null;
		}
		for(LinkType link:link_list){
			link_set.add(new LinkReaderClass(link, dp2_nfv));
		}
		return link_set;
	}

	@Override
	public NffgReader getNffg() {
		if(node==null || dp2_nfv==null){
			return null;
		}
		List<NfFgType> nffg_list = dp2_nfv.getNfFg();
		if(nffg_list==null)
			return null;
		for(NfFgType nffg:nffg_list){
			if(nffg.getName().equals(node.getNffgName()))
				return new NffgReaderClass(dp2_nfv,nffg);
		}
		return null;
		
	}

}
