package it.polito.dp2.NFV.sol3.service;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.polito.dp2.NFV.sol3.service.neo4j.*;

public class Neo4jService {
	private static final String defaultPath = "http://localhost:8080/Neo4JSimpleXML/webapi";
	private static final String URIsysProp = "it.polito.dp2.NFV.lab3.Neo4JSimpleXMLURL";
	private static Logger logger = Logger.getLogger(Neo4jService.class.getName());
	
	private  Map<String, String> hostIdDB;
	private  Map<String, String> revHostIdDB;
	private  Map<String, String> nodeIdDB;
	private WebTarget target;
	
	
	//constructor
	public Neo4jService() throws NullPointerException{
		try{
			this.target=ClientBuilder.newClient().target(getBaseURI());
			
			//retrieve the needed DBS
			hostIdDB = NfvDB.getHostIdDB();
			nodeIdDB = NfvDB.getNodeIdDB();
			revHostIdDB = NfvDB.getRevHostIdDB();
		}catch(NullPointerException e){
			logger.log(Level.SEVERE, "failure in the reading of the Neo4J service URL", e);
			throw new NullPointerException();
		}
	}
	
	public Map<String, String> loadHosts(Set<HostType> hr_set) throws Neo4jServiceException {
		if(hr_set == null){
			return new HashMap<String, String>(); 
		}
		Property neo_prop;
		Node neo_node;
		Node neo_rnode;
		Map<String, String> graphNodeMap = new HashMap<String,String>();
		
		for(HostType host:hr_set){
			if(host!=null){
				neo_prop = new Property();
				Properties props = new Properties();
				neo_node = new Node();
				List<Property> prop_list = props.getProperty(); 
				
				////perform the post request 
				//set the property name and value
				neo_prop.setName("name");
				neo_prop.setValue(host.getName());
				//add the property to the properties live list
				prop_list.add(neo_prop);
				neo_node.setProperties(props);
				
				neo_rnode = null;
				//post - create a new host
				try{
					
					neo_rnode = target.path("/data/node/").request().post(Entity.entity(neo_node, MediaType.APPLICATION_XML), Node.class);
				}catch(ProcessingException pe){
					logger.log(Level.SEVERE, "unable to upload a nffg host in the NEO4J database");
					throw new Neo4jServiceException(pe);
				}catch(WebApplicationException wae){
					logger.log(Level.SEVERE,"server returned error while trying to upload a nffg host");
					throw new Neo4jServiceException(wae);
				}catch(Exception e){
					logger.log(Level.SEVERE,"unexpected exception occurred while trying to upload a nffg host");
					throw new Neo4jServiceException(e);
				}
				//check the post response
				if(neo_rnode==null){
					logger.log(Level.SEVERE,"failure in posting a nffg host. no response from the server");
					throw new Neo4jServiceException();
				}
				else{	
					hostIdDB.put(host.getName(), neo_rnode.getId());
					revHostIdDB.put(neo_rnode.getId(), host.getName());
					graphNodeMap.put(host.getName(), neo_rnode.getId());
					
					//create the label
					Labels labels = new Labels();
					List<String> lab_list = labels.getLabel();
					lab_list.add("Host");
					
					//post - create a label for a node
					try{
						target.path("/data/node/"+neo_rnode.getId()+"/labels").request().post(Entity.entity(labels, MediaType.APPLICATION_XML));
					}catch(ProcessingException pe){
						logger.log(Level.SEVERE,"unable to upload a nffg node label in the NEO4J database");
						throw new Neo4jServiceException(pe);
					}catch(WebApplicationException wae){
						logger.log(Level.SEVERE,"server returned error while trying to upload a nffg node label");
						throw new Neo4jServiceException(wae);
					}catch(Exception e){
						logger.log(Level.SEVERE,"unexpected exception occurred while trying to upload a nffg node label");
						throw new Neo4jServiceException(e);
					}
				}
			}
		}
		return graphNodeMap;
	}
	
	
	public Map<String,String> loadNffg(NffgType nffg) throws Neo4jServiceException {
		Node neo_rnode;
		Property neo_prop;
		Node neo_node;
		List<NodeType> node_set;
		Relationship neo_rel;
		Relationship neo_rrel;
		String dstNodeId = null;
		String srcNodeId = null;

		if(nffg==null){
			throw new Neo4jServiceException("null nffg");
		}
		
		node_set =  nffg.getNodeType();
		if (node_set==null){
			logger.log(Level.SEVERE, "null set of nodes in the nffg");
			throw new Neo4jServiceException("null set of nodes in the nffg");
		}	
		Map<String, String> graphNodeMap= new HashMap<String, String>(); 
		//add all the nodes of the nffg
		for(NodeType node: node_set){
			if(node!=null){
				//create the data to send
				neo_prop = new Property();
				Properties props = new Properties();
				neo_node = new Node();
				List<Property> prop_list = props.getProperty(); 

				////perform the post request 
				//set the property name and value
				neo_prop.setName("name");
				neo_prop.setValue(node.getName());
				//add the property to the properties live list
				prop_list.add(neo_prop);
				neo_node.setProperties(props);	
				
				neo_rnode = null;
				
				//post - create a node
				try{
					
					neo_rnode = target.path("/data/node/").request().post(Entity.entity(neo_node, MediaType.APPLICATION_XML), Node.class);
				}catch(ProcessingException pe){
					logger.log(Level.SEVERE,"unable to upload a nffg node in the NEO4J database");
					throw new Neo4jServiceException(pe);
				}catch(WebApplicationException wae){
					logger.log(Level.SEVERE,"server returned error while trying to upload a nffg node");
					throw new Neo4jServiceException(wae);
				}catch(Exception e){
					logger.log(Level.SEVERE,"unexpected exception occurred while trying to upload a nffg node");
					throw new Neo4jServiceException(e);
				}
				//check the post response
				if(neo_rnode==null){
					logger.log(Level.SEVERE,"null post response received from Neo4jservice while trying to upload a node");
					throw new Neo4jServiceException("null post response received from Neo4jservice while trying to upload a node");
				}
				else{							
					///if the node was correctly created in the DB, add it to the list of added_graphNodes
					graphNodeMap.put(node.getName(), neo_rnode.getId());
					
					//create the label
					Labels labels = new Labels();
					List<String> lab_list = labels.getLabel();
					lab_list.add("Node");
				
					//post - create a label for a node
					try{
						target.path("/data/node/"+neo_rnode.getId()+"/labels").request().post(Entity.entity(labels, MediaType.APPLICATION_XML));
					}catch(ProcessingException pe){
						logger.log(Level.SEVERE,"unable to upload a nffg node label in the NEO4J database");
						throw new Neo4jServiceException(pe);
					}catch(WebApplicationException wae){
						logger.log(Level.SEVERE,"server returned error while trying to upload a nffg node label");
						throw new Neo4jServiceException(wae);
					}catch(Exception e){
						logger.log(Level.SEVERE,"unexpected exception occurred while trying to upload a nffg node label");
						throw new Neo4jServiceException(e);
					}
				}
			}
		}	
		//for all the nodes in the nffg retrieve the links
		for(NodeType node: node_set){
			if(node!=null){
				srcNodeId = graphNodeMap.get(node.getName());
				//retrieve the list of links
				List<LinkType> link_list = node.getLinkType();
				if(link_list!= null){
					for(LinkType link : link_list){
						//post a relationship of type ForwardsTo
						if(link!=null && link.getDestNode()!=null && link.getSourceNode()!=null){	
							dstNodeId = graphNodeMap.get(link.getDestNode());
					
							//create the data to send
							neo_rel = new Relationship();
							neo_rel.setDstNode(dstNodeId);
							neo_rel.setSrcNode(srcNodeId);
							neo_rel.setType("ForwardsTo");
							//post the relationship
							try{
								neo_rrel = target.path("/data/node/"+srcNodeId+"/relationships").request().post(Entity.entity(neo_rel, MediaType.APPLICATION_XML), Relationship.class);
							}catch(ProcessingException pe){
								logger.log(Level.SEVERE,"unable to upload a nffg link in the NEO4J database");
								throw new Neo4jServiceException(pe);
							}catch(WebApplicationException wae){
								logger.log(Level.SEVERE,"server returned error while trying to upload a nffg link");
								throw new Neo4jServiceException(wae);
							}catch(Exception e){
								logger.log(Level.SEVERE,"unexpected exception occurred while trying to upload a nffg link");
								throw new Neo4jServiceException(e);
							}
							//check the post response
							if(neo_rrel==null){
								throw new Neo4jServiceException();
							}
						}
					}
				}
			}
		}
			for(NodeType node: node_set){
			dstNodeId = hostIdDB.get(node.getHost()); 
			srcNodeId = graphNodeMap.get(node.getName());
			//create a AllocatedOn relationship
			neo_rel = new Relationship();
			neo_rel.setType("AllocatedOn");
			neo_rel.setSrcNode(srcNodeId);
			neo_rel.setDstNode(dstNodeId);
			
			try{
				//post - create the relationship
				neo_rrel = target.path("/data/node/"+srcNodeId+"/relationships").request().post(Entity.entity(neo_rel, MediaType.APPLICATION_XML), Relationship.class);
			}catch(ProcessingException pe){
				logger.log(Level.SEVERE,"unable to upload a nffg conenction in the NEO4J database");
				throw new Neo4jServiceException(pe);
			}catch(WebApplicationException wae){
				logger.log(Level.SEVERE,"server returned error while trying to upload a nffg connection");
				throw new Neo4jServiceException(wae);
			}catch(Exception e){
				logger.log(Level.SEVERE,"unexpected exception occurred while trying to upload a nffg connection");
				throw new Neo4jServiceException(e);
			}
			//check the post response
			if(neo_rrel==null){
				logger.log(Level.SEVERE, "null response");
				throw new Neo4jServiceException();
			}
		}
		nodeIdDB.putAll(graphNodeMap);
		return graphNodeMap;
		
	}
	
	
	//function that post a node with no links into an already deployed Nffg , returns the id of the node in the ne4jdb
	public String loadNode(NodeType node) throws Neo4jServiceException{
			String node_id = null;
			Property neo_prop;
			Node neo_node, neo_rnode;
			//create the data to send
			neo_prop = new Property();
			Properties props = new Properties();
			neo_node = new Node();
			List<Property> prop_list = props.getProperty(); 

			////perform the post request 
			//set the property name and value
			neo_prop.setName("name");
			neo_prop.setValue(node.getName());
			//add the property to the properties live list
			prop_list.add(neo_prop);
			neo_node.setProperties(props);	
			
			//post - create a node
			try{
				neo_rnode = target.path("/data/node/").request().post(Entity.entity(neo_node, MediaType.APPLICATION_XML), Node.class);
			}catch(ProcessingException pe){
				System.err.println("unable to upload a nffg node in the NEO4J database");
				throw new Neo4jServiceException(pe);
			}catch(WebApplicationException wae){
				System.err.println("server returned error while trying to upload a nffg node");
				throw new Neo4jServiceException(wae);
			}catch(Exception e){
				System.err.println("unexpected exception occurred while trying to upload a nffg node");
				throw new Neo4jServiceException(e);
			}
			//check the post response
			if(neo_rnode==null){
				throw new Neo4jServiceException();
			}
			else{							
				///if the node was correctly created in the DB, save the id in the local NodeIdDB
				node_id = neo_rnode.getId();
				
				
				//create the label
				Labels labels = new Labels();
				List<String> lab_list = labels.getLabel();
				lab_list.add("Node");
			
				//post - create a label for a node
				try{
					target.path("/data/node/"+neo_rnode.getId()+"/labels").request().post(Entity.entity(labels, MediaType.APPLICATION_XML));
				}catch(ProcessingException pe){
					logger.log(Level.SEVERE,"unable to upload a nffg node label in the NEO4J database");
					throw new Neo4jServiceException(pe);
				}catch(WebApplicationException wae){
					logger.log(Level.SEVERE,"server returned error while trying to upload a nffg node label");
					throw new Neo4jServiceException(wae);
				}catch(Exception e){
					logger.log(Level.SEVERE,"unexpected exception occurred while trying to upload a nffg node label");
					throw new Neo4jServiceException(e);
				}
			}
			
			String dstNodeId = hostIdDB.get(node.getHost());
			String srcNodeId = node_id;
			//create a AllocatedOn relationship
			Relationship neo_rel = new Relationship();
			Relationship neo_rrel;
			neo_rel.setType("AllocatedOn");
			neo_rel.setDstNode(dstNodeId);
			neo_rel.setSrcNode(srcNodeId);
			
			try{
				//post - create the relationship
				neo_rrel = target.path("/data/node/"+srcNodeId+"/relationships").request().post(Entity.entity(neo_rel, MediaType.APPLICATION_XML), Relationship.class);
			}catch(ProcessingException pe){
				logger.log(Level.SEVERE,"unable to upload a nffg conenction in the NEO4J database");
				throw new Neo4jServiceException(pe);
			}catch(WebApplicationException wae){
				logger.log(Level.SEVERE,"server returned error while trying to upload a nffg connection");
				throw new Neo4jServiceException(wae);
			}catch(Exception e){
				logger.log(Level.SEVERE,"unexpected exception occurred while trying to upload a nffg connection");
				throw new Neo4jServiceException(e);
			}
			//check the post response
			if(neo_rrel==null){
				throw new Neo4jServiceException();
			}
			nodeIdDB.put(node.getName(), node_id);
			return node_id;
	}
	
	
	public Set<String> getReachableHosts(String nodeName) throws Neo4jServiceException{
		String nodeId = nodeIdDB.get(nodeName);
		Set<String> reachableHostNames = new HashSet<String>();
		Set<String> visitedNodes = new HashSet<String>();
		reachableHostNames = visit(nodeId, visitedNodes);
		return reachableHostNames;
	}
	
	private Set<String> visit(String nodeId, Set<String> visitedNodes) throws Neo4jServiceException{
		Set<String> reachableHostNames = new HashSet<String>();
		Relationships rels;
		if(!visitedNodes.contains(nodeId)){
			visitedNodes.add(nodeId);
			try{
				rels = target.path("/data/node/"+nodeId+"/relationships/out").request().accept(MediaType.APPLICATION_XML).get(Relationships.class);
			}catch(WebApplicationException wae){
				logger.log(Level.SEVERE,"server returned error while trying to retrieve the relationship list from the db");
				throw new Neo4jServiceException(wae);
			}catch(Exception e){
				logger.log(Level.SEVERE,"unexpected exception occurred while trying to upload a nffg node");
				throw new Neo4jServiceException(e);
			}
			//check the obj returned by the get
			if(rels==null){
				logger.log(Level.SEVERE, "null response");
				throw new Neo4jServiceException();
			}
			
			//else 
			List<Relationships.Relationship> rel_list = rels.getRelationship();
			for(Relationships.Relationship rel : rel_list){
				//if the rel is a "allocationOn" rel
				if(rel.getType().equals("AllocatedOn")){
					String host_id = rel.getDstNode(); //retrieve the dest node ID, so the id of the host this node is allocated on
					//add the host id to the set of host ids reachable from this node
					logger.log(Level.SEVERE,"adding to reach host names "+ revHostIdDB.get(host_id));
					reachableHostNames.add(revHostIdDB.get(host_id));
				}
				//if the rel is a "link" rel
				else if(rel.getType().equals("ForwardsTo")){
					String node_id = rel.getDstNode();
					reachableHostNames.addAll(visit(node_id, visitedNodes));
				}
			}
		}
		return reachableHostNames;
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

	public String loadLink(LinkType link) throws Neo4jServiceException{
		//a ForwardsTo relationship has to be created for each link connecting the node with another node 
		//create the data to send
		String srcNodeId = nodeIdDB.get(link.getSourceNode());
		String dstNodeId = nodeIdDB.get(link.getDestNode());
		
		Relationship neo_rrel;
		Relationship neo_rel = new Relationship();
		neo_rel.setDstNode(dstNodeId);
		neo_rel.setSrcNode(srcNodeId);
		neo_rel.setType("ForwardsTo");
		//post the relationship
		try{
			neo_rrel = target.path("/data/node/"+srcNodeId+"/relationships").request().post(Entity.entity(neo_rel, MediaType.APPLICATION_XML), Relationship.class);
		}catch(ProcessingException pe){
			logger.log(Level.SEVERE,"unable to upload a nffg link in the NEO4J database");
			throw new Neo4jServiceException(pe);
		}catch(WebApplicationException wae){
			logger.log(Level.SEVERE,"server returned error while trying to upload a nffg link");
			throw new Neo4jServiceException(wae);
		}catch(Exception e){
			logger.log(Level.SEVERE,"unexpected exception occurred while trying to upload a nffg link");
			throw new Neo4jServiceException(e);
		}
		//check the post response
		if(neo_rrel==null){
			logger.log(Level.SEVERE, "null response");
			throw new Neo4jServiceException();
		}
		return neo_rrel.getId();
	}
}
