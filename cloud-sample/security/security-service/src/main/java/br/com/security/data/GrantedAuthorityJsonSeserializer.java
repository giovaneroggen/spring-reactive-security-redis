package br.com.security.data;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.security.core.GrantedAuthority;

import java.io.IOException;
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
