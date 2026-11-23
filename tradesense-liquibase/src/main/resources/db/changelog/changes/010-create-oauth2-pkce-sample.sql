--liquibase formatted sql

--changeset tradesense:021-register-tradesense-ui-pkce
INSERT INTO oauth2_registered_client (
    id,
    client_id,
    client_name,
    client_authentication_methods,
    authorization_grant_types,
    redirect_uris,
    post_logout_redirect_uris,
    scopes,
    client_settings,
    token_settings
) VALUES (
    'tradesense-ui-registration',
    'tradesense-ui',
    'TradeSense Browser UI',
    'none',
    'authorization_code,refresh_token',
    'http://localhost:5173/login/oauth2/code/tradesense,http://127.0.0.1:5173/login/oauth2/code/tradesense',
    'http://localhost:5173/,http://127.0.0.1:5173/',
    'openid,profile,email,read,write',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings":{"org.springframework.security.oauth2.server.authorization.client.settings.ClientSettings":{"require-proof-key":true,"require-authorization-consent":true}}}',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings":{}}'
)
ON CONFLICT (id) DO NOTHING;

