package br.com.poc;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class GrantedAuthorityJsonSeserializer extends JsonSerializer<Set<GrantedAuthority>> {
    @Override
    public void serialize(Set<GrantedAuthority> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartArray();
        for (GrantedAuthority model : value) {
            gen.writeString(model.getAuthority());
        }
        gen.writeEndArray();
    }
}
