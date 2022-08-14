package com.aonufrei.gallerywebapp.security;

import com.aonufrei.gallerywebapp.security.data.DbUserDetailsService;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	private final PasswordEncoder passwordEncoder;

	private final DbUserDetailsService userDetailsService;

	public SecurityConfiguration(PasswordEncoder passwordEncoder, DbUserDetailsService userDetailsService) {
		this.passwordEncoder = passwordEncoder;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.csrf().disable()
				.authorizeRequests()
				.antMatchers("/", "/home").permitAll()
				.antMatchers("/logout", "/login-page").permitAll()
				.antMatchers("/register").permitAll()
				.anyRequest().permitAll()
				.and()
				.formLogin()
				.loginPage("/login-page")
				.failureUrl("/login-error")
				.permitAll()
				.and()
				.logout()
				.logoutSuccessUrl("/")
				.permitAll();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
	}

//	@Autowired
//	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//		auth.inMemoryAuthentication().withUser("user").password(passwordEncoder.encode("password")).roles("USER");
//		auth.inMemoryAuthentication().withUser("John").password(passwordEncoder.encode("password")).roles("USER");
//	}

}
