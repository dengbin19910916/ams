package io.xxx.ams;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.autoconfigure.exclude=org.springframework.boot.pulsar.autoconfigure.PulsarAutoConfiguration")
class AmsApplicationTests {

    @Test
    void contextLoads() {
    }

}
