package com.iot.AdminPortal.Security;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import de.codecentric.boot.admin.server.config.AdminServerProperties;

@Configuration(proxyBeanMethods = false)
public class SecuritySecureConfig extends WebSecurityConfigurerAdapter {
	
	@Value("${spring.security.user.name}")
	private String userName;

	@Value("${spring.security.user.password}")
	private String password;

	private final AdminServerProperties adminServer;

	public SecuritySecureConfig(AdminServerProperties adminServer) {
		this.adminServer = adminServer;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
		successHandler.setTargetUrlParameter("redirectTo");
		successHandler.setDefaultTargetUrl(this.adminServer.path("/"));

		http.authorizeRequests((authorizeRequests) -> authorizeRequests.antMatchers(this.adminServer.path("/assets/**"))
				.permitAll().antMatchers(this.adminServer.path("/login")).permitAll().anyRequest().authenticated())
				.formLogin((formLogin) -> formLogin.loginPage(this.adminServer.path("/login"))
						.successHandler(successHandler).and())
				.logout((logout) -> logout.logoutUrl(this.adminServer.path("/logout")))
				.httpBasic(Customizer.withDefaults())
				.csrf((csrf) -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
						.ignoringRequestMatchers(
								new AntPathRequestMatcher(this.adminServer.path("/instances"),
										HttpMethod.POST.toString()),
								new AntPathRequestMatcher(this.adminServer.path("/instances/*"),
										HttpMethod.DELETE.toString()),
								new AntPathRequestMatcher(this.adminServer.path("/actuator/**"))))
				.rememberMe((rememberMe) -> rememberMe.key(UUID.randomUUID().toString()).tokenValiditySeconds(1209600));
	}

	// Required to provide UserDetailsService for "remember functionality"
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication().withUser(userName).password("{noop}"+password).roles("USER");
	}
}
