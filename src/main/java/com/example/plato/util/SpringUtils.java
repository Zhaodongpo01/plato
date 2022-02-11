package com.example.plato.util;

import java.util.Collection;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/3 2:26 下午
 */
@Component
public class SpringUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtils.applicationContext = applicationContext;
    }

    public static <T> T getBean(String serviceName) {
        if (applicationContext.containsBean(serviceName)) {
            return (T) applicationContext.getBean(serviceName);
        }
        return null;
    }

    public static <T> T getBean(Class<T> tClass) {
        return applicationContext.getBean(tClass);
    }

    public static <T> Collection<T> getBeansOfType(Class<T> tClass) {
        Map<String, T> beanMap = applicationContext.getBeansOfType(tClass);
        return beanMap.values();
    }

    public static <B> void setBean(String beanName, Class<B> beanClass, B bean) {
        DefaultListableBeanFactory defaultListableBeanFactory =
                (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        defaultListableBeanFactory
                .registerBeanDefinition(beanName, BeanDefinitionBuilder.genericBeanDefinition(beanClass,
                        () -> bean).getBeanDefinition());
    }

    public static void initApplicationEvent(ApplicationContext appContext) {
        applicationContext = appContext;
    }
}
