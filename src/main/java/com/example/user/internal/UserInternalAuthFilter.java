package com.example.user.internal;

import com.example.user.internal.InternalAuth.InvalidInternalAuthException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SECURITY.md C1. Every {@code /api/**} call must carry a valid
 * {@link InternalAuth#HEADER} (proving it came from the BFF / UI). For an
 * account-scoped path — {@code /api/accounts/{id}} and everything under it — the
 * acting user in that header must equal {@code {id}}, so one logged-in user
 * can't touch another's account. {@code POST /api/accounts} (register),
 * {@code POST /api/authenticate}, and the {@code /api/restrictions} catalogue
 * need the header but no acting-user match.
 *
 * <p>Disable with {@code internal-auth.enabled=false} (tests do). A plain servlet
 * filter — the security starter here only forces BCrypt.
 */
@Component
public class UserInternalAuthFilter extends OncePerRequestFilter {

    private static final Pattern ACCOUNT_SCOPED = Pattern.compile("^/api/accounts/(\\d+)(/.*)?$");

    private final boolean enabled;
    private final String secret;

    public UserInternalAuthFilter(@Value("${internal-auth.enabled:true}") boolean enabled,
                                  @Value("${internal-auth.secret:dev-internal-secret-change-me}") String secret) {
        this.enabled = enabled;
        this.secret = secret;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !enabled || !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        Long actingUser;
        try {
            actingUser = InternalAuth.verify(secret, request.getHeader(InternalAuth.HEADER),
                    request.getMethod(), uri);
        } catch (InvalidInternalAuthException e) {
            forbidden(response, "internal auth: " + e.getMessage());
            return;
        }

        Matcher m = ACCOUNT_SCOPED.matcher(uri);
        if (m.matches()) {
            long pathId = Long.parseLong(m.group(1));
            if (actingUser == null || actingUser != pathId) {
                forbidden(response, "acting user " + actingUser + " may not access account " + pathId);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private static void forbidden(HttpServletResponse response, String detail) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/problem+json");
        response.getWriter().write("{\"status\":403,\"title\":\"Forbidden\",\"detail\":\"" + detail + "\"}");
    }
}
