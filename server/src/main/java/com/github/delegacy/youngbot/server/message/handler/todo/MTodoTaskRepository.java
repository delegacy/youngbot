package com.github.delegacy.youngbot.server.message.handler.todo;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.delegacy.youngbot.server.platform.Platform;

interface MTodoTaskRepository extends JpaRepository<MTodoTask, Long> {
    List<MTodoTask> findByPlatformAndChannelIdAndFile(Platform platform, String channelId, String file);

    List<MTodoTask> findByPlatformAndChannelIdAndFileIn(Platform platform, String channelId,
                                                        Collection<String> files);
}
