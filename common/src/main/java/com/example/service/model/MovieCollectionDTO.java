package com.example.service.model;

import java.io.Serializable;
import java.util.List;

public record MovieCollectionDTO(List<Movie> movies) implements Serializable {}
