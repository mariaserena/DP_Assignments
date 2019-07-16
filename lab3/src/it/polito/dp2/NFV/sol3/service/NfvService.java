package it.polito.dp2.NFV.sol3.service;

import java.math.BigInteger;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.DatatypeConfigurationException;
import java.net.URI;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.polito.dp2.NFV.NfvReaderException;
import javax.ws.rs.core.UriBuilder;
import javax.xml.datatype.XMLGregorianCalendar;


public class NfvService {
	
	private static final String defaultPath = "http://localhost:8080/NfvDeployer/rest";
	private static final String URIsysProp = "it.polito.dp2.Nfv.lab3.URL";
	
	private static Logger logger= Logger.getLogger(NfvService.class.getName());
	
	
	private  Map<String, NffgType> nffgDB;
	private  Map<String, HostType> hostDB;
	private  Map<String, VnftType> vnftDB;
	private  Map<String, PerformanceType> perfDB;
	
	private static NfvDB db;
	private DatatypeFactory dtf;
	private static String basePath;
	private Neo4jService neo4jservice;
	
	//constructor 
	public NfvService() throws MyServerErrorException, Neo4jServiceException{
		//instantiate the DBs
		basePath = getBaseURI().toString();
		
		try{
			neo4jservice = new Neo4jService();
			dtf = DatatypeFactory.newInstance();
			db = NfvDB.getInstance();
			
			//retrieve the DBs
			nffgDB = NfvDB.getNffgDB();
			hostDB = NfvDB.getHostDB();
			vnftDB = NfvDB.getVnftDB();
			perfDB = NfvDB.getPerfDB();
			
		}catch(NfvReaderException nfvre) {
			logger.log(Level.SEVERE, "failure in the NfvReader", nfvre);
			throw new MyServerErrorException(nfvre.getMessage());	
		}catch(DatatypeConfigurationException dtce){
			logger.log(Level.SEVERE, "failure in the instantiation of the Datatype factory", dtce);
			throw new MyServerErrorException(dtce);
		}catch(NullPointerException npe){
			logger.log(Level.SEVERE, "failure in the instantiation of the neo4j service", npe);
			throw new MyServerErrorException(npe);
		}catch (Exception e) {
			logger.log(Level.SEVERE, "failure in init phase", e);
			throw new MyServerErrorException(e);
		}
		
	}
	
	public NffgsType getNffgs(){
		logger.log(Level.FINE, "getNffgs called");
		//data to return 
		NffgsType nffgs = new NffgsType();
		List<NffgType> nffgList = nffgs.getNffgType();
	
		nffgList.addAll(nffgDB.values());
		return nffgs;
	}

	public NffgType postNffg(NffgType nffg) throws Neo4jServiceException, MyForbiddenException, MyBadRequestException{
		logger.log(Level.FINE, "postNffgs called");
		if(nffg==null)
			throw new MyBadRequestException("null nffg, error in request");
		
		Map<String, String> mapping= new HashMap<String,String>();
		String old_name;
		String new_name;

		String nffg_name = "Nffg"+NfvDB.getNextNffg();
		nffg.setName(nffg_name);
		for(NodeType node_t : nffg.getNodeType()){
			old_name = node_t.getName();
			new_name = "Node"+NfvDB.getNextNode();
			mapping.put(old_name, new_name);
			node_t.setName(new_name);
		}
		for(NodeType node_t : nffg.getNodeType()){
			for(LinkType link_t: node_t.getLinkType()){
				link_t.setName("Link"+NfvDB.getNextLink());
				//update the name of scrnode and destnode
				link_t.setDestNode(mapping.get(link_t.getDestNode()));
				link_t.setSourceNode(mapping.get(link_t.getSourceNode()));
			}
		}
		
		//this part is synchronized because allocNodes checks that there is availability on the host
		//then a loadNffg is performed and hosts values are updated. If another thread performs the
		//same operation or postNode there may be conflicts.
		synchronized(NfvDB.getHostSynch()){
			for(NodeType nr : nffg.getNodeType()){
				nr.setHost(allocNode(nr));
				//if the allocNode cannot allocate the node, return null
				//else it returns the name of the host on which the node can be allocated on
				if(nr.getHost()==null){
					logger.log(Level.SEVERE, "not possible to alloc some or all the nodes on the hosts");
					throw new MyForbiddenException("not possible to alloc some or all the nodes on the hosts");
				}
			}
		
			//we can now save the nffg on the Neo4j
			Map<String, String> mapNodeId;
			try{
				mapNodeId= neo4jservice.loadNffg(nffg);
				//once loaded the nodes on the remote DB, we have to upload the data on the local DB
				
				//set the deploy time
				GregorianCalendar gc = new GregorianCalendar();
				XMLGregorianCalendar xml_gc = dtf.newXMLGregorianCalendar(gc);
				nffg.setDeployTime(xml_gc);
				
				//update the value of the hosts in the DB
				updateHostsValues(nffg.getNodeType());
				nffgDB.put(nffg.getName(), nffg);	
				return nffg; 
				
			}
			catch (Neo4jServiceException nje){
				logger.log(Level.SEVERE, "failure in Neo4jService", nje);
				throw new Neo4jServiceException(nje.getMessage());
			}
		}
	}
	
	public NffgType getNffg(String id) throws MyNotFoundException{
		NffgType nffg = nffgDB.get(id);
		if(nffg==null){
			logger.log(Level.SEVERE, "nffg wiht name "+id+" is not in the dp2_nfv system");
			throw new MyNotFoundException("nffg wiht name "+id+" is not in the dp2_nfv system");
		}
		return nffg;
	}
	
	public NodesType getNodes(String nffg_id) throws MyNotFoundException{
		if(nffg_id == null){
			logger.log(Level.SEVERE,"the nffg has an invalid name" );
			throw new MyNotFoundException("the nffg has an invalid name");
		}
		NffgType nffg = nffgDB.get(nffg_id);
		if(nffg==null){
			logger.log(Level.SEVERE,"nffg wiht name "+nffg_id+" is not in the dp2_nfv system");
			throw new MyNotFoundException("nffg wiht name "+nffg_id+" is not in the dp2_nfv system");
		}
		NodesType nodes = new NodesType();
		nodes.getNodeType().addAll(nffg.getNodeType());
		
		return nodes;
	}

	
	public NodeType postNode(NodeType node, String nffg_id) throws MyNotFoundException, MyBadRequestException, MyForbiddenException, Neo4jServiceException{
		logger.log(Level.FINE, "postNode called");
			if(node==null){
				logger.log(Level.SEVERE,"bad request, node to be posted is null" );
				throw new MyBadRequestException("bad request, node to be posted is null");
			}
			//set the node name (it has no links, so no link names have to be chosen)
			node.setName("Node"+NfvDB.getNextNode());
			
			//if the nffg to which the node belongs to is in the local DB, it means it is deployed
			NffgType nffg = nffgDB.get(nffg_id);
			if(nffg!=null){
			synchronized(NfvDB.getHostSynch()){
				node.setHost(allocNode(node));
				if(node.getHost() == null){
					logger.log(Level.SEVERE, "not able to alloc the node on any host");	
					throw new MyForbiddenException("it's not possible to alloc the node on any host");
				}
				//add the node to the neo4jDB
				try{
					String nodeID = neo4jservice.loadNode(node);
					if(nodeID == null){
						logger.log(Level.SEVERE, "error while uploading a node in Neo4jSimpleXML service");
						throw new Neo4jServiceException("error in the Neo4jSimpleXML while uploading a node");
				}
					
				List<NodeType> ntl = new ArrayList<NodeType>();
				ntl.add(node);
				updateHostsValues(ntl);
				
				nffg.getNodeType().add(node);
				nffgDB.put(nffg.getName(), nffg);
				return node;
			
			}catch(Neo4jServiceException njse){
				throw new Neo4jServiceException(njse.getMessage());
			}
		}
			}
			else{
				throw new MyNotFoundException("the nffg "+nffg_id+" is not deployed in the system");
			}
			
	}
	
	public NodeType getNode(String nffg_id,String node_id ) throws  MyNotFoundException{
		logger.log(Level.FINE, "getNode called");
		NffgType nffg = nffgDB.get(nffg_id);
		if(nffg==null){
			logger.log(Level.SEVERE, "nffg wiht name "+nffg_id+" is not in the dp2_nfv system");
			throw new MyNotFoundException("nffg wiht name "+nffg_id+" is not in the dp2_nfv system");
		}
		List<NodeType> nodeList = nffg.getNodeType();
		for(NodeType node: nodeList){
			if(node.getName().equals(node_id)){
				return node;
			}
		}
		logger.log(Level.SEVERE, "node with name "+node_id+" is not in the dp2_nfv system in the "+nffg_id+" nffg");
		throw new MyNotFoundException("node with name "+node_id+" is not in the dp2_nfv system in the "+nffg_id+" nffg");
	}

	public HostsType getReachableHosts(String nffg_id, String node_id) throws MyNotFoundException, Neo4jServiceException{
		logger.log(Level.FINE, "getReachableHosts called");
		HostsType reachHosts = new HostsType();
		List<HostType> reachHostList = reachHosts.getHostType();
		//we have to get the node
		NffgType nffg = nffgDB.get(nffg_id);
		if(nffg==null){
			logger.log(Level.SEVERE,"nffg with name "+nffg_id+" does not exist in the system");
			throw new MyNotFoundException("nffg with name "+nffg_id+" does not exist in the system");
		}
		List<NodeType> nodeList = nffg.getNodeType();
		for(NodeType node: nodeList){
			if(node.getName().equals(node_id)){
				try{
					Set<String> hosts_names = neo4jservice.getReachableHosts(node_id);
					//but we have to return a HostsType. getReachableHosts returns the list of hostNames reachable from this node
					for(String host_name : hosts_names){
						//we have to retrieve the hostType from the host name
						HostType reachHost = hostDB.get(host_name);
						HostType new_h = new HostType();
						new_h.setDiskStorage(reachHost.getDiskStorage());
						new_h.setMaxVnf(reachHost.getMaxVnf());
						new_h.setMemory(reachHost.getMemory());
						new_h.setName(reachHost.getName());
						new_h.getNode().addAll(reachHost.getNode());
						reachHostList.add(reachHost);
					}
					return reachHosts;
				}catch(Neo4jServiceException n){
					throw new Neo4jServiceException(n.getMessage());
				}
			}
		}
		throw new MyNotFoundException("node with name "+node_id+" does not exist in the nffg "+nffg_id);
	}
	
	
	public LinksType getLinks(String nffg_id, String node_id) throws MyNotFoundException{
		logger.log(Level.FINE, "getLinks called");
		NffgType nffg;
		LinksType links = new LinksType();
		nffg = nffgDB.get(nffg_id);
		if(nffg==null){
			logger.log(Level.SEVERE, "nffg wiht name "+nffg_id+" is not in the dp2_nfv system");
			throw new MyNotFoundException("nffg wiht name "+nffg_id+" is not in the dp2_nfv system");
		}
		
		for(NodeType n: nffg.getNodeType()){
			if(n.getName().equals(node_id)){
				links.getLinkType().addAll(n.getLinkType());
				return links;
			}
		}
		logger.log(Level.SEVERE,"node wiht name "+node_id+" is not in the dp2_nfv system in the nffg "+nffg_id);
		throw new MyNotFoundException("node wiht name "+node_id+" is not in the dp2_nfv system in the nffg "+nffg_id);
	}
	
	public LinkType postLink(LinkType link, String nffg_id, String node_id, boolean overwrite) throws Neo4jServiceException, MyNotFoundException, MyForbiddenException, MyBadRequestException{
		logger.log(Level.FINE, "postLinks called");
		String linkName = null;
		if(link == null){
			logger.log(Level.SEVERE,"wrong query parameters. link to post is null");
			throw new MyBadRequestException("wrong query parameters. link to post is null");
		}
		
		//if two threads try to add the same link and they have overwrite = false, there may be issues
		synchronized(NfvDB.getNffgSynch()){
			//check the existance of nffg and node
			NffgType nffg = nffgDB.get(nffg_id);
			if(nffg == null){
				logger.log(Level.SEVERE,"nffg with name "+nffg_id+" not deployed in the system");
				throw new MyNotFoundException("nffg with name "+nffg_id+" not deployed in the system");
			}
			//check the existance of both the nodes
			for(NodeType src_node: nffg.getNodeType()){
				if(src_node.getName().equals(link.getSourceNode())){
					for(NodeType dest_node: nffg.getNodeType()){
						if(dest_node.getName().equals(link.getDestNode())){
	
							LinkType linkToRemove = null;
							//check if the link already exists
							for(LinkType l: src_node.getLinkType()){
							if(l.getSourceNode().equals(link.getSourceNode())&& l.getDestNode().equals(link.getDestNode()) && overwrite==false){
								throw new MyForbiddenException("node already existing. cannot be overwritten");
							}
							else if(l.getSourceNode().equals(link.getSourceNode())&& l.getDestNode().equals(link.getDestNode()) && overwrite==true){
								linkName = l.getName();
								linkToRemove = l;
								break;
							}
						}				
					
							if(linkName== null){
								link.setName("Link"+NfvDB.getNextLink());
							}
							else{link.setName(linkName);}
									
							try{
							String linkId = neo4jservice.loadLink(link);
								if(linkId == null){
									logger.log(Level.SEVERE,"error while uploading a link in neo4j DB");
									throw new Neo4jServiceException("error while uploading a link in neo4j DB");
								}
										
							//if the link already exists, the old link is deleted 
							if(linkToRemove!=null){
								src_node.getLinkType().remove(linkToRemove);
								dest_node.getLinkType().remove(linkToRemove);
							}
							src_node.getLinkType().add(link);
							nffgDB.put(nffg_id, nffg);
							return link;
							}catch(Neo4jServiceException e){
								throw new Neo4jServiceException(e.getMessage());
							}
						}
					}
				}
			}
			logger.log(Level.SEVERE,"one of the 2 nodes does not exist");
			throw new MyForbiddenException("one of the 2 nodes does not exist");
		}
		
	}
	

	public HostsType getHosts(){
		logger.log(Level.FINE, "getHosts called");
		HostsType hosts = new HostsType();
		//retrieve from the hosts only the necessary info, without disclosing the internal info
		for(HostType h: hostDB.values()){
			HostType new_h = new HostType();
			new_h.setDiskStorage(h.getDiskStorage());
			new_h.setMaxVnf(h.getMaxVnf());
			new_h.setMemory(h.getMemory());
			new_h.setName(h.getName());
			new_h.getNode().addAll(h.getNode());
			hosts.getHostType().add(new_h);
		}
		return hosts;
	}


	public HostType getHost(String host_id) throws MyNotFoundException {
		logger.log(Level.FINE, "getHost called");
		for(HostType h: hostDB.values()){
			if(h.getName().equals(host_id)){
				HostType new_h = new HostType();
				new_h.setDiskStorage(h.getDiskStorage());
				new_h.setMaxVnf(h.getMaxVnf());
				new_h.setMemory(h.getMemory());
				new_h.setName(h.getName());
				new_h.getNode().addAll(h.getNode());
				return new_h;
			}
		}
		throw new MyNotFoundException("host with name "+host_id+" is not in the dp2_nfv system");
	}

	
	public PerformancesType getPerformance()throws MyNotFoundException {
		logger.log(Level.FINE, "getPerformance called");
		PerformancesType perf= new PerformancesType();
		perf.getPerformanceType().addAll(perfDB.values());
		if(perf == null){
			logger.log(Level.SEVERE, "one or both the hosts in the searched connection do not exist");
			throw new MyNotFoundException("one or both the hosts in the searched connection do not exist");
		}
		return perf;
	}
	
	public VnftsType getVnfts() {
		logger.log(Level.FINE, "getVnfts called");
		VnftsType vnfts = new VnftsType();
		vnfts.getVnftType().addAll(vnftDB.values());
		return vnfts;
	}
	
	//retrieve the uri of the service from the dedicated system property
	private static URI getBaseURI(){
		String uri_string = null;
		URI uri;
		uri_string = System.getProperty(URIsysProp);
		if(uri_string==null){
			uri_string =defaultPath;
		}
		uri = UriBuilder.fromUri(uri_string).build();
		return uri;
	}

	
	private void updateHostsValues(List<NodeType> nodes) {
		// update the values of the hosts after that a new Nffg is deployed
		//or a new Node is allocated on a host
		for(NodeType n: nodes){
			VnftType v = vnftDB.get(n.getVNFT());
			HostType h = hostDB.get(n.getHost());	
			List<String> allocated_nodes = h.getNode();
			allocated_nodes.add(n.getName());
			h.setLeftDiskStorage(h.getLeftDiskStorage()-v.getDiskStorage());
			h.setLeftMemory(h.getLeftMemory()-v.getMemory());
			h.setVnf(h.getVnf().add(BigInteger.valueOf(1)));
			hostDB.put(h.getName(), h);
		}
		
	}
	
	private String allocNode(NodeType node){
		//retrieve the correspondent vnft 
		VnftType vnft = vnftDB.get(node.getVNFT());
		//check if the client has chosen an host on which allocate the node on
		if(node.getHost()!=null && vnft!=null){
			HostType sel_host = hostDB.get(node.getHost());
			if(sel_host!=null){
				//check if the node can be allocated on this host
				if(sel_host.getMaxVnf().compareTo(sel_host.getVnf())==1 && sel_host.getLeftDiskStorage()>=vnft.getDiskStorage() && sel_host.getLeftMemory()>=vnft.getMemory()){
					return sel_host.getName();
				}
			}
		}
		//if not, check which is the first host the node can be allocated on
		logger.log(Level.SEVERE,"the selected host is not present or not available");
		for(HostType host: hostDB.values()){
			if(host.getMaxVnf().compareTo(host.getVnf())==1 && host.getLeftDiskStorage()>=vnft.getDiskStorage() && host.getLeftMemory()>=vnft.getMemory())
				return host.getName();
		}
		return null;
	}

}
