package com.study.springboot.config;

import com.study.springboot.service.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity //웹보안 활성화를위한 annotation
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    final private SecurityService securityService;
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests() // 요청에 대한 보안설정을 시작
                .antMatchers("/**").permitAll()
                .antMatchers("/user/join").permitAll()
                .antMatchers("/user/joinAction").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
       .and()
                .formLogin() //로그인 인증에 대한 설정을 시작
                .loginPage("/user/login") //로그인 페이지를 /loginForm URL로 하겠다.
                .loginProcessingUrl("/user/loginAction") //로그인 액션 URI를 지정한다.
                .successHandler( (request,response,authentication) -> {
                    System.out.println("로그인 성공했습니다.");
                    response.sendRedirect("/");
                })
                .failureUrl("/user/login?error")
                .permitAll()
       .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/user/logoutAction"))
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .logoutSuccessUrl("/");
        ; //루트경로 아래 모든 요청을 허가한다

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(securityService).passwordEncoder(passwordEncoder());
    }
}