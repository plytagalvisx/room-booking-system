package com.codewithmosh.store;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled // Here we disable this test because it is not a unit test. It is an integration test that requires the Spring context to load, which can be slow and unnecessary for simple unit testing.
@SpringBootTest
class StoreApplicationTests {

    @Test
    void contextLoads() {
    }

}
