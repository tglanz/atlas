package com.tglanz.atlas.backend.movement.application.service;

import com.tglanz.atlas.backend.movement.application.port.in.MovementCommandUseCase;
import com.tglanz.atlas.backend.movement.application.port.in.MovementQueryUseCase;
import com.tglanz.atlas.backend.movement.application.port.out.MovementRepository;
import com.tglanz.atlas.backend.movement.domain.Illustration;
import com.tglanz.atlas.backend.movement.domain.Movement;
import com.tglanz.atlas.backend.movement.domain.MovementContent;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import com.tglanz.atlas.backend.shared.error.ConflictException;
import com.tglanz.atlas.backend.shared.error.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MovementService implements MovementQueryUseCase, MovementCommandUseCase {

  private final MovementRepository repository;

  public MovementService(MovementRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Movement> search(String text, String tag) {
    return repository.search(blankToEmpty(text), blankToEmpty(tag).toLowerCase());
  }

  @Override
  @Transactional(readOnly = true)
  public Movement get(MovementKey key) {
    return repository.find(key).orElseThrow(() -> new NotFoundException("No movement " + key));
  }

  @Override
  @Transactional(readOnly = true)
  public boolean exists(MovementKey key) {
    return repository.exists(key);
  }

  @Override
  public Movement create(MovementKey key, MovementContent content) {
    if (repository.exists(key)) {
      throw new ConflictException("Movement " + key + " already exists");
    }
    Movement movement = Movement.create(key, content);
    repository.save(movement);
    return movement;
  }

  @Override
  public Movement update(MovementKey key, MovementContent content) {
    Movement movement = get(key);
    movement.edit(content);
    repository.save(movement);
    return movement;
  }

  @Override
  public Movement replaceIllustrations(MovementKey key, List<Illustration> illustrations) {
    Movement movement = get(key);
    movement.replaceIllustrations(illustrations);
    repository.save(movement);
    return movement;
  }

  private static String blankToEmpty(String value) {
    return value == null ? "" : value.strip();
  }
}
