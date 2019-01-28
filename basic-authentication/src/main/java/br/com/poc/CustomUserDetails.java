package br.com.poc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private String password;
    private String username;
    @JsonSerialize(using = GrantedAuthorityJsonSeserializer.class)
    @JsonDeserialize(using = GrantedAuthorityJsonDeserializer.class)
    private Set<GrantedAuthority> authorities;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;

    public CustomUserDetails(String password, String username, Set<GrantedAuthority> authorities) {
        this.password = password;
        this.username = username;
        this.authorities = authorities;
    }
}
