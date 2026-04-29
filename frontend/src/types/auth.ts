export interface UserInfo {
  id: string;
  username: string;
  nickname: string;
  roleCode: string;
}

export interface AuthTokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: 'Bearer';
  expiresIn: number;
  user: UserInfo;
}

export interface LoginPayload {
  username: string;
  password: string;
}
