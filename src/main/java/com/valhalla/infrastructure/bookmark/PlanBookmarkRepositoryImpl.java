package com.valhalla.infrastructure.bookmark;

import com.valhalla.domain.bookmark.PlanBookmark;
import com.valhalla.domain.bookmark.PlanBookmarkRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PlanBookmarkRepositoryImpl implements PlanBookmarkRepository {

  private final JpaPlanBookmarkRepository jpaPlanBookmarkRepository;

  @Autowired
  public PlanBookmarkRepositoryImpl(JpaPlanBookmarkRepository jpaPlanBookmarkRepository) {
    this.jpaPlanBookmarkRepository = jpaPlanBookmarkRepository;
  }

  @Override
  public Optional<PlanBookmark> findByUserIdAndPlanId(UUID userId, UUID planId) {
    return jpaPlanBookmarkRepository.findByUserIdAndPlanId(userId, planId);
  }

  @Override
  public List<PlanBookmark> findByUserId(UUID userId) {
    return jpaPlanBookmarkRepository.findByUserIdOrderByCreatedAtDescIdDesc(userId);
  }

  @Override
  public Set<UUID> findPlanIdsByUserId(UUID userId) {
    return jpaPlanBookmarkRepository.findPlanIdsByUserId(userId);
  }

  @Override
  public PlanBookmark save(PlanBookmark bookmark) {
    return jpaPlanBookmarkRepository.save(bookmark);
  }

  @Override
  public void deleteByUserIdAndPlanId(UUID userId, UUID planId) {
    jpaPlanBookmarkRepository.deleteByUserIdAndPlanId(userId, planId);
  }

  @Override
  public void deleteAllByUserId(UUID userId) {
    jpaPlanBookmarkRepository.deleteAllByUserId(userId);
  }
}
