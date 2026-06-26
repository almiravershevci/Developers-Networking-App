jest.mock('../../lib/firestore', () => ({
  db: {
    collection: jest.fn(),
  },
  projectId: 'developers-networking-app',
  admin: {
    auth: jest.fn(),
  },
}));

const { db } = require('../../lib/firestore');
const { canReadProject, assertCanReadProject } = require('../../lib/authorization');
const { createMockDoc } = require('../helpers/testUtils');

function mockProjectDoc(projectId, data, memberExists = false) {
  const projectRef = {
    get: jest.fn().mockResolvedValue(createMockDoc(projectId, data, Boolean(data))),
    collection: jest.fn().mockReturnValue({
      doc: jest.fn().mockReturnValue({
        get: jest.fn().mockResolvedValue(createMockDoc('member', {}, memberExists)),
      }),
    }),
  };

  db.collection.mockReturnValue({
    doc: jest.fn().mockReturnValue(projectRef),
  });

  return projectRef;
}

describe('authorization.canReadProject', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('allows public projects for any signed-in user', async () => {
    mockProjectDoc('proj_public', { visibility: 'public', ownerUserId: 'owner-1' });

    const result = await canReadProject('proj_public', 'viewer-1');
    expect(result.allowed).toBe(true);
    expect(result.reason).toBe('public');
  });

  test('allows unlisted projects for any signed-in user', async () => {
    mockProjectDoc('proj_unlisted', { visibility: 'unlisted', ownerUserId: 'owner-1' });

    const result = await canReadProject('proj_unlisted', 'viewer-1');
    expect(result.allowed).toBe(true);
  });

  test('allows private project for owner', async () => {
    mockProjectDoc('proj_private', { visibility: 'private', ownerUserId: 'owner-1' });

    const result = await canReadProject('proj_private', 'owner-1');
    expect(result.allowed).toBe(true);
    expect(result.reason).toBe('owner');
  });

  test('allows private project for members', async () => {
    mockProjectDoc(
      'proj_private',
      { visibility: 'private', ownerUserId: 'owner-1' },
      true,
    );

    const result = await canReadProject('proj_private', 'member-1');
    expect(result.allowed).toBe(true);
    expect(result.reason).toBe('member');
  });

  test('denies private project for non-members', async () => {
    mockProjectDoc(
      'proj_private',
      { visibility: 'private', ownerUserId: 'owner-1' },
      false,
    );

    const result = await canReadProject('proj_private', 'stranger-1');
    expect(result.allowed).toBe(false);
    expect(result.reason).toBe('forbidden');
  });

  test('returns not_found for missing project', async () => {
    mockProjectDoc('missing', null);

    const result = await canReadProject('missing', 'viewer-1');
    expect(result.allowed).toBe(false);
    expect(result.reason).toBe('not_found');
  });
});

describe('authorization.assertCanReadProject', () => {
  test('writes 403 response when access is denied', async () => {
    mockProjectDoc(
      'proj_private',
      { visibility: 'private', ownerUserId: 'owner-1' },
      false,
    );

    const res = {
      status: jest.fn().mockReturnThis(),
      json: jest.fn(),
    };

    const allowed = await assertCanReadProject('proj_private', 'stranger-1', res);
    expect(allowed).toBe(false);
    expect(res.status).toHaveBeenCalledWith(403);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({ error: 'forbidden' }),
    );
  });
});
