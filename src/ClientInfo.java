import java.net.InetAddress;


public class ClientInfo {
	
	private String name;
	private InetAddress address;
	private int port;
	
	public ClientInfo(String name,InetAddress address, int port){
		this.name = name;
		this.address = address;
		this.port = port;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public InetAddress getAddress(){
		return address;
	}
	
	public int getPort() {
		return port;
	}

}
