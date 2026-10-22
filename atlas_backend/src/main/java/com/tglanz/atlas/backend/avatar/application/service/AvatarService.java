package com.tglanz.atlas.backend.avatar.application.service;

import com.tglanz.atlas.backend.avatar.application.port.in.AvatarUseCase;
import com.tglanz.atlas.backend.avatar.application.port.out.AvatarRepository;
import com.tglanz.atlas.backend.avatar.domain.Avatar;
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
    Avatar created = Avatar.create(name, specification);
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
  public void delete(UUID id) {
    get(id);
    repository.deleteById(id);
  }
}
