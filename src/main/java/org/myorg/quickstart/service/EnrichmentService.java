package org.myorg.quickstart.service;

import java.util.Optional;

import org.myorg.quickstart.domain.CompositeSiniestro;
import org.myorg.quickstart.domain.Siniestro;
import org.myorg.quickstart.domain.SiniestroRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class EnrichmentService {

    private static final Logger logger = LoggerFactory.getLogger(EnrichmentService.class);

    private final SiniestroRepository repository;
    private final ObjectMapper objectMapper;

    public EnrichmentService(SiniestroRepository repository) {
        this.repository = repository;
        this.objectMapper = new ObjectMapper();
    }

    public Optional<String> enrich(int siniestroId) {
        try {
            Optional<Siniestro> siniestroOpt = repository.findSiniestroById(siniestroId);

            if (siniestroOpt.isPresent()) {
                Siniestro siniestro = siniestroOpt.get();
                CompositeSiniestro composite = mapToComposite(siniestro);

                // Fetch related data
                repository.findClienteById(siniestro.getClienteId()).ifPresent(composite::setCliente);
                repository.findExpedienteById(siniestro.getExpedienteId())
                        .ifPresent(exp -> composite.setExpedientes(java.util.Collections.singletonList(exp)));
                composite.setServicios(repository.findServiciosBySiniestroId(siniestroId));

                return Optional.of(objectMapper.writeValueAsString(composite));
            } else {
                logger.warn("Siniestro ID {} not found.", siniestroId);
            }
        } catch (Exception e) {
            logger.error("Failed to enrich Siniestro ID {}", siniestroId, e);
            e.getCause();
        }
        return Optional.empty();
    }

    // Method overload to handle business rule of which IDs to process
    public Optional<String> processEvent(String table, int id, int siniestroIdIfApplicable) {
        int targetId = -1;
        switch (table) {
            case "siniestros":
                targetId = id;
                break;
            case "servicios":
                targetId = siniestroIdIfApplicable;
                break;
            // TODO: Add logic for 'clientes' updates here if needed by fetching related
            // siniestros
            default:
                return Optional.empty();
        }

        if (targetId != -1) {
            return enrich(targetId);
        }
        return Optional.empty();
    }

    private CompositeSiniestro mapToComposite(Siniestro s) {
        CompositeSiniestro c = new CompositeSiniestro();
        c.setSiniestroId(s.getId());
        c.setDescripcion(s.getDescripcion());
        c.setEstado(s.getEstado());
        c.setUltimaActualizacion(s.getUltimaActualizacion());
        return c;
    }
}
