package br.com.security.data;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class GrantedAuthorityJsonDeserializer extends JsonDeserializer {
    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException{
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        Set<String> set = mapper.readValue(p, Set.class);
        if(set != null){
            return set.stream()
                       .map(SimpleGrantedAuthority::new)
                       .collect(Collectors.toSet());
        }
        return Set.of();
    }
}
