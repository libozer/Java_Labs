package com.postcode.component;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class CrudLogger {
    private final Logger logger = LoggerFactory.getLogger(CrudLogger.class);

    @Pointcut("execution(* com.postcode.service.*.create*(..))")
    public void create() {}

    @Pointcut("execution(* com.postcode.service.*.update*(..))")
    public void update() {}

    @Pointcut("execution(* com.postcode.service.*.delete*(..))")
    public void delete() {}

    @AfterReturning(pointcut = "create()", returning = "result")
    public void logCreate(Object result) {
        logger.info("New entity created: {}", result);
    }

    @AfterReturning(pointcut = "update()", returning = "result")
    public void logUpdate(Object result) {
        logger.info("Entity updated: {}", result);
    }

    @AfterReturning(pointcut = "delete()", returning = "result")
    public void logDelete(Object result) {
        logger.info("Removed entity: {}", result);
    }
}