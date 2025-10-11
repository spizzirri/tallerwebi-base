package com.tallerwebi;

import com.tallerwebi.config.DatabaseInitializationConfig;
import com.tallerwebi.config.HibernateConfig;
import com.tallerwebi.config.SpringWebConfig;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration;

public class MyServletInitializer
        extends AbstractAnnotationConfigDispatcherServletInitializer {

    // services and data sources
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[0];
    }

    // controller, view resolver, handler mapping
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{SpringWebConfig.class, HibernateConfig.class, DatabaseInitializationConfig.class};
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }


    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        registration.setMultipartConfig(getMultipartConfigElement());
    }

    private MultipartConfigElement getMultipartConfigElement() {
        MultipartConfigElement multipartConfigElement = new MultipartConfigElement( LOCATION, MAX_FILE_SIZE, MAX_REQUEST_SIZE, FILE_SIZE_THRESHOLD);
        return multipartConfigElement;
    }

    private static final String LOCATION            = "C:/temp/";   // Temporary location where files will be stored
    private static final long MAX_FILE_SIZE         = 5242880;      // 5MB : Max file size - Beyond that size spring will throw exception.
    private static final long MAX_REQUEST_SIZE      = 20971520;     // 20MB : Total request size containing Multi part.
    private static final int FILE_SIZE_THRESHOLD    = 0;            // Size threshold after which files will be written to disk


}
