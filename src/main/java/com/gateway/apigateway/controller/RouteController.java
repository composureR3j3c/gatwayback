package com.gateway.apigateway.controller;

import com.gateway.apigateway.model.Route;
import com.gateway.apigateway.repo.RouteRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/routes")
@CrossOrigin("*")  // allow frontend
public class RouteController {

    private final RouteRepository repo;

    public RouteController(RouteRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Route> list() {
        return repo.findAll();
    }

    @PostMapping
    public Route create(@RequestBody Route route) {
        return repo.save(route);
    }

    @PutMapping("/{id}")
    public Route update(@PathVariable Long id, @RequestBody Route data) {
        return repo.findById(id).map(r -> {
            r.setName(data.getName());
            r.setPath(data.getPath());
            r.setUpstream(data.getUpstream());
            return repo.save(r);
        }).orElseThrow();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}
