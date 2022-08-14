package ai.ecma.order;

import com.google.gson.Gson;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableEurekaClient
@EnableFeignClients
@SpringBootApplication
@EnableJpaRepositories(basePackages = "ai.ecma.lib.repository")
@EntityScan(basePackages = "ai.ecma.lib.entity")
public class AppOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppOrderServiceApplication.class, args);
    }

    @Bean
    public Gson gson() {
        return new Gson();
    }
}
