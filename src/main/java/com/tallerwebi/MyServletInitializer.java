package com.tallerwebi;

import com.tallerwebi.config.DatabaseInitializationConfig;
import com.tallerwebi.config.HibernateConfig;
import com.tallerwebi.config.SpringSecurityConfig;
import com.tallerwebi.config.SpringWebConfig;
import javax.servlet.Filter;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class MyServletInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

  // services and data sources
  @Override
  protected Class<?>[] getRootConfigClasses() {
    return new Class[0];
  }

  // controller, view resolver, handler mapping
  @Override
  protected Class<?>[] getServletConfigClasses() {
    return new Class[] {
      SpringWebConfig.class,
      HibernateConfig.class,
      DatabaseInitializationConfig.class,
      SpringSecurityConfig.class,
    };
  }

  @Override
  protected String[] getServletMappings() {
    return new String[] { "/" };
  }

  @Override
  protected Filter[] getServletFilters() {
    CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
    encodingFilter.setEncoding("UTF-8");
    encodingFilter.setForceEncoding(true);
    return new Filter[] { encodingFilter };
  }
}
