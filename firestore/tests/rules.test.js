import { readFileSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
} from '@firebase/rules-unit-testing';
import { doc, getDoc, setDoc, updateDoc } from 'firebase/firestore';

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
});
