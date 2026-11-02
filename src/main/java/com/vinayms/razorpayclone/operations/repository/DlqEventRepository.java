package com.vinayms.razorpayclone.operations.repository;

import com.vinayms.razorpayclone.operations.entity.DlqEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DlqEventRepository extends JpaRepository<DlqEvent, UUID> { }
