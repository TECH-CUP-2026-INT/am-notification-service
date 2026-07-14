package co.edu.escuelaing.techcup.notifications;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class ServiceNotificationsApplicationTests {

	@Container
	@ServiceConnection
	static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:7");

	@Test
	void contextLoads() {
	}

}
