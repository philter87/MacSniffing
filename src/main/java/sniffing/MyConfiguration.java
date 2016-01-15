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
                    Thread.sleep(15*1000);
                    System.out.println("Half the waiting is done");
                    Thread.sleep(15*1000);
                    RestTemplate template = new RestTemplate();
                    template.getForObject("http://localhost:8080/start",String.class);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        return "ipIsSent";
    }
}
