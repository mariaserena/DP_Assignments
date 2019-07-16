package it.polito.dp2.NFV.sol3.client2;

import it.polito.dp2.NFV.ConnectionPerformanceReader;


public class ConnectionPerformanceReaderClass implements ConnectionPerformanceReader {
	private PerformanceType conn;
	
	public ConnectionPerformanceReaderClass(PerformanceType conn){
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
