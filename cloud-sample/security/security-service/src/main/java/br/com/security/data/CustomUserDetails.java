package br.com.security.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;

@Data
@Document
@NoArgsConstructor
@AllArgsConstructor
public class CustomUserDetails{

    @Id
    private String id;
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
