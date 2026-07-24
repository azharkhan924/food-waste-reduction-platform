package com.foodwaste.controller;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.AutowiredFieldValueResolver;
import org.springframework.beans.factory.support.RegisteredBean;

/**
 * Autowiring for {@link HomeController}.
 */
@Generated
public class HomeController__Autowiring {
  /**
   * Apply the autowiring.
   */
  public static HomeController apply(RegisteredBean registeredBean, HomeController instance) {
    instance.service = AutowiredFieldValueResolver.forRequiredField("service").resolve(registeredBean);
    return instance;
  }
}
