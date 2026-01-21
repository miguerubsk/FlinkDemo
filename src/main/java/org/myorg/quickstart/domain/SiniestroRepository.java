package org.myorg.quickstart.domain;

import java.util.List;
import java.util.Optional;

public interface SiniestroRepository extends AutoCloseable {
    Optional<Siniestro> findSiniestroById(int id);

    Optional<Cliente> findClienteById(int id);

    Optional<Expediente> findExpedienteById(int id);

    List<Servicio> findServiciosBySiniestroId(int id);

    void open() throws Exception;
}
