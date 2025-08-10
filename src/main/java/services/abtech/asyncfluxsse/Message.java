package services.abtech.asyncfluxsse;

import java.util.UUID;

public record Message(UUID uuid, String body) {}
