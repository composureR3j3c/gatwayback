package com.gateway.apigateway.model;

import jakarta.persistence.*;

@Entity
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;        // e.g. Payment API
    private String path;        // e.g. /payment
    private String upstream;    // e.g. http://localhost:9002

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPath() { return path; }
    public String getUpstream() { return upstream; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPath(String path) { this.path = path; }
    public void setUpstream(String upstream) { this.upstream = upstream; }
}
