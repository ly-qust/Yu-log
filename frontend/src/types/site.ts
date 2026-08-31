import type { ArticleListItem } from './content';
import type { NoteItem } from './note';
import type { ProjectItem } from './project';
import type { TimelineEventItem } from './timeline';

export interface HomeHero {
  title: string;
  subtitle?: string | null;
  description?: string | null;
  statusText?: string | null;
}

export interface HomeStats {
  articleCount: number;
  projectCount: number;
  noteCount: number;
  messageCount: number;
}

export interface HomeOverview {
  hero: HomeHero;
  stats: HomeStats;
  latestArticles: ArticleListItem[];
  featuredProjects: ProjectItem[];
  latestNotes: NoteItem[];
  timelinePreview: TimelineEventItem[];
  currentlyLearning: string[];
}

export interface AboutProfile {
  nickname?: string;
  role?: string;
  avatar?: string;
  description?: string;
  location?: string;
  email?: string;
  githubUrl?: string;
  careerDirection?: string[];
  [key: string]: unknown;
}

export interface AboutData {
  profile: AboutProfile;
  skills: string[];
  education: string[];
  learningPhilosophy?: string | null;
}

export interface SiteSetting {
  id: string;
  settingKey: string;
  settingValue: string;
  settingType?: string | null;
  groupName?: string | null;
  description?: string | null;
  updatedAt?: string | null;
}

export interface SiteSettingUpdatePayload {
  settingValue: string;
  settingType?: string;
  groupName?: string;
  description?: string;
}

export interface SiteSettingBatchItem extends SiteSettingUpdatePayload {
  settingKey: string;
}
