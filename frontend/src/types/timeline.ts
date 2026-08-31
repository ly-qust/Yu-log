export interface TimelineEventItem {
  id: string;
  title: string;
  description?: string | null;
  eventDate: string;
  type?: string | null;
  tags: string[];
  sortOrder?: number | null;
  visible?: boolean | null;
  createdAt?: string | null;
}

export interface TimelineQuery {
  type?: string;
  page?: number;
  size?: number;
}

export interface AdminTimelineQuery extends TimelineQuery {
  keyword?: string;
}

export interface TimelineSavePayload {
  title: string;
  description?: string;
  eventDate: string;
  type?: string;
  tags: string[];
  sortOrder?: number;
  visible: boolean;
}
