package com.example.user.query;

import java.util.UUID;
import org.jilt.Builder;
import org.jilt.BuilderStyle;

@Builder(style = BuilderStyle.STAGED)
public record UserView(UUID userId, String email, String password) {

}
