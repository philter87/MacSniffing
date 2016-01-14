package sniffing;

import org.pcap4j.core.*;
import org.pcap4j.packet.Packet;
import org.pcap4j.util.NifSelector;

import java.io.IOException;

public class pCap {

    private static final String PCAP_FILE = "logs/sniffing_00164_20151224173021.pcap";

    public static void main3(String[] args) throws PcapNativeException, IOException, NotOpenException {
        PcapNetworkInterface nif = new NifSelector().selectNetworkInterface();
        System.out.println(nif.getName());
        System.out.println(nif.getAddresses());
        System.out.println(nif.getLinkLayerAddresses().get(0).getAddress());

        PcapHandle handle = new PcapHandle.Builder("\\Device\\NPF_{8F3DC10C-16EB-4992-9BC1-90A9AA7DE48B}").rfmon(true).build();

        long start = System.currentTimeMillis();
        while(true) {
            Packet packet = handle.getNextPacket();
            long now = System.currentTimeMillis();
            System.out.println("Time elapsed: " + (now - start));
            start = now;

            //System.out.println(packet.getHeader().toString());
            //System.out.println(packet.getPayload());
        }


    }
}
