package com.sai.slog.intelligence;

import com.google.common.base.Predicates;
import org.apache.log4j.Logger;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.scheduling.annotation.EnableAsync;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Created by saipkri on 07/09/16.
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.sai.slog.intelligence"})
@EnableSwagger2
@EnableAsync
@EnableNeo4jRepositories
public class SlogIntelligenceApp {

    private static final Logger LOGGER = Logger.getLogger(SlogIntelligenceApp.class);

    @Value("${esUrl}")
    private String esUrl;

    @Value("${neo4jDb:#{null}}")
    private String neo4jDb;

    @Value("${neo4jUser:#{null}}")
    private String neo4jUser;

    @Value("${neo4jPassword:#{null}}")
    private String neo4jPassword;

    private String neo4jDomainPkgs = "com.sai.slog.intelligence.domain";

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        return new PropertySourcesPlaceholderConfigurer();
    }


    @Bean
    public SessionFactory sessionFactory() {
        org.neo4j.ogm.config.Configuration config = new org.neo4j.ogm.config.Configuration();
        config.driverConfiguration()
                .setDriverClassName("org.neo4j.ogm.drivers.http.driver.HttpDriver")
                .setCredentials(neo4jUser, neo4jPassword)
                .setURI(neo4jDb);
        return new SessionFactory(config, neo4jDomainPkgs);
    }

    @Bean
    public Neo4jTransactionManager transactionManager() {
        return new Neo4jTransactionManager(sessionFactory());
    }

    /**
     * Swagger 2 docket bean configuration.
     *
     * @return swagger 2 Docket.
     */
    @Bean
    public Docket configApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("config")
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(Predicates.not(PathSelectors.regex("/error"))) // Exclude Spring error controllers
                .build();
    }


    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Slog-Intelligence REST API")
                .contact("sai@concordesearch.co.uk")
                .version("1.0")
                .build();
    }


    public static void main(String[] args) {
        SpringApplicationBuilder application = new SpringApplicationBuilder();
        application //
                .headless(true) //
                .addCommandLineProperties(true) //
                .sources(SlogIntelligenceApp.class) //
                .main(SlogIntelligenceApp.class) //
                .registerShutdownHook(true)
                .run(args);
        System.out.println(" ------------- " + application);
    }


}
