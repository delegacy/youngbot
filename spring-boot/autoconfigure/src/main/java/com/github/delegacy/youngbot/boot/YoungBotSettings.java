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
}
