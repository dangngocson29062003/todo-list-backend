package com.example.weaver.dtos.events;


import java.util.UUID;

public record UserRegisteredEvent(UUID userId,String email) {}