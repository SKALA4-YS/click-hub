package com.skala.clickhub.security.jwt;

import com.skala.clickhub.entity.User;
import com.skala.clickhub.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 토큰 subject(=User.id)로 실제 사용자를 조회해 SecurityContext에 세팅한다.
 * 소프트 삭제(deletedAt)된 계정은 토큰이 유효해도 인증되지 않은 것으로 취급한다.
 * 인증된 요청의 principal은 User 엔티티가 아니라 UUID(user id)로 둔다 —
 * 컨트롤러에서 @AuthenticationPrincipal UUID userId로 바로 받아 쓸 수 있고,
 * 매 요청마다 영속성 컨텍스트 밖으로 엔티티를 들고 다니는 LazyInitializationException 위험도 없앤다.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null && jwtUtils.isTokenValid(token)) {
            UUID userId = UUID.fromString(jwtUtils.getSubject(token));

            userRepository.findById(userId)
                    .filter(user -> user.getDeletedAt() == null)
                    .ifPresent(this::authenticate);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(User user) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user.getId(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
