/**
 * Encode a Firestore cursor as an opaque base64url token.
 * @param {{ id: string, sortValue?: string | null }} payload
 */
function encodeCursor(payload) {
  return Buffer.from(JSON.stringify(payload), 'utf8').toString('base64url');
}

/**
 * @param {string | undefined} raw
 * @returns {{ id: string, sortValue?: string | null } | null}
 */
function decodeCursor(raw) {
  if (!raw || typeof raw !== 'string') return null;
  try {
    const parsed = JSON.parse(Buffer.from(raw, 'base64url').toString('utf8'));
    if (!parsed || typeof parsed.id !== 'string') return null;
    return parsed;
  } catch {
    return null;
  }
}

function parseLimit(raw, fallback = 25, max = 50) {
  const value = Number(raw);
  if (!Number.isFinite(value) || value <= 0) return fallback;
  return Math.min(Math.floor(value), max);
}

/**
 * Apply cursor pagination to a Firestore query ordered by a single field.
 * Fetches limit+1 to detect hasMore.
 */
async function paginateQuery(query, { limit, cursor, collectionRef }) {
  let pagedQuery = query.limit(limit + 1);

  const decoded = decodeCursor(cursor);
  if (decoded?.id) {
    const cursorSnap = await collectionRef.doc(decoded.id).get();
    if (cursorSnap.exists) {
      pagedQuery = query.startAfter(cursorSnap).limit(limit + 1);
    }
  }

  const snap = await pagedQuery.get();
  const docs = snap.docs.slice(0, limit);
  const hasMore = snap.docs.length > limit;
  const lastDoc = docs.at(-1);

  return {
    docs,
    pagination: {
      limit,
      hasMore,
      nextCursor: hasMore && lastDoc
        ? encodeCursor({ id: lastDoc.id })
        : null,
    },
  };
}

module.exports = {
  encodeCursor,
  decodeCursor,
  parseLimit,
  paginateQuery,
};
