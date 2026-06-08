function createMockDoc(id, data, exists = true) {
  return {
    id,
    exists,
    data: () => data,
    get: (field) => data?.[field],
  };
}

function createMockCollection(docsById = {}) {
  const docs = new Map(Object.entries(docsById));

  return {
    doc(id) {
      const ref = {
        id,
        async get() {
          if (!docs.has(id)) {
            return createMockDoc(id, null, false);
          }
          return createMockDoc(id, docs.get(id), true);
        },
        collection(name) {
          const nested = docs.get(`${id}.__${name}`) || {};
          return createMockCollection(nested);
        },
      };
      return ref;
    },
    where() {
      return this;
    },
    limit() {
      return this;
    },
    async get() {
      const entries = [...docs.entries()].filter(([key]) => !key.includes('.__'));
      return {
        docs: entries.map(([key, value]) => createMockDoc(key, value, true)),
        empty: entries.length === 0,
      };
    },
  };
}

function createTestAuthMiddleware(uid = 'test-user-1') {
  return (req, _res, next) => {
    req.user = { uid, email: `${uid}@example.com` };
    next();
  };
}

module.exports = {
  createMockDoc,
  createMockCollection,
  createTestAuthMiddleware,
};
