const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const PROJECT_ID = "demo-no-project";
let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: fs.readFileSync("firestore.rules", "utf8"),
      host: "127.0.0.1",
      port: 8085
    }
  });
});

after(async () => {
  if (testEnv) await testEnv.cleanup();
});

beforeEach(async () => {
  if (testEnv) await testEnv.clearFirestore();
});

test("Unauthenticated user cannot read or write settings", async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore();
  const settingsDoc = unauthedDb.collection("users").doc("user_alice").collection("settings").doc("current");
  await assertFails(settingsDoc.get());
  await assertFails(settingsDoc.set({
    userId: "user_alice",
    defaultMode: "NORMAL",
    strobeFrequency: 5,
    hapticFeedback: true,
    createdAt: new Date(),
    updatedAt: new Date()
  }));
});

test("Authenticated user can create and read own settings", async () => {
  const aliceDb = testEnv.authenticatedContext("user_alice").firestore();
  const settingsDoc = aliceDb.collection("users").doc("user_alice").collection("settings").doc("current");

  await assertSucceeds(settingsDoc.set({
    userId: "user_alice",
    defaultMode: "NORMAL",
    strobeFrequency: 5,
    hapticFeedback: true,
    screenLightColor: "#FFFFFF",
    screenBrightness: 0.9,
    createdAt: new Date(),
    updatedAt: new Date()
  }));

  await assertSucceeds(settingsDoc.get());
});

test("User cannot read or write another user's settings", async () => {
  const aliceDb = testEnv.authenticatedContext("user_alice").firestore();
  const bobDb = testEnv.authenticatedContext("user_bob").firestore();

  const aliceSettings = aliceDb.collection("users").doc("user_alice").collection("settings").doc("current");
  await assertSucceeds(aliceSettings.set({
    userId: "user_alice",
    defaultMode: "NORMAL",
    strobeFrequency: 5,
    hapticFeedback: true,
    createdAt: new Date(),
    updatedAt: new Date()
  }));

  const bobTryingAlice = bobDb.collection("users").doc("user_alice").collection("settings").doc("current");
  await assertFails(bobTryingAlice.get());
  await assertFails(bobTryingAlice.set({
    userId: "user_alice",
    defaultMode: "NORMAL",
    strobeFrequency: 5,
    hapticFeedback: true,
    createdAt: new Date(),
    updatedAt: new Date()
  }));
});

test("Settings validation rejects invalid defaultMode or frequency", async () => {
  const aliceDb = testEnv.authenticatedContext("user_alice").firestore();
  const settingsDoc = aliceDb.collection("users").doc("user_alice").collection("settings").doc("current");

  // Invalid defaultMode
  await assertFails(settingsDoc.set({
    userId: "user_alice",
    defaultMode: "SUPER_FLASH",
    strobeFrequency: 5,
    hapticFeedback: true,
    createdAt: new Date(),
    updatedAt: new Date()
  }));

  // Frequency out of bounds (> 20)
  await assertFails(settingsDoc.set({
    userId: "user_alice",
    defaultMode: "STROBE",
    strobeFrequency: 50,
    hapticFeedback: true,
    createdAt: new Date(),
    updatedAt: new Date()
  }));
});

test("Flashlight logs can be created and read by owner, but cannot be modified", async () => {
  const aliceDb = testEnv.authenticatedContext("user_alice").firestore();
  const logDoc = aliceDb.collection("users").doc("user_alice").collection("logs").doc("log_1");

  await assertSucceeds(logDoc.set({
    userId: "user_alice",
    action: "ON",
    durationSeconds: 15,
    createdAt: new Date()
  }));

  await assertSucceeds(logDoc.get());

  // Updates must be rejected (immutable log)
  await assertFails(logDoc.update({
    durationSeconds: 30
  }));
});
