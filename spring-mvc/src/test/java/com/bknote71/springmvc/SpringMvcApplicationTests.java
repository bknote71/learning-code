package com.bknote71.springmvc;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.MessageFormat;

@SpringBootTest
class SpringMvcApplicationTests {

	@Test
	void contextLoads() {
		String format = MessageFormat.format("abc {0}", new Object[]{123});
		System.out.println(format);
	}

}
