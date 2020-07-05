package org.piotr.allegrotestapp.service;

import java.util.UUID;

public class UuidService {
    public UUID uuid;
    public UuidService() {
        UUID uuid = UUID.randomUUID();
        this.uuid = uuid;
    }

}
