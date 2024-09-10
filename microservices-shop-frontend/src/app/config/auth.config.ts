import { PassedInitialConfig } from 'angular-auth-oidc-client';

export const authConfig: PassedInitialConfig = {
  config: {
    authority: 'http://localhost:8181/realms/master/protocol/openid-connect/auth?client_id=security-admin-console&redirect_uri=http%3A%2F%2Flocalhost%3A8181%2Fadmin%2Fmaster%2Fconsole%2F&state=085281cd-05b0-46d0-8032-45de7fc9b676&response_mode=fragment&response_type=code&scope=openid&nonce=e5683280-8ad9-4e52-8332-4011db7fb948&code_challenge=PzgHAIk5etGXGkssbHVRpXAGyoGkT5LWj3dR3cjiLSA&code_challenge_method=S256',
    redirectUrl: window.location.origin,
    postLogoutRedirectUri: window.location.origin,
    clientId: 'angular-client',
    scope: 'openid profile offline_access',
    responseType: 'code',
    silentRenew: true,
    useRefreshToken: true,
    renewTimeBeforeTokenExpiresInSeconds: 30,
  }
}
