import { boardColumnLabel, isOpenTaskColumn, NotificationKind } from "../src/constants";

describe("Cloud Functions constants", () => {
  test("boardColumnLabel maps known columns", () => {
    expect(boardColumnLabel("in_progress")).toBe("In Progress");
    expect(boardColumnLabel("unknown")).toBe("unknown");
  });

  test("isOpenTaskColumn treats done as closed", () => {
    expect(isOpenTaskColumn("todo")).toBe(true);
    expect(isOpenTaskColumn("done")).toBe(false);
  });

  test("notification kinds include feed and message", () => {
    expect(NotificationKind.FEED).toBe("feed");
    expect(NotificationKind.MESSAGE).toBe("message");
  });
});

describe("Cloud Functions lib modules compile", () => {
  test("firestoreHelpers exports are loadable after build", () => {
    // eslint-disable-next-line @typescript-eslint/no-require-imports
    const admin = require("firebase-admin");
    if (admin.apps.length === 0) {
      admin.initializeApp({ projectId: "devconnect-functions-test" });
    }
    // eslint-disable-next-line @typescript-eslint/no-require-imports
    const helpers = require("../lib/lib/firestoreHelpers");
    expect(typeof helpers.ensureUserStats).toBe("function");
    expect(typeof helpers.createInboxNotification).toBe("function");
  });
});
