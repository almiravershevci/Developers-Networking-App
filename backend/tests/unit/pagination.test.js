const { decodeCursor, encodeCursor, parseLimit } = require('../../lib/pagination');

describe('pagination helpers', () => {
  test('encodeCursor and decodeCursor round-trip', () => {
    const token = encodeCursor({ id: 'doc-123', sortValue: '2026-01-01T00:00:00.000Z' });
    expect(decodeCursor(token)).toEqual({
      id: 'doc-123',
      sortValue: '2026-01-01T00:00:00.000Z',
    });
  });

  test('parseLimit clamps invalid and oversized values', () => {
    expect(parseLimit(undefined, 25, 50)).toBe(25);
    expect(parseLimit('999', 25, 50)).toBe(50);
    expect(parseLimit('-1', 25, 50)).toBe(25);
  });
});
