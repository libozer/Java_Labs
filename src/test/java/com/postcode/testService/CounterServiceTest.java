package com.postcode.testService;

import com.postcode.service.CounterService;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

@ExtendWith(MockitoExtension.class)
class CounterServiceTest {
    private static final Logger LOG = LoggerFactory.getLogger(CounterServiceTest.class);

    @InjectMocks
    private CounterService counterService;

    @Test
    void testEnhanceCounter() {
        // Arrange
        AtomicInteger initialCounter = new AtomicInteger(0);
        CounterService.serviceCounter.setCounterRequest(initialCounter);

        // Act
        CounterService.enhanceCounter();
        int counterValue = CounterService.getCounter();

        // Assert
        Assertions.assertEquals(1, counterValue);
    }

    @Test
    void testGetCounter() {
        // Arrange
        AtomicInteger initialCounter = new AtomicInteger(5);
        CounterService.serviceCounter.setCounterRequest(initialCounter);

        // Act
        int counterValue = CounterService.getCounter();

        // Assert
        Assertions.assertEquals(5, counterValue);
    }
}