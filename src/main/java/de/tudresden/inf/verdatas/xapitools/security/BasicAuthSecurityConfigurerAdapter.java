package de.tudresden.inf.verdatas.xapitools.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Configuration for Web Security
 *
 * In this Class, Security is turned on for all Paths below and including root.
 * The mechanism is BasicAuth with its parameters (username and password) set
 * using the default properties 'spring.security.user.name', 'spring.security.user.password' and 'spring.security.user.role'.
 *
 * Also, CSRF is managed (i.e. turned on) here.
 */
@Configuration
@EnableWebSecurity
public class BasicAuthSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

    private final BasicAuthenticationEntryPoint entryPoint;

    public BasicAuthSecurityConfigurerAdapter(BasicAuthenticationEntryPoint entryPoint) {
        this.entryPoint = entryPoint;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/**", "!/h2-console/**").authenticated()
                .and()
                .httpBasic()
                .authenticationEntryPoint(entryPoint)
                .and()
                .csrf().csrfTokenRepository(new CookieCsrfTokenRepository());
        http.authorizeRequests().antMatchers("/h2-console/**").permitAll().and().headers().frameOptions().sameOrigin().and().csrf().disable();
    }
}
