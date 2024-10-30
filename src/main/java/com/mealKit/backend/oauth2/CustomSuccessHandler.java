package com.mealKit.backend.oauth2;

import com.mealKit.backend.dto.CustomOAuth2User;
import com.mealKit.backend.jwt.JwtUtil;
import com.mealKit.backend.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        String pid = customUserDetails.getPid();
        String pt = customUserDetails.getProviderType();
        System.out.println("pid : " + pid);
        System.out.println("providerType : " + pt);

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        System.out.println("role : " + role);

        String token = jwtUtil.createJwt(pid, pt, role, 1000*60*3L);
        response.addCookie(createCookie("Authorization", token));

        if (role.equals("ROLE_GUEST")){
            String email = customUserDetails.getEmail();
            response.sendRedirect("http://localhost:3000/register?email="+ email); // 회원가입 폼으로
        }else{
            response.sendRedirect("http://localhost:3000/");
        }
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key,value);
        cookie.setMaxAge(60*60*60);

        //cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
}