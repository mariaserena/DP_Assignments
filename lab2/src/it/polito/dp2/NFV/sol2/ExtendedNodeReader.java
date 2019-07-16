package it.polito.dp2.NFV.sol2;

import java.util.HashSet;
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
import it.polito.dp2.NFV.VNFTypeReader;
import it.polito.dp2.NFV.lab2.NoGraphException;
import it.polito.dp2.NFV.lab2.ServiceException;

public class ExtendedNodeReader implements it.polito.dp2.NFV.lab2.ExtendedNodeReader {

	//it has inside the NodeReader and 
	private NodeReader node;
	private String id;
	private WebTarget target;
	private NfvReader nfv;
	
	public ExtendedNodeReader(WebTarget target,NodeReader node, String id, NfvReader nfv){
		this.node=node;
		this.id=id;
		this.target = target;
		this.nfv = nfv;
	}
	
	/*@Override
	public Set<HostReader> getReachableHosts() throws NoGraphException, ServiceException {
		/**
		 * Gets the interfaces for reading information about the hosts that are reachable from this node
		 * @return	a set of objects for reading information about the hosts that are reachable from this host
		 * @throws NoGraphException	if no graph is currently loaded
		 * @throws ServiceException	if any other error occurs when trying to perform the operation
		 */ 
	/*	Relationships rels;
		Property host_prop;
		String host_name = null;
		HostReader h;
		Set<HostReader> host_set = new HashSet<HostReader>();
		//check if there is a graph loaded - if not, NoGraphException
		
		System.out.println("------------------------------------------------------------------------------------TRYING TO retrieve hostreader from node: "+node.getName());
		
		
		//retrieve all the OUT relationship this node is involved in 
		try{
			System.out.println(target.path("data/node/"+this.id+"/relationships/out"));
			rels = target.path("data/node/"+this.id+"/relationships/out").request().accept(MediaType.APPLICATION_XML).get(Relationships.class);
		}catch(WebApplicationException wae){
			System.err.println("server returned error while trying to retrieve the relationship list from the db");
			throw new ServiceException(wae);
		}catch(Exception e){
			System.err.println("unexpected exception occurred while trying to upload a nffg node");
			throw new ServiceException(e);
		}
		//check the obj returned by the get
		if(rels==null){
			System.out.println("failure in getting the relationship list. no response from the server");
			throw new ServiceException();
		}
		
		
		//from each rel in rels retrieve the dstNode and search the name in the added_graphnodes
		List<Relationships.Relationship> rel_list = rels.getRelationship();
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<founded "+rel_list.size()+" OUT relationships (linka nd allocated on)");
		for(Relationships.Relationship rel : rel_list){
			String host_id = rel.getDstNode(); //this gives the host id, so we have to search for the host name
			if(host_id!=null && rel.getType().equals("AllocatedOn")){
				//search for it in the graph -> get property of node by name
				try{
					host_prop = target.path("data/node/"+host_id+"/properties/name").request().get(Property.class);
				}catch(ProcessingException pe){
					System.err.println("unable to retrieve a host property from the NEO4J database");
					throw new ServiceException(pe);
				}catch(WebApplicationException wae){
					System.err.println("server returned error while trying to retrieve a host property");
					throw new ServiceException(wae);
				}catch(Exception e){
					System.err.println("unexpected exception occurred while trying to retrieve a host property");
					throw new ServiceException(e);
				}
				
				if(host_prop == null){
					System.out.println("failure in retrieving a host property. no response from the server");
					throw new ServiceException();
				}
				host_name = host_prop.getValue();
				System.out.println("node "+node.getName()+" "+this.id+" allocated on "+host_name+" "+host_id);
				//then search the host_id in the nfv and retrieve the HostReader
				h = nfv.getHost(host_name);
				if(h!=null){
					
					host_set.add(h);
				}
			}
		}
		
		return host_set;
		
	}*/
	
	@Override
	public Set<HostReader> getReachableHosts() throws NoGraphException, ServiceException {
		/**
		 * Gets the interfaces for reading information about the hosts that are reachable from this node
		 * @return	a set of objects for reading information about the hosts that are reachable from this host
		 * @throws NoGraphException	if no graph is currently loaded
		 * @throws ServiceException	if any other error occurs when trying to perform the operation
		 */
		
		Set<HostReader> host_set = new HashSet<HostReader>();
		Set<NodeReader> visitedNodes = new HashSet<NodeReader>();
		host_set = visit(this.node, this.id, visitedNodes);
		return host_set;
		
	}
	
	private Set<HostReader> visit(NodeReader node_r, String id, Set<NodeReader> visitedNodes) throws ServiceException{
		Set<HostReader> host_set = new HashSet<HostReader>();
		Relationships rels;
		if(!visitedNodes.contains(node_r)){
			visitedNodes.add(node_r);
			HostReader host_r = node_r.getHost();
			//retrieve from the graph all the OUT relationships
			try{
				System.out.println(target.path("data/node/"+id+"/relationships/out"));
				rels = target.path("data/node/"+id+"/relationships/out").request().accept(MediaType.APPLICATION_XML).get(Relationships.class);
			}catch(WebApplicationException wae){
				System.err.println("server returned error while trying to retrieve the relationship list from the db");
				throw new ServiceException(wae);
			}catch(Exception e){
				System.err.println("unexpected exception occurred while trying to upload a nffg node");
				throw new ServiceException(e);
			}
			//check the obj returned by the get
			if(rels==null){
				System.out.println("failure in getting the relationship list. no response from the server");
				throw new ServiceException();
			}
			
			//else 
			Property prop;
			List<Relationships.Relationship> rel_list = rels.getRelationship();
			System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<founded "+rel_list.size()+" OUT relationships (linka nd allocated on)");
			for(Relationships.Relationship rel : rel_list){
				//if the rel is a "allocationOn" rel
				if(rel.getType().equals("AllocatedOn")){
					String host_id = rel.getDstNode(); //retrieve the dest node ID, so the id of the host this node is allocated on
					//retrieve the corresp host name
					try{
						prop = target.path("data/node/"+host_id+"/properties/name").request().get(Property.class);
					}catch(ProcessingException pe){
						System.err.println("unable to retrieve a host property from the NEO4J database");
						throw new ServiceException(pe);
					}catch(WebApplicationException wae){
						System.err.println("server returned error while trying to retrieve a host property");
						throw new ServiceException(wae);
					}catch(Exception e){
						System.err.println("unexpected exception occurred while trying to retrieve a host property");
						throw new ServiceException(e);
					}
					
					if(prop == null){
						System.out.println("failure in retrieving a host property. no response from the server");
						throw new ServiceException();
					}
					String host_name = prop.getValue();

					HostReader h = nfv.getHost(host_name);
					if(h!=null){
						host_set.add(h);
					}
				}
				//if the rel is a "link" rel
				else if(rel.getType().equals("ForwardsTo")){
					//trova l'id del nodo destinazione 
					String node_id = rel.getDstNode();
					//da questo cerca il nome del nodo
					try{
						prop = target.path("data/node/"+node_id+"/properties/name").request().get(Property.class);
					}catch(ProcessingException pe){
						System.err.println("unable to retrieve a host property from the NEO4J database");
						throw new ServiceException(pe);
					}catch(WebApplicationException wae){
						System.err.println("server returned error while trying to retrieve a host property");
						throw new ServiceException(wae);
					}catch(Exception e){
						System.err.println("unexpected exception occurred while trying to retrieve a host property");
						throw new ServiceException(e);
					}
					if(prop == null){
						System.out.println("failure in retrieving a host property. no response from the server");
						throw new ServiceException();
					}
					String node_name = prop.getValue();
					
					NodeReader new_node_r = nfv.getNffg(node_r.getNffg().getName()).getNode(node_name);
					if(new_node_r!=null){
						host_set.addAll(visit(new_node_r, node_id,visitedNodes));
					}
				}
			}
		}
		return host_set;
	}
	


	@Override
	public VNFTypeReader getFuncType() {
		return node.getFuncType();
	}

	@Override
	public HostReader getHost() {
		return node.getHost();
	}

	@Override
	public Set<LinkReader> getLinks() {
		return node.getLinks();
	}

	@Override
	public NffgReader getNffg() {
		return node.getNffg();
	}

	@Override
	public String getName() {
		return node.getName();
	}
	
	public String getId(){
		return this.id;
	}

}
