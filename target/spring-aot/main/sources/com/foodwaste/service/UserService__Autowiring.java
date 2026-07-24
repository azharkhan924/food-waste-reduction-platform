package com.foodwaste.service;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.AutowiredFieldValueResolver;
import org.springframework.beans.factory.support.RegisteredBean;

/**
 * Autowiring for {@link UserService}.
 */
@Generated
public class UserService__Autowiring {
  /**
   * Apply the autowiring.
   */
  public static UserService apply(RegisteredBean registeredBean, UserService instance) {
    instance.repo = AutowiredFieldValueResolver.forRequiredField("repo").resolve(registeredBean);
    return instance;
  }
}
