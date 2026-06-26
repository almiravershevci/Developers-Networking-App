const { FieldValue } = require('firebase-admin/firestore');

const MatchWorkflow = {
  PENDING: 'pending',
  ACCEPTED: 'accepted',
  DECLINED: 'declined',
};

const EventRegistrationStatus = {
  GOING: 'going',
  WAITLIST: 'waitlist',
};

const AccountRole = {
  ADMIN: 'admin',
};

function directConversationId(userA, userB) {
  const sorted = [userA, userB].sort();
  return `direct_${sorted[0]}_${sorted[1]}`;
}

function timestampToIso(value) {
  if (!value || typeof value.toDate !== 'function') return null;
  return value.toDate().toISOString();
}

function mapUserProfile(doc) {
  if (!doc.exists) return null;
  const data = doc.data();
  return {
    id: doc.id,
    displayName: data.displayName || '',
    usernameLower: data.usernameLower || '',
    email: data.email || '',
    headline: data.headline || '',
    bio: data.bio || '',
    skillTags: Array.isArray(data.skillTags) ? data.skillTags : [],
    accountRole: data.accountRole || 'user',
    profileVisibility: data.profileVisibility || 'public',
    updatedAt: timestampToIso(data.updatedAt),
  };
}

function mapProject(doc) {
  const data = doc.data();
  return {
    id: doc.id,
    title: data.title || '',
    subtitle: data.subtitle || '',
    description: data.description || '',
    primaryStackLabel: data.primaryStackLabel || '',
    stackTags: Array.isArray(data.stackTags) ? data.stackTags : [],
    ownerUserId: data.ownerUserId || '',
    spotsOpen: Number(data.spotsOpen || 0),
    memberCount: Number(data.memberCount || 0),
    openRoleLabels: Array.isArray(data.openRoleLabels) ? data.openRoleLabels : [],
    lifecycleStatus: data.lifecycleStatus || '',
    visibility: data.visibility || '',
  };
}

function mapTask(doc) {
  const data = doc.data();
  return {
    id: doc.id,
    title: data.title || '',
    boardColumn: data.boardColumn || 'todo',
    priority: data.priority || 'medium',
    assigneeUserId: data.assigneeUserId || null,
    createdByUserId: data.createdByUserId || '',
    updatedAt: timestampToIso(data.updatedAt),
  };
}

function mapEvent(doc) {
  const data = doc.data();
  return {
    id: doc.id,
    title: data.title || '',
    summaryLine: data.summaryLine || '',
    participantCount: Number(data.participantCount || 0),
    formatKind: data.formatKind || 'online',
    eventStatus: data.eventStatus || 'scheduled',
    startsAt: timestampToIso(data.startsAt),
  };
}

function mapInboxNotification(doc) {
  const data = doc.data();
  return {
    id: doc.id,
    recipientUserId: data.recipientUserId || '',
    notificationKind: data.notificationKind || 'feed',
    title: data.title || '',
    body: data.body || '',
    deepLink: data.deepLink || null,
    read: Boolean(data.read),
    createdAt: timestampToIso(data.createdAt),
  };
}

function mapMatchRequest(doc) {
  const data = doc.data();
  return {
    id: doc.id,
    fromUserId: data.fromUserId || '',
    toUserId: data.toUserId || '',
    workflowStatus: data.workflowStatus || MatchWorkflow.PENDING,
    message: data.message || null,
    createdAt: timestampToIso(data.createdAt),
    resolvedAt: timestampToIso(data.resolvedAt),
  };
}

function mapConversation(doc) {
  const data = doc.data();
  return {
    id: doc.id,
    title: data.title || null,
    conversationKind: data.conversationKind || 'direct',
    participantIds: Array.isArray(data.participantIds) ? data.participantIds : [],
    lastMessagePreview: data.lastMessagePreview || '',
    lastMessageAt: timestampToIso(data.lastMessageAt),
    projectId: data.projectId || null,
  };
}

function mapMessage(doc) {
  const data = doc.data();
  return {
    id: doc.id,
    senderId: data.senderId || '',
    body: data.body || '',
    messageKind: data.messageKind || 'text',
    readByUserIds: Array.isArray(data.readByUserIds) ? data.readByUserIds : [],
    createdAt: timestampToIso(data.createdAt),
  };
}

module.exports = {
  AccountRole,
  EventRegistrationStatus,
  FieldValue,
  MatchWorkflow,
  directConversationId,
  mapConversation,
  mapEvent,
  mapInboxNotification,
  mapMatchRequest,
  mapMessage,
  mapProject,
  mapTask,
  mapUserProfile,
  timestampToIso,
};
