package co.edu.escuelaing.techcup.notifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ServiceNotificationsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceNotificationsApplication.class, args);
	}

}
