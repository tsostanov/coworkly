package ru.ifmo.coworkly;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import ru.ifmo.coworkly.booking.BookingService;

@SpringBootTest
@ActiveProfiles("test")
class CoworklyApplicationTests {

	@MockBean
	private BookingService bookingService;

	@Test
	void contextLoads() {
	}

}
