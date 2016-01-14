package sniffing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class MyConfiguration {
    @Bean
    public String postIpAddressToDigitalOcean() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Thread to start sniffing is created");
                try {
                    Thread.sleep(30*1000);
                    System.out.println("Half the waiting is done");
                    Thread.sleep(30*1000);
                    RestTemplate template = new RestTemplate();
                    List<String> ips=NetworkInterface.getByName("wlan0").getInterfaceAddresses().stream().map(address -> address.getAddress().getHostAddress()).collect(Collectors.toList());
                    template.postForLocation("http://45.55.144.129:8080/ip",ips);
                    template.getForObject("http://localhost:8080/start",String.class);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        return "ipIsSent";
    }
}
