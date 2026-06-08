const { ApiError } = require('../../lib/errors');
const { asyncHandler } = require('../../lib/asyncHandler');

describe('asyncHandler', () => {
  test('forwards ApiError to Express error middleware', async () => {
    const handler = asyncHandler(async () => {
      throw new ApiError(403, 'forbidden', 'Denied');
    });

    const req = { requestId: 'req-1' };
    const res = {};
    const next = jest.fn();

    await handler(req, res, next);
    expect(next).toHaveBeenCalledWith(expect.any(ApiError));
  });
});
