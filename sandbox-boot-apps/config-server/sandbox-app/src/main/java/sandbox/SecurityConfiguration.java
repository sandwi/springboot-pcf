package sandbox;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/**"
    };

    private static final String[] ACTUATOR_ENDPOINTS = {
            "/refresh",
            "/bus-refresh"
    };

    @Override
    protected void configure(HttpSecurity security) throws Exception {
        security.httpBasic().disable()
                .authorizeRequests()
                .antMatchers(PUBLIC_ENDPOINTS).permitAll();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(ACTUATOR_ENDPOINTS);
    }

}