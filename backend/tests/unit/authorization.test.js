const { requireProjectReadAccess, canReadProject } = require('../../lib/authorization');
const { ApiError } = require('../../lib/errors');

jest.mock('../../lib/firestore', () => ({
  db: {
    collection: jest.fn(),
  },
}));

const { db } = require('../../lib/firestore');
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

describe('authorization', () => {
  beforeEach(() => jest.clearAllMocks());

  test('canReadProject allows public projects', async () => {
    mockProjectDoc('proj_public', { visibility: 'public', ownerUserId: 'owner-1' });
    const result = await canReadProject('proj_public', 'viewer-1');
    expect(result.allowed).toBe(true);
  });

  test('canReadProject denies private project for non-members', async () => {
    mockProjectDoc('proj_private', { visibility: 'private', ownerUserId: 'owner-1' }, false);
    const result = await canReadProject('proj_private', 'stranger-1');
    expect(result.allowed).toBe(false);
  });

  test('requireProjectReadAccess throws ApiError when forbidden', async () => {
    mockProjectDoc('proj_private', { visibility: 'private', ownerUserId: 'owner-1' }, false);
    await expect(requireProjectReadAccess('proj_private', 'stranger-1')).rejects.toThrow(ApiError);
  });
});
