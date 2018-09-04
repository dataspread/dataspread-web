package api;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer,
        ApplicationListener {

    // TODO remove this.
    public static String MESSAGE_PREFIX = " ";

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ds-push").setAllowedOrigins("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/push");
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof SessionUnsubscribeEvent) {
            SessionUnsubscribeEvent sessionUnsubscribeEvent = (SessionUnsubscribeEvent) applicationEvent;
            UISessionManager.getInstance()
                    .getUISession(sessionUnsubscribeEvent.getMessage()
                    .getHeaders().get("simpSessionId").toString())
                    .clearSheet();

        } else if (applicationEvent instanceof SessionDisconnectEvent) {
            SessionDisconnectEvent sessionDisconnectEvent = (SessionDisconnectEvent) applicationEvent;
            UISessionManager.getInstance().deleteSession(
                            sessionDisconnectEvent.getMessage()
                                    .getHeaders().get("simpSessionId").toString());

        } else if (applicationEvent instanceof SessionConnectEvent) {
            SessionConnectEvent sessionConnectEvent = (SessionConnectEvent) applicationEvent;
            UISessionManager.getInstance().addSession(
                                sessionConnectEvent.getMessage()
                                    .getHeaders().get("simpSessionId").toString());
        }
    }
}
