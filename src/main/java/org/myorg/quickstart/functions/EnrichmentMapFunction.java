package org.myorg.quickstart.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.myorg.quickstart.infra.JdbcSiniestroRepository;
import org.myorg.quickstart.service.EnrichmentService;

public class EnrichmentMapFunction extends RichMapFunction<String, String> {

    private transient EnrichmentService enrichmentService;
    private transient JdbcSiniestroRepository repository;
    private transient ObjectMapper mapper;

    @Override
    public void open(Configuration parameters) throws Exception {
        repository = new JdbcSiniestroRepository();
        repository.open();
        enrichmentService = new EnrichmentService(repository);
        mapper = new ObjectMapper();
    }

    @Override
    public void close() throws Exception {
        if (repository != null) {
            repository.close();
        }
    }

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(EnrichmentMapFunction.class);

    @Override
    public String map(String value) throws Exception {
        JsonNode root = mapper.readTree(value);
        JsonNode data = root.get("after");
        if (data == null) {
            LOG.debug("Ignored delete or null data for event: {}", value);
            return null; // Ignore deletes
        }

        JsonNode source = root.get("source");
        String sourceTable = source.get("table").asText();
        LOG.info("Processing event from table: {}", sourceTable);

        int id = -1;
        int siniestroId = -1;

        if (data.has("id")) {
            id = data.get("id").asInt();
        }
        if (data.has("siniestro_id")) {
            siniestroId = data.get("siniestro_id").asInt();
        }

        LOG.info("Event IDs - id: {}, siniestro_id: {}", id, siniestroId);
        return enrichmentService.processEvent(sourceTable, id, siniestroId).orElse(null);
    }
}
