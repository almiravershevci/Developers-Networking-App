/**
 * Shared Firestore mock for supertest suites.
 */
function createMockDoc(id, data, exists = true, ref = null) {
  return {
    id,
    exists,
    data: () => data,
    get: (field) => data?.[field],
    ref,
  };
}

function createFirestoreMock() {
  const store = new Map();

  function docRef(path) {
    const ref = {
      id: path.split('/').pop(),
      path,
      async get() {
        if (!store.has(path)) {
          return createMockDoc(ref.id, null, false, ref);
        }
        return createMockDoc(ref.id, store.get(path), true, ref);
      },
      async set(data, options = {}) {
        const current = store.get(path) || {};
        store.set(path, options.merge ? { ...current, ...data } : data);
      },
      async update(data) {
        const current = store.get(path) || {};
        store.set(path, { ...current, ...data });
      },
      async delete() {
        store.delete(path);
      },
      collection(subName) {
        return collection(`${path}/${subName}`);
      },
    };
    return ref;
  }

  function collection(name) {
    const prefix = name;

    return {
      doc(id) {
        return docRef(`${prefix}/${id}`);
      },
      async add(data) {
        const id = `auto_${store.size + 1}`;
        const path = `${prefix}/${id}`;
        store.set(path, data);
        return docRef(path);
      },
      where(field, op, value) {
        const filters = [{ field, op, value }];
        const chain = {
          where(f, o, v) {
            filters.push({ field: f, op: o, value: v });
            return chain;
          },
          orderBy() { return chain; },
          limit() { return chain; },
          startAfter() { return chain; },
          async get() {
            const docs = [...store.entries()]
              .filter(([key]) => key.startsWith(`${prefix}/`))
              .filter(([key]) => key.split('/').length === prefix.split('/').length + 1)
              .map(([key, value]) => {
                const id = key.split('/').pop();
                return createMockDoc(id, value, true);
              })
              .filter((doc) => filters.every(({ field, op, value: v }) => {
                const data = doc.data();
                if (op === '==') return data?.[field] === v;
                if (op === 'array-contains') {
                  return Array.isArray(data?.[field]) && data[field].includes(v);
                }
                return true;
              }));
            return { docs, empty: docs.length === 0 };
          },
        };
        return chain;
      },
      orderBy() { return this; },
      limit() { return this; },
      startAfter() { return this; },
      async get() {
        const docs = [...store.entries()]
          .filter(([key]) => key.startsWith(`${prefix}/`) && key.split('/').length === prefix.split('/').length + 1)
          .map(([key, value]) => createMockDoc(key.split('/').pop(), value, true));
        return { docs, empty: docs.length === 0 };
      },
    };
  }

  return {
    collection,
    batch() {
      const ops = [];
      return {
        set(ref, data) { ops.push(() => ref.set(data)); },
        async commit() { for (const op of ops) await op(); },
      };
    },
    async runTransaction(fn) {
      const tx = {
        async get(ref) { return ref.get(); },
        update(ref, data) { return ref.update(data); },
        set(ref, data) { return ref.set(data); },
      };
      return fn(tx);
    },
    __store: store,
  };
}

module.exports = { createFirestoreMock, createMockDoc };
