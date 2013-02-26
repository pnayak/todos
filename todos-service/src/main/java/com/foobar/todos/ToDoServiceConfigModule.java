package com.foobar.todos;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *.
 * @author Prashant Nayak (pnayak)
 *.
 *         Provide any Spring @Bean configuration as needed
 */
@Configuration
public class ToDoServiceConfigModule {


  @Bean
  protected SetupService getSetupService(){
    return new SetupService();
  }
}