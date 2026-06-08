const { attachWeakEtag, sendJsonWithEtag } = require('../../lib/etag');

describe('etag helpers', () => {
  test('attachWeakEtag returns stable hash', () => {
    const res = { setHeader: jest.fn() };
    const tag = attachWeakEtag(res, { ok: true });
    expect(tag).toMatch(/^"[a-f0-9]{40}"$/);
    expect(res.setHeader).toHaveBeenCalledWith('ETag', tag);
  });

  test('sendJsonWithEtag returns 304 when If-None-Match matches', () => {
    const req = { headers: {} };
    let statusCode = 200;
    const res = {
      setHeader: jest.fn(),
      status(code) {
        statusCode = code;
        return this;
      },
      end: jest.fn(),
      type: jest.fn().mockReturnThis(),
      send: jest.fn(),
    };

    sendJsonWithEtag(req, res, { items: [1] });
    const etag = res.setHeader.mock.calls.find(([key]) => key === 'ETag')[1];
    req.headers['if-none-match'] = etag;

    sendJsonWithEtag(req, res, { items: [1] });
    expect(statusCode).toBe(304);
    expect(res.end).toHaveBeenCalled();
  });
});
