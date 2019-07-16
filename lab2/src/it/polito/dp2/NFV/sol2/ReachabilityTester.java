package it.polito.dp2.NFV.sol2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import it.polito.dp2.NFV.HostReader;
import it.polito.dp2.NFV.LinkReader;
import it.polito.dp2.NFV.NffgReader;
import it.polito.dp2.NFV.NfvReader;
import it.polito.dp2.NFV.NodeReader;
import it.polito.dp2.NFV.lab2.ExtendedNodeReader;
import it.polito.dp2.NFV.lab2.AlreadyLoadedException;
import it.polito.dp2.NFV.lab2.NoGraphException;
import it.polito.dp2.NFV.lab2.ServiceException;
import it.polito.dp2.NFV.lab2.UnknownNameException;

public class ReachabilityTester implements it.polito.dp2.NFV.lab2.ReachabilityTester {
	private WebTarget target;
	private NfvReader nfv;
	private String nffgName;
	
	private List<Node> added_graphNodes;
	
	//constructor
	public ReachabilityTester(WebTarget target, NfvReader nfv){
		this.target=target;
		this.nfv=nfv;
		//this.added_nodes=new HashSet<ExtendedNodeReader>();
		this.added_graphNodes = new ArrayList<Node>();
	}
	
	@Override
	public void loadGraph(String nffgName) throws UnknownNameException, AlreadyLoadedException, ServiceException {

		Node neo_rnode;
		Property neo_prop;
		Node neo_node;
		Set<NodeReader> node_set;
		Relationship neo_rel;
		Relationship neo_rrel;
		Relationships rels;
		String dstNodeId = null;
		String srcNodeId = null;
		
	//--------------------------CHECKS
		//check if the searched nffg is present in the current nfv
		NffgReader nffg = nfv.getNffg(nffgName);
		if(nffg==null){
			throw new UnknownNameException("unable to find the seached nffg in the nfv");
		}
		// If the graph was already successfully loaded, it is not loaded again and an AlreadyLoadedException is thrown.
		// @param nffgName	the name of the NF-FG for which nodes have to be loaded
		if(this.isLoaded(nffgName)){
			throw new AlreadyLoadedException("the nffg is already loaded");
		}
	//-----------------IF ANOTHER GRAPH, CLEAN THE DB
		else if(this.nffgName != null && !this.nffgName.equals(nffgName)){
			//a different graph is already loaded, before loading the new nffg we have to delete the previous one
			
			//delete all the nodes added to the graph
			for(Node added_node: added_graphNodes){
				//retrieve all the out relationships the node is involved in
				try{
					rels = target.path("data/node/"+added_node.getId()+"/relationships/out").request().get(Relationships.class);
				}catch(ProcessingException pe){
					System.err.println("unable to retrieve the out relationship list of a nffg node from the NEO4J database");
					throw new ServiceException(pe);
				}catch(WebApplicationException wae){
					System.err.println("server returned error while trying to retrieve the out relationship list of a nffg node");
					throw new ServiceException(wae);
				}catch(Exception e){
					System.err.println("unexpected exception occurred while trying to retrieve the out relationship list of a nffg node");
					throw new ServiceException(e);
				}
				if(rels == null){
					System.out.println("failure in retrieving the out relationship list of a nffg node. no response from the server");
					throw new ServiceException();
				}
				else{
					//delete each relationship found
					List<Relationships.Relationship> rel_list = rels.getRelationship();
					if(rel_list!= null){
						for(Relationships.Relationship rel:rel_list){
							try{
								//System.out.println("------------------ "+target.path("data/relationship/"+rel.getId()));
								//System.out.println("trying to del rel. relationship id: "+rel.getId());
								target.path("data/relationship/"+rel.getId()).request().delete();
							}catch(ProcessingException pe){
								System.err.println("unable to delete a node relationship in the NEO4J database");
								throw new ServiceException(pe);
							}catch(WebApplicationException wae){
								System.err.println("server returned error while trying to delete a node relationship");
								throw new ServiceException(wae);
							}catch(Exception e){
								System.err.println("unexpected exception occurred while trying to delete a node relationship");
								throw new ServiceException(e);
							}
							System.out.println("deleted relationship with id: "+rel.getId());	
						}
					}
				}
				//delete the node
				try{
					System.out.println("trying to del the node: "+added_node.getId());
					//System.out.println("------- "+target.path("data/node/"+added_node.getId()));
					target.path("data/node/"+added_node.getId()).request().delete();
				}catch(ProcessingException pe){
					System.err.println("unable to delete a host node in the NEO4J database");
					throw new ServiceException(pe);
				}catch(WebApplicationException wae){
					System.err.println("server returned error while trying to delete a host node");
					throw new ServiceException(wae);
				}catch(Exception e){
					System.err.println("unexpected exception occurred while trying to delete a host node");
					throw new ServiceException(e);
				}
							
				//delete the node
				try{
					target.path("data/node/"+added_node.getId()).request().delete();
				}catch(ProcessingException pe){
					System.err.println("unable to delete a node in the NEO4J database");
					throw new ServiceException(pe);
				}catch(WebApplicationException wae){
					System.err.println("server returned error while trying to delete a node");
					throw new ServiceException(wae);
				}catch(Exception e){
					System.err.println("unexpected exception occurred while trying to delete a node");
					throw new ServiceException(e);
				}
				
				//CHECK IT
				System.out.println("node deleted: "+added_node.getId());
				//remove the node from the added_node list
				for (Iterator<Node> iter = added_graphNodes.listIterator(); iter.hasNext(); ) {
				    Node n = iter.next();
				    if (n.getId().equals(added_node.getId())) {
				        iter.remove();
				    }
				}
			}
		}
		
		//---------------------------- UPLOAD THE NEW GRAPH
		this.nffgName = nffgName;
		System.out.println(" --------- GRAPH NAME :"+this.nffgName); //TO DELETE
	
		////insert the nodes of the nffg in the NEO4J DB
		//retrieve the nodes in the node_set
		node_set =  nffg.getNodes();
		if (node_set==null){
			System.out.println("no node in the selected nffg");
			return;
		}	
		//add all the nodes of the nffg
		for(NodeReader node: node_set){
			if(node!=null){
				System.out.println("++++++++++++++++++++++++++++ preparing to upload node :"+node.getName());
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
					//System.out.println(target.path("data/node/"));
					neo_rnode = target.path("data/node/").request().post(Entity.entity(neo_node, MediaType.APPLICATION_XML), Node.class);
				}catch(ProcessingException pe){
					System.err.println("unable to upload a nffg node in the NEO4J database");
					throw new ServiceException(pe);
				}catch(WebApplicationException wae){
					System.err.println("server returned error while trying to upload a nffg node");
					throw new ServiceException(wae);
				}catch(Exception e){
					System.err.println("unexpected exception occurred while trying to upload a nffg node");
					throw new ServiceException(e);
				}
				//check the post response
				if(neo_rnode==null){
					System.out.println("failure in posting a nffg node. no response from the server");
					throw new ServiceException();
				}
				else{		
					System.out.println("nffg node added successfully to the NEO4J db! node id: "+neo_rnode.getId());
					
					///if the node was correctly created in the DB, add it to the list of added_graphNodes
					added_graphNodes.add(neo_rnode);
					
					//create the label
					Labels labels = new Labels();
					List<String> lab_list = labels.getLabel();
					lab_list.add("Node");
				
					//post - create a label for a node
					try{
						target.path("data/node/"+neo_rnode.getId()+"/labels").request().post(Entity.entity(labels, MediaType.APPLICATION_XML));
					}catch(ProcessingException pe){
						System.err.println("unable to upload a nffg node label in the NEO4J database");
						throw new ServiceException(pe);
					}catch(WebApplicationException wae){
						System.err.println("server returned error while trying to upload a nffg node label");
						throw new ServiceException(wae);
					}catch(Exception e){
						System.err.println("unexpected exception occurred while trying to upload a nffg node label");
						throw new ServiceException(e);
					}
					
					//add the label to the node
					for (Iterator<Node> iter = added_graphNodes.listIterator(); iter.hasNext(); ) {
					    Node n = iter.next();
					    if (n.getId().equals(neo_rnode.getId())) {
					        n.setLabels(labels);
					    }
					}	
				}
			}
		}	
		//for all the nodes in the nffg retrieve the links
		for(NodeReader node: node_set){
			//System.out.println("nome nodo sorgente: "+node.getName());
			if(node!=null){
				//retrieve the list of links
				Set<LinkReader> link_list = node.getLinks();
				if(link_list!= null){
					for(LinkReader link : link_list){
						//post a relationship of type ForwardsTo
						if(link!=null){
							//we have to search for the dest node id 
							for(Node n: added_graphNodes){
								//retrieve the property list to find the dstNodeId and the scrNodeId
								List<Property> nodeProp_list = n.getProperties().getProperty();
								for(Property p : nodeProp_list){
									//System.out.println("nodo dest?? "+p.getValue()+" == "+node.getName()+" ??");
									//System.out.println("node to reach: "+link.getDestinationNode().getName());
									//System.out.println("name property: "+p.getName());
									if(p.getName().equals("name") && p.getValue().equals(node.getName())){
										srcNodeId = n.getId();
										//System.out.println("TROVATO!!!!!");
									}
									if(p.getName().equals("name") && p.getValue().equals(link.getDestinationNode().getName())){
										dstNodeId = n.getId();
									}
								}						
							}
							//create the data to send
							neo_rel = new Relationship();
							neo_rel.setDstNode(dstNodeId);
							neo_rel.setType("ForwardsTo");
							try{
								//post - create the link
								//System.out.println("------------------- "+target.path("data/node/"+srcNodeId+"/relationships/     "+neo_rel.getDstNode()));
								
								neo_rrel = target.path("data/node/"+srcNodeId+"/relationships").request().post(Entity.entity(neo_rel, MediaType.APPLICATION_XML), Relationship.class);
							}catch(ProcessingException pe){
								System.err.println("unable to upload a nffg link in the NEO4J database");
								throw new ServiceException(pe);
							}catch(WebApplicationException wae){
								System.err.println("server returned error while trying to upload a nffg link");
								throw new ServiceException(wae);
							}catch(Exception e){
								System.err.println("unexpected exception occurred while trying to upload a nffg link");
								throw new ServiceException(e);
							}
							//check the post response
							if(neo_rrel==null){
								System.out.println("failure in posting a nffg link. no response from the server");
								throw new ServiceException();
							}
							else{		
								System.out.println("nffg link added successfully to the NEO4J db: "+srcNodeId+" "+dstNodeId);
								//add the link to something????
								//the post response carries in the body the created link
							}
					}
				}
			}
		}
	}
		
		//save the In structure
		Set<HostReader> hr_set = nfv.getHosts();
		if(hr_set == null){
			System.out.println("no In in the nffg");
			return;
		}
		for(HostReader host:hr_set){
			System.out.println("++++++++++++++++++++++++++++ preparing to upload the hosts");
			//create the data to send
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
				
			
				//post - create a new host
				try{
					//System.out.println(target.path("data/node/"));
					neo_rnode = target.path("data/node/").request().post(Entity.entity(neo_node, MediaType.APPLICATION_XML), Node.class);
				}catch(ProcessingException pe){
					System.err.println("unable to upload a nffg host in the NEO4J database");
					throw new ServiceException(pe);
				}catch(WebApplicationException wae){
					System.err.println("server returned error while trying to upload a nffg host");
					throw new ServiceException(wae);
				}catch(Exception e){
					System.err.println("unexpected exception occurred while trying to upload a nffg host");
					throw new ServiceException(e);
				}
				//check the post response
				if(neo_rnode==null){
					System.out.println("failure in posting a nffg host. no response from the server");
					throw new ServiceException();
				}
				else{		
					System.out.println("nffg host "+host.getName()+" added successfully to the NEO4J db "+neo_rnode.getId());
					//add the host to the added_hosts list
					added_graphNodes.add(neo_rnode);
					
					//create the label
					Labels labels = new Labels();
					List<String> lab_list = labels.getLabel();
					lab_list.add("Host");
					
					//post - create a label for a node
					try{
						target.path("data/node/"+neo_rnode.getId()+"/labels").request().post(Entity.entity(labels, MediaType.APPLICATION_XML));
					}catch(ProcessingException pe){
						System.err.println("unable to upload a nffg node label in the NEO4J database");
						throw new ServiceException(pe);
					}catch(WebApplicationException wae){
						System.err.println("server returned error while trying to upload a nffg node label");
						throw new ServiceException(wae);
					}catch(Exception e){
						System.err.println("unexpected exception occurred while trying to upload a nffg node label");
						throw new ServiceException(e);
					}
						
					//add the label to the node
					for (Iterator<Node> iter = added_graphNodes.listIterator(); iter.hasNext(); ) {
					    Node n = iter.next();
					    if (n.getId().equals(neo_rnode.getId())) {
					        n.setLabels(labels);
					    }
					}	
					
					//if the host was added, a relationship has to be created for each nffg node allocated in the host
					Set<NodeReader> allNode_set = host.getNodes();
					//debug
					for(NodeReader n: allNode_set){
						System.out.println(".......................................................host "+host.getName()+" allocated on node: "+n.getName());
					}
					//debug
					
					if(allNode_set!=null){
						for(NodeReader allNode:allNode_set){
							//se il nodo appartiene a questo nffg
							
							if(allNode.getNffg().getName().equals(nffgName)){
								System.out.println("considering node "+allNode.getName());
								//create a relationship
								neo_rel = new Relationship();
								neo_rel.setType("AllocatedOn");
								neo_rel.setDstNode(neo_rnode.getId());
								for(Node n: added_graphNodes){
										//retrieve the list of properties
									for(Property p : n.getProperties().getProperty()){
										if(p.getName().equals("name") && p.getValue().equals(allNode.getName())){
											srcNodeId = n.getId();
										}
									}
								}
							
								//for now ID is not setted
								try{
									//post - create the relationship
									neo_rrel = target.path("data/node/"+srcNodeId+"/relationships").request().post(Entity.entity(neo_rel, MediaType.APPLICATION_XML), Relationship.class);
								}catch(ProcessingException pe){
									System.err.println("unable to upload a nffg conenction in the NEO4J database");
									throw new ServiceException(pe);
								}catch(WebApplicationException wae){
									System.err.println("server returned error while trying to upload a nffg connection");
									throw new ServiceException(wae);
								}catch(Exception e){
									System.err.println("unexpected exception occurred while trying to upload a nffg connection");
									throw new ServiceException(e);
								}
								//check the post response
								if(neo_rrel==null){
									System.out.println("failure in posting a nffg connection. no response from the server");
									throw new ServiceException();
								}
								else{		
									System.out.println("nffg connection (relationship) between "+srcNodeId+" and "+neo_rel.getDstNode()+" added successfully to the NEO4J db: "+neo_rrel.getId());
									//add the connection to something????
									//the post response carries in the body the created link
								}
							}
						}
					}
				}
			}
		}	
	}
	
	/**
	 * Returns a set of objects that can be used for reading information about NF-FG nodes and
	 * for getting reachable hosts.
	 * @param nffgName	the name of the NF-FG for which nodes have to be returned
	 * @return a set of ExtendedNodeReader objects
	 * @throws UnknownNameException if the name of the NF-FG is unknown or null
	 * @throws NoGraphException if no graph has been loaded for the requested NF-FG
	 * @throws ServiceException	if any other error occurs when trying to create the ExtendedNodeReader objects.
	 * */
	@Override
	public Set<ExtendedNodeReader> getExtendedNodes(String nffgName)
			throws UnknownNameException, NoGraphException, ServiceException {
		//@throws UnknownNameException if the name of the NF-FG is unknown or null
		if(nffgName == null){
			throw new UnknownNameException("nffg name null");
		}
		NffgReader nffg = nfv.getNffg(nffgName);
		if(nffg==null){
			throw new UnknownNameException("unable to find the searched nffg");
		}
		
		//@throws NoGraphException if no graph has been loaded for the requested NF-FG
		if(this.nffgName==null || !this.nffgName.equals(nffgName)){
			throw new NoGraphException("no graph has been loaded for the requested NF-FG");
		}
	
		//per ogni nodo in added_nodes, crea un extended node reader
		Set<ExtendedNodeReader> enr_list = new HashSet<ExtendedNodeReader>();
		ExtendedNodeReader enr;
		NodeReader nr;
		String node_name = null;
		List<Property> nodeProp_list;
		List<String> nodeLab_list;
		for(Node node : added_graphNodes){ //NB: we have to consider ONLY the ones that are Nodes not Hosts!
			nodeLab_list = node.getLabels().getLabel();
			for(String lab: nodeLab_list){
				if(lab.equals("Node") || lab.equals("node")){
					//System.out.println("------------------------------------------------------------------going to create a new ENR for node"+node.getId());
					//from the nfv retrieve the correspondent node reader
					//to do it, retrieve the node name from the node properties
					nodeProp_list = node.getProperties().getProperty();
					for(Property p : nodeProp_list){
						if(p.getName().equals("name")){
							node_name = p.getValue();
						}
					}
					//then search for the node in the nffg
					nr = nffg.getNode(node_name);
					
					if(nr == null){
						System.out.println("!!!!!!!!!!!!!!!!!!!!!!!NODE WITH NAME: "+node_name+" NOT FOUND");
						throw new ServiceException("searched node not found in the current nffg. unable to create the corresponding extended node reader ");
					}
					//System.out.println("node name: "+nr.getName());
					enr = new it.polito.dp2.NFV.sol2.ExtendedNodeReader(target, nr, node.getId(), nfv);
					enr_list.add(enr);
				}
			}
			
		}
		
		return enr_list;
	}

	
	/**
	 * Checks if the graph has already been successfully loaded.
	 * This is a local operation that can never fail.
	 * @return	true if the graph has already been successfully loaded, false otherwise.
	 * @throws UnknownNameException if the name of the NF-FG is unknown or null
	 */
	@Override
	public boolean isLoaded(String nffgName) throws UnknownNameException {
		if(nffgName==null){
			throw new UnknownNameException("nffgName is null");
		}
		else if(this.nfv.getNffg(nffgName)==null){
			throw new UnknownNameException("nffgName not valid");
		}
		else if(this.nffgName == null){
			return false;
		}
		//Checks if the graph has already been successfully loaded.		
		else if (this.nffgName.equals(nffgName)){
			return true;
		}
		else {return false;
		}
	}
}


