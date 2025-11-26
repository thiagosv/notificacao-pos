package com.notification.template.repository;

import com.notification.template.model.Channel;
import com.notification.template.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TemplateRepository extends JpaRepository<Template, UUID> {

    Optional<Template> findByClientIdAndChannelAndTemplateCodeAndActiveTrue(
        String clientId,
        Channel channel,
        String templateCode
    );

    List<Template> findByClientIdAndActiveTrue(String clientId);

    List<Template> findByClientIdAndChannelAndActiveTrue(String clientId, Channel channel);

    List<Template> findByClientIdAndChannelAndTemplateCodeOrderByVersionDesc(
        String clientId,
        Channel channel,
        String templateCode
    );
}

