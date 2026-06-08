export const SCHEMA_VERSION = 1;

export const NotificationKind = {
  TASK_UPDATE: "task_update",
  MESSAGE: "message",
  MATCH: "match",
  PROJECT_INVITE: "project_invite",
  EVENT: "event",
  FEED: "feed",
} as const;

export const ActivityVerb = {
  COMMENTED: "commented",
  STATUS_CHANGED: "status_changed",
  INVITED: "invited",
  JOINED: "joined",
} as const;

export const TaskBoardColumn = {
  TODO: "todo",
  IN_PROGRESS: "in_progress",
  DONE: "done",
  BLOCKED: "blocked",
} as const;

export const BOARD_COLUMN_LABELS: Record<string, string> = {
  [TaskBoardColumn.TODO]: "To Do",
  [TaskBoardColumn.IN_PROGRESS]: "In Progress",
  [TaskBoardColumn.DONE]: "Done",
  [TaskBoardColumn.BLOCKED]: "Blocked",
};

export function boardColumnLabel(column: string): string {
  return BOARD_COLUMN_LABELS[column] ?? column;
}

export function isOpenTaskColumn(column: string): boolean {
  return column !== TaskBoardColumn.DONE;
}
