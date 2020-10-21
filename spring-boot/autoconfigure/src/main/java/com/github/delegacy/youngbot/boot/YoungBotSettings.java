package com.github.delegacy.youngbot.boot;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TBW.
 */
@ConfigurationProperties(prefix = "youngbot")
public class YoungBotSettings {
    /**
     * TBW.
     */
    public static class Line {
        private String channelToken = "";

        private String channelSecret = "";

        private String webhookPath = "/api/line/v1/webhook";

        /**
         * TBW.
         */
        public String getChannelToken() {
            return channelToken;
        }

        /**
         * TBW.
         */
        public void setChannelToken(String channelToken) {
            this.channelToken = requireNonNull(channelToken, "channelToken");
        }

        /**
         * TBW.
         */
        public String getChannelSecret() {
            return channelSecret;
        }

        /**
         * TBW.
         */
        public void setChannelSecret(String channelSecret) {
            this.channelSecret = requireNonNull(channelSecret, "channelSecret");
        }

        /**
         * TBW.
         */
        public String getWebhookPath() {
            return webhookPath;
        }

        /**
         * TBW.
         */
        public void setWebhookPath(String webhookPath) {
            this.webhookPath = webhookPath;
        }
    }

    /**
     * TBW.
     */
    public static class Slack {
        /**
         * TBW.
         */
        public static class Rtm {
            private boolean enabled;

            /**
             * TBW.
             */
            public boolean isEnabled() {
                return enabled;
            }

            /**
             * TBW.
             */
            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        }

        private String botToken = "";

        private String signingSecret = "";

        private String webhookPath = "/api/slack/v1/webhook";

        private Rtm rtm = new Rtm();

        /**
         * TBW.
         */
        public String getBotToken() {
            return botToken;
        }

        /**
         * TBW.
         */
        public void setBotToken(String botToken) {
            this.botToken = requireNonNull(botToken, "botToken");
        }

        /**
         * TBW.
         */
        public String getSigningSecret() {
            return signingSecret;
        }

        /**
         * TBW.
         */
        public void setSigningSecret(String signingSecret) {
            this.signingSecret = requireNonNull(signingSecret, "signingSecret");
        }

        /**
         * TBW.
         */
        public String getWebhookPath() {
            return webhookPath;
        }

        /**
         * TBW.
         */
        public void setWebhookPath(String webhookPath) {
            this.webhookPath = webhookPath;
        }

        /**
         * TBW.
         */
        public Rtm getRtm() {
            return rtm;
        }

        /**
         * TBW.
         */
        public void setRtm(Rtm rtm) {
            this.rtm = rtm;
        }
    }

    @Nullable
    private Line line;

    @Nullable
    private Slack slack;

    private String webhookPath = "/api/message/v1/webhook";

    /**
     * TBW.
     */
    @Nullable
    public Line getLine() {
        return line;
    }

    /**
     * TBW.
     */
    public void setLine(Line line) {
        this.line = requireNonNull(line, "line");
    }

    /**
     * TBW.
     */
    @Nullable
    public Slack getSlack() {
        return slack;
    }

    /**
     * TBW.
     */
    public void setSlack(Slack slack) {
        this.slack = requireNonNull(slack, "slack");
    }

    /**
     * TBW.
     */
    public String getWebhookPath() {
        return webhookPath;
    }

    /**
     * TBW.
     */
    public void setWebhookPath(String webhookPath) {
        this.webhookPath = webhookPath;
    }
}
