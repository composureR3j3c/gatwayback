package com.gateway.apigateway.controller;

import com.gateway.apigateway.model.Plugin;
import com.gateway.apigateway.repo.PluginRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/plugins")
@CrossOrigin("*")
public class PluginController {

    private final PluginRepository repo;

    public PluginController(PluginRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Plugin> list() {
        return repo.findAll();
    }

    @PostMapping
    public Plugin create(@RequestBody Plugin plugin) {
        return repo.save(plugin);
    }

    @PutMapping("/{id}")
    public Plugin update(@PathVariable Long id, @RequestBody Plugin data) {
        return repo.findById(id).map(p -> {
            p.setLabel(data.getLabel());
            p.setName(data.getName());
            p.setEnabled(data.isEnabled());
            p.setConfig(data.getConfig());
            return repo.save(p);
        }).orElseThrow();
    }

    // Update *only* config object
    @PatchMapping("/{id}/config")
    public Plugin updateConfig(@PathVariable Long id, @RequestBody Map<String, Object> config) {
        return repo.findById(id).map(p -> {
            p.setConfig(config);
            return repo.save(p);
        }).orElseThrow();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}
