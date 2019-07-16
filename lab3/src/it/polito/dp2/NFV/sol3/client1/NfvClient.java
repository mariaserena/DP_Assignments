package it.polito.dp2.NFV.sol3.client1;


import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import it.polito.dp2.NFV.lab3.AllocationException;
import it.polito.dp2.NFV.lab3.DeployedNffg;
import it.polito.dp2.NFV.lab3.NffgDescriptor;
import it.polito.dp2.NFV.lab3.ServiceException;
import it.polito.dp2.NFV.lab3.UnknownEntityException;


public class NfvClient implements it.polito.dp2.NFV.lab3.NfvClient {
	private WebTarget target;
	private MyConverter converter;
	private final static String nffgsPath = "/resources/nffgs";

	
	
	public NfvClient(WebTarget target, MyConverter converter) {
		this.target = target;
		this.converter = converter;
	}

	/**
	 * Deploys a new NF-FG with the features described by an NffgDescriptor 
	 * and returns an object that can be used to interact with the deployed NF-FG.
	 * @param nffg	an object that describes the NF-FG to be deployed
	 * @throws AllocationException if the NF-FG could not be deployed because allocation was not possible.
	 * @throws ServiceException	if any other error occurred when trying to deploy the NF-FG.
	 * returns an object that implements the DeployedNffg interface and that allows interaction with the deployed NF-FG
	 */
	@Override
	public DeployedNffg deployNffg(NffgDescriptor nffg) throws AllocationException, ServiceException {
		// create a NffgType from the NffgDescriptor
		NffgType nffg_t = converter.buildNffgType(nffg);
		
		// perform a postNffg to the NfvDeployer service
		Response response;
		try{
			response = target.path(nffgsPath).request().post(Entity.entity(nffg_t, MediaType.APPLICATION_XML));
		}
		catch (Exception e){
			throw new ServiceException("Error while trying to deploy an nffg " + e.getLocalizedMessage());
		}
		if(response.getStatus() == 403){
			throw new AllocationException("the nffg cannot be allocated");
		}

		if(response == null || response.getStatus() != 201){
			String err = "no response";
			if(response != null){
				err = response.readEntity(String.class);
			}
			throw new ServiceException("Error while trying to deploy an nffg " + err);
		}
		DeployedNffg nffg_deployed = new it.polito.dp2.NFV.sol3.client1.DeployedNffg(response.readEntity(NffgType.class), converter, target);
		
		return nffg_deployed;
	}

	/**
	 * Looks for an already deployed NF-FG by name and 
	 * returns an object that can be used to interact with it
	 * @param	name the name of the NF-FG we are looking for
	 * @return	an object that implements the DeployedNffg interface 
	 * and that allows interaction with the deployed NF-FG
	 * @throws UnknownEntityException	if the name passed as argument 
	 * does not correspond to a deployed NF-FG
	 * @throws ServiceException if any other error occurred when trying to access the service
	 */
	@Override
	public DeployedNffg getDeployedNffg(String name) throws UnknownEntityException, ServiceException {
		Response response;
		try{
			response = target.path(nffgsPath+"/"+name).request().get();
		}catch (Exception e){
			throw new ServiceException("Error while trying to deploy an nffg " + e.getLocalizedMessage());
		}
		if(response.getStatus() == 404){
			throw new UnknownEntityException("the searched nffg is not deployed");
		}
		if(response == null || response.getStatus() != 200){
			String err = "no response";
			if(response != null){
				err = response.readEntity(String.class);					
			}
			throw new ServiceException("Error while trying to deploy an nffg " + err);
		}
		DeployedNffg nffg_deployed = new it.polito.dp2.NFV.sol3.client1.DeployedNffg(response.readEntity(NffgType.class), converter, target);
		return nffg_deployed;
	}

}
