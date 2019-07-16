package it.polito.dp2.NFV.sol3.client1;

import java.util.List;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import it.polito.dp2.NFV.LinkReader;
import it.polito.dp2.NFV.NffgReader;
import it.polito.dp2.NFV.NodeReader;
import it.polito.dp2.NFV.VNFTypeReader;
import it.polito.dp2.NFV.lab3.AllocationException;
import it.polito.dp2.NFV.lab3.LinkAlreadyPresentException;
import it.polito.dp2.NFV.lab3.NoNodeException;
import it.polito.dp2.NFV.lab3.ServiceException;


public class DeployedNffg implements it.polito.dp2.NFV.lab3.DeployedNffg {
	private NffgType nffg_t;
	private MyConverter converter;
	private WebTarget target;
	private final static String nffgsPath = "/resources/nffgs";
	private final static String nodesPath = "/nodes";
	private final static String linksPath = "/links";
	private final static String hostsPath = "/resources/hosts";
	private final static String vnftsPath = "/resources/vnfts";
	
	public DeployedNffg(NffgType nffg_t, MyConverter converter, WebTarget target){
		this.nffg_t = nffg_t;
		this.converter = converter;
		this.target = target;
	}

	@Override
	/**
	 * Adds a new node to this already deployed NF-FG. The node is added without any link.
	 * A node name is chosen by the implementation
	 * @param type	the type of VNF the node to be added has to implement
	 * @param hostName	the name of the host requested for allocation or null if no specific host is requested
	 * @throws AllocationException if the node could not be added because allocation was not possible.
	 * @throws ServiceException	if any other error occurred when trying to deploy the NF-FG.
	 * returns	an interface for reading information about the added node.
	 */
	public NodeReader addNode(VNFTypeReader type, String hostName) throws AllocationException, ServiceException {
		NodeType node_t = converter.buildNodeType(type, hostName, nffg_t.getName());
		Response response;
		try{
			 response = target.path(nffgsPath+"/"+this.nffg_t.getName()+nodesPath).request().post(Entity.entity(node_t, MediaType.APPLICATION_XML));
			 
		}
		catch (Exception e){
			throw new ServiceException("Error while trying to add a node to a deployed nffg " + e.getLocalizedMessage());
		}
		if (response.getStatus()==403){
			throw new AllocationException("allocation of the node was not possible");
		}
		else if(response == null || response.getStatus() != 201){
			String err = "no response";
			if(response != null){
				err = response.readEntity(String.class);
			}
			throw new ServiceException("Error while trying to add a node to a deployed nffg " + err);
		}
		NodeType respNode; 
		try{
				respNode = (NodeType)response.readEntity(NodeType.class);
		}catch(ProcessingException e){
				throw new ServiceException("Error while trying to read the result " + e.getLocalizedMessage());
		} catch(IllegalStateException e){
				throw new ServiceException("Error while trying to read the result " + e.getLocalizedMessage());
		}
		List<NodeType> node_t_list = nffg_t.getNodeType();
		node_t_list.add(respNode);
		VnftsType vnfts;
		HostsType hosts;
		NffgsType nffgs;
		try{
			response = target.path(hostsPath).request().get();
		}catch(Exception e){
			throw new ServiceException("failure in the creation of the nffg reader - NfvDeployer (retrieving hosts)");
		}
		if(response.getStatus()!=200){
			throw new ServiceException("failure in the creation of the nffg reader - NfvDeployer response (retrieving hosts)");
		}
		try{
			hosts = (HostsType)response.readEntity(HostsType.class);
		}catch(ProcessingException e){
			throw new ServiceException("Error while trying to read the result " + e.getLocalizedMessage());
		} catch(IllegalStateException e){
			throw new ServiceException("Error while trying to read the result " + e.getLocalizedMessage());
		}
		
		try{
			response = target.path(vnftsPath).request().get();
		}catch(Exception e){
			throw new ServiceException("failure in the creation of the nffg reader - NfvDeployer (retrieving vnfts)");
		}
		if(response.getStatus()!=200){
			throw new ServiceException("failure in the creation of the nffg reader - NfvDeployer (retrieving vnfts) response");
		}
		try{
			vnfts = (VnftsType)response.readEntity(VnftsType.class);
		}catch(ProcessingException e){
			throw new ServiceException("Error while trying to read the result " + e.getLocalizedMessage());
		} catch(IllegalStateException e){
			throw new ServiceException("Error while trying to read the result " + e.getLocalizedMessage());
		}
		
		try{
			response = target.path(nffgsPath).request().get();
		}catch(Exception e){
			throw new ServiceException("failure in the creation of the nffg reader - NfvDeployer (retrieving nffgs)");
		}
		if(response.getStatus()!=200){
			throw new ServiceException("failure in the creation of the nffg reader - NfvDeployer (retrieving nffgs) response");
		}
		
		try{
			nffgs = (NffgsType)response.readEntity(NffgsType.class);
		}catch(ProcessingException e){
			throw new ServiceException("Error while trying to read the result " + e.getLocalizedMessage());
		} catch(IllegalStateException e){
			throw new ServiceException("Error while trying to read the result " + e.getLocalizedMessage());
		}	
		
		
		NodeReader node_r = new NodeReaderClass(respNode, vnfts, hosts,nffgs);
		return node_r;
	}

	@Override
	/**
	 * Adds a new link to this already deployed NF-FG without 
	 * specification of throughput and latency requirements.
	 * A link name is chosen by the implementation
	 * @param source	the source node of the link
	 * @param dest	the destination node of the link
	 * @param overwrite	true if the link information has to be overwritten 
	 * if the link was already present
	 * @throws NoNodeException if any of the nodes passed as arguments does not 
	 * exist in the deployed NF-FG.
	 * @throws LinkAlreadyPresentException	if a link connecting the specified 
	 * nodes was already present and overwrite is false.
	 * @throws ServiceException	if any other error occurred when trying to add the link.
	 * returns	an interface for reading information about the added link.
	 */
	public LinkReader addLink(NodeReader source, NodeReader dest, boolean overwrite)
			throws NoNodeException, LinkAlreadyPresentException, ServiceException {
		LinkType lt = converter.buildLinkType(source, dest);
		Response response;
		try{
			response = target.path(nffgsPath+"/"+nffg_t.getName()+nodesPath+"/"+source.getName()+linksPath).queryParam("overwrite", overwrite).request().post(Entity.entity(lt, MediaType.APPLICATION_XML));
		}catch (Exception e){
			throw new ServiceException("Error while trying to load a link . " + e.getLocalizedMessage());
		}
		if(response != null && response.getStatus() == 403){
			String err;
			err = response.readEntity(String.class);
			throw new LinkAlreadyPresentException(err);
		}
		if(response != null && response.getStatus() == 404){
			String err;
			err = response.readEntity(String.class);
			throw new NoNodeException(err);
		}
		//Any other error
		if(response == null || response.getStatus() != 201){
			String err = "no response";
			if(response != null){
				err = response.readEntity(String.class);
			}
			throw new ServiceException("Error while trying to load a link " + err);
		}
		LinkType respLink; 
		try{
				respLink = (LinkType)response.readEntity(LinkType.class);
		}catch(ProcessingException e){
				throw new ServiceException("Error while trying to read the result " + e.getLocalizedMessage());
		} catch(IllegalStateException e){
				throw new ServiceException("Error while trying to read the result " + e.getLocalizedMessage());
		}
			
		for(NodeType nt: this.nffg_t.getNodeType()){
			if(nt.getName().equals(respLink.getSourceNode())){
				nt.getLinkType().add(respLink);
			}
		}
		//retrieve the VNFTS and the HOSTS from the NvfReader
				VnftsType vnfts;
				HostsType hosts;
				NffgsType nffgs;
				try{
				//retrieve the hosts
					
					response = target.path(hostsPath).request().get();
				}catch(Exception e){
					throw new ServiceException("failure in the creation of the nffg reader - NfvDeployer (retrieving hosts)");
				}
				if(response.getStatus()!=200){
					throw new ServiceException("failure in the creation of the nffg reader - NfvDeployer response (retrieving hosts)");
				}
				
				//try to read what you got
				try{
					hosts = (HostsType)response.readEntity(HostsType.class);
				}catch(ProcessingException e){
					throw new ServiceException("Error while trying to read the result " + e.getLocalizedMessage());
				} catch(IllegalStateException e){
					throw new ServiceException("Error while trying to read the result " + e.getLocalizedMessage());
				}
				
				try{
					//retrieve the vnfts
					response = target.path(vnftsPath).request().get();
				}catch(Exception e){
					throw new ServiceException("failure in the creation of the nffg reader - NfvDeployer (retrieving vnfts)");
				}
				if(response.getStatus()!=200){
					throw new ServiceException("failure in the creation of the nffg reader - NfvDeployer (retrieving vnfts) response");
				}
				
				//try to read what you got
				try{
					vnfts = (VnftsType)response.readEntity(VnftsType.class);
				}catch(ProcessingException e){
					throw new ServiceException("Error while trying to read the result " + e.getLocalizedMessage());
				} catch(IllegalStateException e){
					throw new ServiceException("Error while trying to read the result " + e.getLocalizedMessage());
				}
				
				try{
					//retrieve the Nffgs
					response = target.path(nffgsPath).request().get();
				}catch(Exception e){
					throw new ServiceException("failure in the creation of the nffg reader - NfvDeployer (retrieving nffgs)");
				}
				if(response.getStatus()!=200){
					throw new ServiceException("failure in the creation of the nffg reader - NfvDeployer (retrieving nffgs) response");
				}
				
				//try to read what you got
				try{
					nffgs = (NffgsType)response.readEntity(NffgsType.class);
				}catch(ProcessingException e){
					throw new ServiceException("Error while trying to read the result " + e.getLocalizedMessage());
				} catch(IllegalStateException e){
					throw new ServiceException("Error while trying to read the result " + e.getLocalizedMessage());
				}
				
				
				LinkReader lr = new LinkReaderClass(respLink, nffgs, vnfts, hosts);
		return lr;
	}

	@Override
	/**
	 * Provides an NffgReader to read information about the deployed NF-FG.
	 * The reader must be up to date with the modifications made by the other 
	 * methods of this interface
	 * @return an object that provides information about the deployed NF-FG
	 * @throws ServiceException if any error occurs when trying to create the reader
	 */
	public NffgReader getReader() throws ServiceException {
		//retrieve the VNFTS and the HOSTS from the NvfReader
		Response response;
		VnftsType vnfts;
		HostsType hosts;
		NffgsType nffgs;
		try{
			//retrieve the hosts
			response = target.path(hostsPath).request().get();
		}catch(Exception e){
			throw new ServiceException("failure in the creation of the nffg reader - NfvDeployer (retrieving hosts)");
		}
		if(response.getStatus()!=200){
			throw new ServiceException("failure in the creation of the nffg reader - NfvDeployer response (retrieving hosts)");
		}
		
		//try to read what you got
		try{
			hosts = (HostsType)response.readEntity(HostsType.class);
		}catch(ProcessingException e){
			throw new ServiceException("Error while trying to read the result " + e.getLocalizedMessage());
		} catch(IllegalStateException e){
			throw new ServiceException("Error while trying to read the result " + e.getLocalizedMessage());
		}
		
		try{
			//retrieve the vnfts
			response = target.path(vnftsPath).request().get();
		}catch(Exception e){
			throw new ServiceException("failure in the creation of the nffg reader - NfvDeployer (retrieving vnfts)");
		}
		if(response.getStatus()!=200){
			throw new ServiceException("failure in the creation of the nffg reader - NfvDeployer (retrieving vnfts) response");
		}
		
		//try to read what you got
		try{
			vnfts = (VnftsType)response.readEntity(VnftsType.class);
		}catch(ProcessingException e){
			throw new ServiceException("Error while trying to read the result " + e.getLocalizedMessage());
		} catch(IllegalStateException e){
			throw new ServiceException("Error while trying to read the result " + e.getLocalizedMessage());
		}
		
		try{
			//retrieve the Nffgs
			response = target.path(nffgsPath).request().get();
		}catch(Exception e){
			throw new ServiceException("failure in the creation of the nffg reader - NfvDeployer (retrieving nffgs)");
		}
		if(response.getStatus()!=200){
			throw new ServiceException("failure in the creation of the nffg reader - NfvDeployer (retrieving nffgs) response");
		}
		
		//try to read what you got
		try{
			nffgs = (NffgsType)response.readEntity(NffgsType.class);
		}catch(ProcessingException e){
			throw new ServiceException("Error while trying to read the result " + e.getLocalizedMessage());
		} catch(IllegalStateException e){
			throw new ServiceException("Error while trying to read the result " + e.getLocalizedMessage());
		}
		
		//turns the NffgType into an NffgReader
		NffgReader nffg_r = new NffgReaderClass(this.nffg_t,nffgs, vnfts, hosts);
		
		return nffg_r;
	}

}
