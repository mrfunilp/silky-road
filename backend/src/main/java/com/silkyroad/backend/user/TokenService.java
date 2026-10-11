package com.silkyroad.backend.user;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final long ttlSeconds;

    public TokenService(JwtEncoder jwtEncoder, @Value("${silkyroad.jwt.ttl-seconds}") long ttlSeconds) {
        this.jwtEncoder = jwtEncoder;
        this.ttlSeconds = ttlSeconds;
    }

    public IssuedToken issue(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("silkyroad")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(ttlSeconds))
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String value = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedToken(value, ttlSeconds);
    }

    public record IssuedToken(String token, long expiresIn) {}
}
