package com.tglanz.atlas.backend.generation.application.service;

import com.tglanz.atlas.backend.generation.application.port.in.ViewpointUseCase;
import com.tglanz.atlas.backend.generation.application.port.out.ViewpointRepository;
import com.tglanz.atlas.backend.generation.domain.Viewpoint;
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
  public Viewpoint makeDefault(UUID id) {
    Viewpoint target = get(id);
    if (target.isDefault()) {
      return target;
    }
    // Clear the old default first: at most one Viewpoint may be the default at any time.
    repository.findDefault().ifPresent(current -> repository.save(current.withDefault(false)));
    Viewpoint updated = target.withDefault(true);
    repository.save(updated);
    return updated;
  }

  @Override
  public void delete(UUID id) {
    get(id);
    repository.deleteById(id);
  }
}
