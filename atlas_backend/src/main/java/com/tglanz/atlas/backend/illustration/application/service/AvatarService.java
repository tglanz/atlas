package com.tglanz.atlas.backend.illustration.application.service;

import com.tglanz.atlas.backend.illustration.application.port.in.AvatarUseCase;
import com.tglanz.atlas.backend.illustration.application.port.out.AvatarRepository;
import com.tglanz.atlas.backend.illustration.domain.Avatar;
import com.tglanz.atlas.backend.shared.error.ConflictException;
import com.tglanz.atlas.backend.shared.error.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AvatarService implements AvatarUseCase {

  private final AvatarRepository repository;

  public AvatarService(AvatarRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Avatar> list() {
    return repository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public Avatar get(UUID id) {
    return repository.findById(id).orElseThrow(() -> new NotFoundException("Avatar not found"));
  }

  @Override
  public Avatar create(String name, String specification) {
    boolean first = repository.findDefault().isEmpty();
    Avatar created = Avatar.create(name, specification, first);
    repository.save(created);
    return created;
  }

  @Override
  public Avatar update(UUID id, String name, String specification) {
    Avatar updated = get(id).edit(name, specification);
    repository.save(updated);
    return updated;
  }

  @Override
  public Avatar makeDefault(UUID id) {
    Avatar target = get(id);
    if (target.isDefault()) {
      return target;
    }
    // Clear the old default first: at most one Avatar may be the default at any time.
    repository.findDefault().ifPresent(current -> repository.save(current.withDefault(false)));
    Avatar updated = target.withDefault(true);
    repository.save(updated);
    return updated;
  }

  @Override
  public void delete(UUID id) {
    if (get(id).isDefault()) {
      throw new ConflictException("The default avatar cannot be deleted");
    }
    repository.deleteById(id);
  }
}
