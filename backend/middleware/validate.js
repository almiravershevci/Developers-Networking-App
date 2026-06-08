const { z } = require('zod');
const { sendValidationError } = require('../lib/errors');

/**
 * @param {import('zod').ZodSchema} schema
 * @param {'body' | 'query'} source
 */
function validate(schema, source = 'body') {
  return (req, res, next) => {
    const parsed = schema.safeParse(req[source]);
    if (!parsed.success) {
      const issues = parsed.error.issues.map((issue) => ({
        path: issue.path.join('.'),
        message: issue.message,
      }));
      return sendValidationError(res, issues, req.requestId);
    }
    req.validated = parsed.data;
    next();
  };
}

const patchMeSchema = z
  .object({
    displayName: z.string().trim().min(1).max(80).optional(),
    headline: z.string().trim().max(120).optional(),
    bio: z.string().trim().max(500).optional(),
  })
  .refine((value) => Object.keys(value).length > 0, {
    message: 'Provide at least one of displayName, headline, or bio.',
  });

const createMatchRequestSchema = z.object({
  toUserId: z.string().trim().min(1),
  message: z.string().trim().max(280).optional().nullable(),
});

const sendMessageSchema = z.object({
  body: z.string().trim().min(1).max(4000),
});

const eventRegistrationSchema = z.object({
  status: z.enum(['going', 'waitlist']).optional(),
});

const adminBroadcastSchema = z.object({
  title: z.string().trim().min(1).max(120),
  body: z.string().trim().min(1).max(500),
  audience: z.union([
    z.literal('all'),
    z.string().trim().min(1),
    z.array(z.string().trim().min(1)).min(1),
  ]).optional(),
});

const paginationQuerySchema = z.object({
  limit: z.coerce.number().int().min(1).max(50).optional(),
  cursor: z.string().trim().min(1).optional(),
});

module.exports = {
  validate,
  patchMeSchema,
  createMatchRequestSchema,
  sendMessageSchema,
  eventRegistrationSchema,
  adminBroadcastSchema,
  paginationQuerySchema,
};
