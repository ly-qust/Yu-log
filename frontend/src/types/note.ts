export interface NoteItem {
  id: string;
  title: string;
  slug: string;
  summary?: string | null;
  content?: string | null;
  topic?: string | null;
  tags: string[];
  isPublic?: boolean | null;
  sortOrder?: number | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface NoteQuery {
  keyword?: string;
  topic?: string;
  page?: number;
  size?: number;
}

export interface AdminNoteQuery extends NoteQuery {
  isPublic?: boolean | '';
}

export interface NoteSavePayload {
  title: string;
  slug: string;
  summary?: string;
  content: string;
  topic?: string;
  tags: string[];
  isPublic: boolean;
  sortOrder?: number;
}
