/**
 * Shared Firestore mock for supertest suites.
 */
function createMockDoc(id, data, exists = true) {
  return {
    id,
    exists,
    data: () => data,
    get: (field) => data?.[field],
  };
}

function createFirestoreMock() {
  const store = new Map();

  function collection(name) {
    const prefix = name;

    return {
      doc(id) {
        const path = `${prefix}/${id}`;
        return {
          id,
          async get() {
            if (!store.has(path)) {
              return createMockDoc(id, null, false);
            }
            return createMockDoc(id, store.get(path), true);
          },
          async set(data, options = {}) {
            const current = store.get(path) || {};
            store.set(path, options.merge ? { ...current, ...data } : data);
          },
          async update(data) {
            const current = store.get(path) || {};
            store.set(path, { ...current, ...data });
          },
          collection(subName) {
            return collection(`${path}/${subName}`);
          },
        };
      },
      where() {
        return this;
      },
      orderBy() {
        return this;
      },
      limit() {
        return this;
      },
      startAfter() {
        return this;
      },
      async get() {
        const docs = [...store.entries()]
          .filter(([key]) => key.startsWith(`${prefix}/`) && key.split('/').length === 2)
          .map(([key, value]) => createMockDoc(key.split('/')[1], value, true));
        return { docs, empty: docs.length === 0 };
      },
    };
  }

  return {
    collection,
    batch() {
      const ops = [];
      return {
        set(ref, data) {
          ops.push(() => ref.set(data));
        },
        async commit() {
          for (const op of ops) await op();
        },
      };
    },
  };
}

module.exports = { createFirestoreMock, createMockDoc };
