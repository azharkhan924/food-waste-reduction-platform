package com.foodwaste;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link FoodwasteApplication}.
 */
@Generated
public class FoodwasteApplication__BeanDefinitions {
  /**
   * Get the bean definition for 'foodwasteApplication'.
   */
  public static BeanDefinition getFoodwasteApplicationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(FoodwasteApplication.class);
    beanDefinition.setInstanceSupplier(FoodwasteApplication::new);
    return beanDefinition;
  }
}
