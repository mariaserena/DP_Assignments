package it.polito.dp2.NFV.sol1;

import it.polito.dp2.NFV.ConnectionPerformanceReader;
import it.polito.dp2.NFV.sol1.jaxb.ConnectionType;

public class ConnectionPerformanceReaderClass implements ConnectionPerformanceReader {
	private ConnectionType conn;
	
	public ConnectionPerformanceReaderClass(ConnectionType conn){
		this.conn= conn;
	}

	@Override
	public int getLatency() {
		if(conn==null){
			return 0;
		}
		return conn.getAvgLatency();
	}

	@Override
	public float getThroughput() {
		if(conn==null){
			return 0;
		}
		return conn.getAvgThrough();
	}

}
