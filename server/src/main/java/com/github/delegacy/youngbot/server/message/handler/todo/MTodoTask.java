package com.github.delegacy.youngbot.server.message.handler.todo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.validation.constraints.NotNull;

import com.github.delegacy.youngbot.server.platform.Platform;

@Entity(name = "todo")
class MTodoTask {
    static final String TODO_FILE = "TODO";
    static final String DONE_FILE = "DONE";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private @NotNull Platform platform;

    private @NotNull String channelId;

    private @NotNull String file = TODO_FILE;

    @Lob
    private @NotNull String text;

    Long getId() {
        return id;
    }

    @SuppressWarnings("unused")
    MTodoTask setId(Long id) {
        this.id = id;
        return this;
    }

    Platform getPlatform() {
        return platform;
    }

    MTodoTask setPlatform(Platform platform) {
        this.platform = platform;
        return this;
    }

    String getChannelId() {
        return channelId;
    }

    MTodoTask setChannelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    String getFile() {
        return file;
    }

    MTodoTask setFile(String file) {
        this.file = file;
        return this;
    }

    String getText() {
        return text;
    }

    MTodoTask setText(String text) {
        this.text = text;
        return this;
    }
}
