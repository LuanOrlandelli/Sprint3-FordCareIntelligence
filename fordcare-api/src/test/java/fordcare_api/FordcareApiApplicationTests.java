package fordcare_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"jwt.secret=fordcare-application-test-secret-key-2026-123456789"
})
class FordcareApiApplicationTests {

	@Test
	void contextLoads() {
	}
}
