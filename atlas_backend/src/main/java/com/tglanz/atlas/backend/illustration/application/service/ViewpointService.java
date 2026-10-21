package com.tglanz.atlas.backend.illustration.application.service;

import com.tglanz.atlas.backend.illustration.application.port.in.ViewpointUseCase;
import com.tglanz.atlas.backend.illustration.application.port.out.ViewpointRepository;
import com.tglanz.atlas.backend.illustration.domain.Viewpoint;
import com.tglanz.atlas.backend.shared.error.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ViewpointService implements ViewpointUseCase {

  private final ViewpointRepository repository;

  public ViewpointService(ViewpointRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Viewpoint> list() {
    return repository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public Viewpoint get(UUID id) {
    return repository.findById(id).orElseThrow(() -> new NotFoundException("Viewpoint not found"));
  }

  @Override
  public Viewpoint create(String name, String specification) {
    Viewpoint created = Viewpoint.create(name, specification);
    repository.save(created);
    return created;
  }

  @Override
  public Viewpoint update(UUID id, String name, String specification) {
    Viewpoint updated = get(id).edit(name, specification);
    repository.save(updated);
    return updated;
  }

  @Override
  public void delete(UUID id) {
    get(id);
    repository.deleteById(id);
  }
}
