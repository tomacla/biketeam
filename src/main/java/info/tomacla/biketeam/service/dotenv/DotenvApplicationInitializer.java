package info.tomacla.biketeam.service.dotenv;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class DotenvApplicationInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  @Override
  public void initialize(final ConfigurableApplicationContext applicationContext) {
    DotenvPropertySource.addToEnvironment(applicationContext.getEnvironment());
  }
}
