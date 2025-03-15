package com.ecommerce.project.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 1. While spring performs component scan, it checks for the classes annotated with @Configuration, @Component, @Service, @Repository and etc.
 * 2. Inside that class it checks for the method which are annotated with @Bean and create a bean in the Spring Container
 */
@Configuration
public class AppConfig {

    /**
     * 1. We need to create a bean inside @Configuration class if it is external library. Then only @AutoWired will work for it.
     * 2. Spring create a bean with name as method name.
     * 3. Bean name is required when two or more beans of same type are present in Application Context. We have to use bean name to inject the dependency using bean name using @Qualifier("beanName") annotation.
     */
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
