import { readFileSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
} from '@firebase/rules-unit-testing';
import { doc, getDoc, setDoc, updateDoc, deleteDoc } from 'firebase/firestore';

const __dirname = dirname(fileURLToPath(import.meta.url));
const rules = readFileSync(resolve(__dirname, '../../firestore.rules'), 'utf8');
const PROJECT_ID = 'devconnect-rules-test';

let testEnv;

beforeAll(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: { rules },
  });
});

afterAll(async () => {
  await testEnv.cleanup();
});

beforeEach(async () => {
  await testEnv.clearFirestore();
});

function authedDb(uid, claims = {}) {
  return testEnv
    .authenticatedContext(uid, claims)
    .firestore();
}

async function seedBaseData() {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await setDoc(doc(db, 'users/alice'), {
      schemaVersion: 1,
      displayName: 'Alice',
      usernameLower: 'alice',
      accountRole: 'user',
      profileVisibility: 'public',
    });
    await setDoc(doc(db, 'users/bob'), {
      schemaVersion: 1,
      displayName: 'Bob',
      usernameLower: 'bob',
      accountRole: 'user',
      profileVisibility: 'public',
    });
    await setDoc(doc(db, 'projects/proj_public'), {
      schemaVersion: 1,
      title: 'Public Project',
      visibility: 'public',
      ownerUserId: 'alice',
      lifecycleStatus: 'recruiting',
    });
    await setDoc(doc(db, 'projects/proj_private'), {
      schemaVersion: 1,
      title: 'Private Project',
      visibility: 'private',
      ownerUserId: 'alice',
      lifecycleStatus: 'recruiting',
    });
    await setDoc(doc(db, 'projects/proj_private/members/bob'), {
      memberUserId: 'bob',
      memberRole: 'contributor',
    });
    await setDoc(doc(db, 'userStats/alice'), {
      schemaVersion: 1,
      openTasksCount: 0,
    });
    await setDoc(doc(db, 'inbox/notif_alice'), {
      schemaVersion: 1,
      recipientUserId: 'alice',
      notificationKind: 'feed',
      title: 'Hello',
      body: 'Welcome',
      read: false,
    });
    await setDoc(doc(db, 'users/admin'), {
      schemaVersion: 1,
      displayName: 'Admin',
      usernameLower: 'admin',
      accountRole: 'admin',
      profileVisibility: 'public',
    });
    await setDoc(doc(db, 'conversations/conv_team'), {
      schemaVersion: 1,
      participantIds: ['alice', 'bob'],
      title: 'Team',
      createdBy: 'alice',
    });
    await setDoc(doc(db, 'matchRequests/mr_1'), {
      schemaVersion: 1,
      fromUserId: 'alice',
      toUserId: 'bob',
      workflowStatus: 'pending',
    });
    await setDoc(doc(db, 'events/evt_1'), {
      schemaVersion: 1,
      title: 'Meetup',
      eventStatus: 'scheduled',
    });
    await setDoc(doc(db, 'activity/act_alice'), {
      schemaVersion: 1,
      audienceUserId: 'alice',
      verb: 'joined',
      summary: 'Joined project',
    });
    await setDoc(doc(db, 'newsHighlights/nh_1'), {
      schemaVersion: 1,
      title: 'Release',
      sortOrder: 1,
    });
    await setDoc(doc(db, 'collaboratorSuggestions/cs_1'), {
      schemaVersion: 1,
      viewerUserId: 'alice',
      suggestedUserId: 'bob',
      rank: 1,
    });
    await setDoc(doc(db, 'projects/proj_private/tasks/task_1'), {
      schemaVersion: 1,
      title: 'Task',
      boardColumn: 'todo',
      assigneeUserId: 'bob',
    });
  });
}

describe('Firestore security rules', () => {
  test('clients cannot write userStats (server-only)', async () => {
    await seedBaseData();
    const db = authedDb('alice');

    await assertFails(
      updateDoc(doc(db, 'userStats/alice'), { openTasksCount: 99 }),
    );
  });

  test('users can read their own inbox notifications', async () => {
    await seedBaseData();
    const db = authedDb('alice');

    await assertSucceeds(getDoc(doc(db, 'inbox/notif_alice')));
  });

  test('users cannot read another user inbox notification', async () => {
    await seedBaseData();
    const db = authedDb('bob');

    await assertFails(getDoc(doc(db, 'inbox/notif_alice')));
  });

  test('clients cannot create inbox rows (Functions/admin only)', async () => {
    await seedBaseData();
    const db = authedDb('alice');

    await assertFails(
      setDoc(doc(db, 'inbox/notif_new'), {
        schemaVersion: 1,
        recipientUserId: 'alice',
        notificationKind: 'feed',
        title: 'Spam',
        body: 'Spam',
        read: false,
      }),
    );
  });

  test('signed-in users can read public projects', async () => {
    await seedBaseData();
    const db = authedDb('bob');

    await assertSucceeds(getDoc(doc(db, 'projects/proj_public')));
  });

  test('non-members cannot read private projects', async () => {
    await seedBaseData();
    const db = authedDb('charlie');

    await assertFails(getDoc(doc(db, 'projects/proj_private')));
  });

  test('project members can read private projects', async () => {
    await seedBaseData();
    const db = authedDb('bob');

    await assertSucceeds(getDoc(doc(db, 'projects/proj_private')));
  });

  test('conversation participants can read thread', async () => {
    await seedBaseData();
    const db = authedDb('alice');
    await assertSucceeds(getDoc(doc(db, 'conversations/conv_team')));
  });

  test('non-participants cannot read conversation', async () => {
    await seedBaseData();
    const db = authedDb('charlie');
    await assertFails(getDoc(doc(db, 'conversations/conv_team')));
  });

  test('participants can create messages with own senderId', async () => {
    await seedBaseData();
    const db = authedDb('alice');
    await assertSucceeds(setDoc(doc(db, 'conversations/conv_team/messages/m1'), {
      schemaVersion: 1,
      senderId: 'alice',
      body: 'Hi team',
      messageKind: 'text',
    }));
  });

  test('participants cannot spoof senderId on messages', async () => {
    await seedBaseData();
    const db = authedDb('alice');
    await assertFails(setDoc(doc(db, 'conversations/conv_team/messages/m2'), {
      schemaVersion: 1,
      senderId: 'bob',
      body: 'Spoof',
      messageKind: 'text',
    }));
  });

  test('match request parties can read request', async () => {
    await seedBaseData();
    const db = authedDb('bob');
    await assertSucceeds(getDoc(doc(db, 'matchRequests/mr_1')));
  });

  test('unrelated user cannot read match request', async () => {
    await seedBaseData();
    const db = authedDb('charlie');
    await assertFails(getDoc(doc(db, 'matchRequests/mr_1')));
  });

  test('user can create match request as sender', async () => {
    await seedBaseData();
    const db = authedDb('alice');
    await assertSucceeds(setDoc(doc(db, 'matchRequests/mr_new'), {
      schemaVersion: 1,
      fromUserId: 'alice',
      toUserId: 'charlie',
      workflowStatus: 'pending',
    }));
  });

  test('user cannot create self match request', async () => {
    await seedBaseData();
    const db = authedDb('alice');
    await assertFails(setDoc(doc(db, 'matchRequests/mr_self'), {
      schemaVersion: 1,
      fromUserId: 'alice',
      toUserId: 'alice',
      workflowStatus: 'pending',
    }));
  });

  test('signed-in users can read events', async () => {
    await seedBaseData();
    const db = authedDb('bob');
    await assertSucceeds(getDoc(doc(db, 'events/evt_1')));
  });

  test('non-admin cannot create events', async () => {
    await seedBaseData();
    const db = authedDb('alice');
    await assertFails(setDoc(doc(db, 'events/evt_new'), {
      schemaVersion: 1,
      title: 'Hack day',
      eventStatus: 'scheduled',
    }));
  });

  test('user can RSVP for self', async () => {
    await seedBaseData();
    const db = authedDb('bob');
    await assertSucceeds(setDoc(doc(db, 'events/evt_1/registrations/bob'), {
      schemaVersion: 1,
      userId: 'bob',
      status: 'going',
    }));
  });

  test('user cannot RSVP for another user', async () => {
    await seedBaseData();
    const db = authedDb('alice');
    await assertFails(setDoc(doc(db, 'events/evt_1/registrations/bob'), {
      schemaVersion: 1,
      userId: 'bob',
      status: 'going',
    }));
  });

  test('project member can read tasks', async () => {
    await seedBaseData();
    const db = authedDb('bob');
    await assertSucceeds(getDoc(doc(db, 'projects/proj_private/tasks/task_1')));
  });

  test('non-member cannot read private project tasks', async () => {
    await seedBaseData();
    const db = authedDb('charlie');
    await assertFails(getDoc(doc(db, 'projects/proj_private/tasks/task_1')));
  });

  test('member can create task on accessible project', async () => {
    await seedBaseData();
    const db = authedDb('bob');
    await assertSucceeds(setDoc(doc(db, 'projects/proj_private/tasks/task_new'), {
      schemaVersion: 1,
      title: 'New task',
      boardColumn: 'todo',
      assigneeUserId: 'bob',
    }));
  });

  test('users can read own activity feed', async () => {
    await seedBaseData();
    const db = authedDb('alice');
    await assertSucceeds(getDoc(doc(db, 'activity/act_alice')));
  });

  test('users cannot read another user activity feed', async () => {
    await seedBaseData();
    const db = authedDb('bob');
    await assertFails(getDoc(doc(db, 'activity/act_alice')));
  });

  test('clients cannot create activity rows', async () => {
    await seedBaseData();
    const db = authedDb('alice');
    await assertFails(setDoc(doc(db, 'activity/act_new'), {
      schemaVersion: 1,
      audienceUserId: 'alice',
      verb: 'joined',
      summary: 'Fake',
    }));
  });

  test('signed-in users can read news highlights', async () => {
    await seedBaseData();
    const db = authedDb('bob');
    await assertSucceeds(getDoc(doc(db, 'newsHighlights/nh_1')));
  });

  test('non-admin cannot write news highlights', async () => {
    await seedBaseData();
    const db = authedDb('alice');
    await assertFails(setDoc(doc(db, 'newsHighlights/nh_new'), {
      schemaVersion: 1,
      title: 'Leak',
      sortOrder: 9,
    }));
  });

  test('viewer can read collaborator suggestions', async () => {
    await seedBaseData();
    const db = authedDb('alice');
    await assertSucceeds(getDoc(doc(db, 'collaboratorSuggestions/cs_1')));
  });

  test('other users cannot read suggestions meant for alice', async () => {
    await seedBaseData();
    const db = authedDb('bob');
    await assertFails(getDoc(doc(db, 'collaboratorSuggestions/cs_1')));
  });

  test('admin can create inbox notifications', async () => {
    await seedBaseData();
    const db = authedDb('admin');
    await assertSucceeds(setDoc(doc(db, 'inbox/notif_admin'), {
      schemaVersion: 1,
      recipientUserId: 'alice',
      notificationKind: 'feed',
      title: 'Ops',
      body: 'Maintenance',
      read: false,
    }));
  });

  test('user can mark own inbox notification read', async () => {
    await seedBaseData();
    const db = authedDb('alice');
    await assertSucceeds(updateDoc(doc(db, 'inbox/notif_alice'), { read: true }));
  });

  test('user can delete own inbox notification', async () => {
    await seedBaseData();
    const db = authedDb('alice');
    await assertSucceeds(deleteDoc(doc(db, 'inbox/notif_alice')));
  });
});
