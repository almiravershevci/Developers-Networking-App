const { validate, patchMeSchema } = require('../../middleware/validate');

function createMockRes() {
  const res = {
    statusCode: 200,
    body: null,
    status(code) {
      this.statusCode = code;
      return this;
    },
    json(payload) {
      this.body = payload;
      return this;
    },
  };
  return res;
}

describe('validate middleware', () => {
  test('rejects empty PATCH /me body', () => {
    const middleware = validate(patchMeSchema);
    const req = { body: {}, requestId: 'req-1' };
    const res = createMockRes();
    const next = jest.fn();

    middleware(req, res, next);

    expect(next).not.toHaveBeenCalled();
    expect(res.statusCode).toBe(400);
    expect(res.body.error).toBe('validation_error');
  });

  test('accepts valid PATCH /me body', () => {
    const middleware = validate(patchMeSchema);
    const req = { body: { displayName: 'Alex' }, requestId: 'req-2' };
    const res = createMockRes();
    const next = jest.fn();

    middleware(req, res, next);

    expect(next).toHaveBeenCalled();
    expect(req.validated.displayName).toBe('Alex');
  });
});
