package io.tetrate.portfolio.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private GrantedAuthoritiesConverter grantedAuthoritiesConverter;

    // @Autowired
    // private OAuth2ResourceServerProperties resourceServerProperties;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:https://keycloak.cloud.zwickey.net/auth/realms/tetrate}")
    private String issuer;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests().mvcMatchers(HttpMethod.GET, "/actuator/health").permitAll()
            .anyRequest().fullyAuthenticated()
            // /.and().oauth2Client()
            .and().oauth2ResourceServer()
            .jwt().jwtAuthenticationConverter(grantedAuthoritiesConverter).decoder(jwtDecoder());
    }

    @Bean
    protected JwtDecoder jwtDecoder() {
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(withIssuer);
    
        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders.fromOidcIssuerLocation(issuer);
        jwtDecoder.setJwtValidator(validator);
        return jwtDecoder;
      } 
}
