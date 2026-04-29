import type { PageResult } from './api';

export type InteractionStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface CommentSubmitPayload {
  nickname: string;
  email?: string;
  content: string;
}

export interface PublicComment {
  id: string;
  nickname: string;
  content: string;
  createdAt?: string | null;
  adminReply?: string | null;
  repliedAt?: string | null;
}

export interface AdminComment extends PublicComment {
  articleId: string;
  articleTitle?: string | null;
  email?: string | null;
  status: InteractionStatus;
  updatedAt?: string | null;
}

export interface AdminCommentQuery {
  articleId?: string;
  status?: InteractionStatus | '';
  keyword?: string;
  page?: number;
  size?: number;
}

export interface MessageSubmitPayload {
  nickname: string;
  email?: string;
  content: string;
}

export interface PublicMessage {
  id: string;
  nickname: string;
  content: string;
  adminReply?: string | null;
  createdAt?: string | null;
  repliedAt?: string | null;
}

export interface AdminMessage extends PublicMessage {
  email?: string | null;
  status: InteractionStatus;
  updatedAt?: string | null;
}

export interface AdminMessageQuery {
  status?: InteractionStatus | '';
  keyword?: string;
  page?: number;
  size?: number;
}

export type PublicMessagePage = PageResult<PublicMessage>;
