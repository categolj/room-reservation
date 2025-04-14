package com.example.config;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceLogAspect {

	@Around("@within(org.springframework.stereotype.Service)")
	public Object logServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();

		String className = joinPoint.getTarget().getClass().getName();
		String simpleClassName = joinPoint.getTarget().getClass().getSimpleName();

		Logger logger = LoggerFactory.getLogger(className);

		String methodName = method.getName();

		Object[] args = joinPoint.getArgs();

		logger.info("event=methodStart class={} method={} args={}", simpleClassName, methodName, Arrays.toString(args));

		long start = System.currentTimeMillis();
		Object result = null;
		try {
			result = joinPoint.proceed();
			return result;
		}
		catch (Exception e) {
			logger.error("event=exception class={} method={} exceptionType={} message={}", simpleClassName, methodName,
					e.getClass().getSimpleName(), e.getMessage(), e);
			throw e;
		}
		finally {
			long executionTime = System.currentTimeMillis() - start;

			logger.info("event=methodEnd class={} method={} result={} executionTime={}ms", simpleClassName, methodName,
					result, executionTime);
		}
	}

}