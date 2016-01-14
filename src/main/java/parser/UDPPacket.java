package parser;

public class UDPPacket extends IPPacket{
    
    public UDPPacket(IPPacket packet){
	super(packet.timestamp);
	
	this.src_ip = packet.src_ip;
	this.dst_ip = packet.dst_ip;
    }

    public int src_port;
    public int dst_port;
    
    public byte[] data;
}
