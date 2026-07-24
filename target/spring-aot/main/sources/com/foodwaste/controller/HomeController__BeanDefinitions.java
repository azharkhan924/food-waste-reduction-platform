package com.foodwaste.controller;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.InstanceSupplier;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link HomeController}.
 */
@Generated
public class HomeController__BeanDefinitions {
  /**
   * Get the bean definition for 'homeController'.
   */
  public static BeanDefinition getHomeControllerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(HomeController.class);
    InstanceSupplier<HomeController> instanceSupplier = InstanceSupplier.using(HomeController::new);
    instanceSupplier = instanceSupplier.andThen(HomeController__Autowiring::apply);
    beanDefinition.setInstanceSupplier(instanceSupplier);
    return beanDefinition;
  }
}
