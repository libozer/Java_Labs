package com.postcode.service;

import com.postcode.controller.PostCodeController;
import com.postcode.model.ServiceCounter;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.concurrent.atomic.AtomicInteger;

@Data
public final class CounterService {
    private static final Logger LOG = LoggerFactory.getLogger(CounterService.class);

    private CounterService() {
    }

    public static final ServiceCounter serviceCounter = new ServiceCounter();

    private static AtomicInteger newEnhanceCounter = new AtomicInteger(0);

    public static synchronized void enhanceCounter() {
        if (serviceCounter.getCounterRequest() != null) {
            newEnhanceCounter = serviceCounter.getCounterRequest();
        }
        int number = newEnhanceCounter.incrementAndGet();
        serviceCounter.setCounterRequest(newEnhanceCounter);
        LOG.info("number of access to service is {}", number);
    }

    public static synchronized int getCounter() {
        AtomicInteger newCounter = serviceCounter.getCounterRequest();
        return newCounter.get();
    }
}