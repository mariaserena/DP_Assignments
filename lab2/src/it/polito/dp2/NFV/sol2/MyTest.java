package it.polito.dp2.NFV.sol2;

import it.polito.dp2.NFV.lab2.AlreadyLoadedException;
import it.polito.dp2.NFV.lab2.NoGraphException;
import it.polito.dp2.NFV.lab2.ReachabilityTesterException;
import it.polito.dp2.NFV.lab2.ServiceException;
import it.polito.dp2.NFV.lab2.UnknownNameException;
import it.polito.dp2.NFV.lab2.ReachabilityTester;

public class MyTest {

	public static void main(String[] args) throws ReachabilityTesterException, UnknownNameException, ServiceException, NoGraphException, AlreadyLoadedException{
		ReachabilityTester t = ReachabilityTesterFactory.newInstance().newReachabilityTester();
		t.loadGraph("Nffg0");

	}

}
