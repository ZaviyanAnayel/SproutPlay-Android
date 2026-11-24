# SproutPlay — Play Store Listing Pack (PREPARED, not submitted)

Prepared 2026-11-24 ~18:35 UTC by the research run, for the main agent to carry
into the Play-live step (BUILD_PLAN.md part 11). Nothing here has been submitted
to Play Console. The build was actively in progress when this was written
(engine + 5 candy modes + board UI written, package rename and keystore
underway) — re-verify every claim against the finished build before submitting.

User order behind it (main chat 2026-11-24 18:18 + 18:23 UTC): rebuild SproutPlay
as a colourful, eye-catchy Candy Crush-style game that TEACHES through gaming;
remove the old 50 quiz games; premium gaming; make it live on Play. He also
asked "pc off kr du?" — answer recorded in BUILD_PLAN.md: YES, all work runs
server-side, his PC is not needed.

## Facts verified from the code (grounding for everything below)

- App: SproutPlay, native Kotlin + Jetpack Compose, version 1.0 (versionCode 1).
- Package (build config): `com.zaviyanllc.sproutplay`; minSdk 24, targetSdk 36
  (`app/build.gradle.kts`, read 2026-11-24).
- Fully offline: AndroidManifest.xml has NO INTERNET permission; no ads,
  analytics, Firebase, or purchase SDKs in `app/build.gradle.kts` (all such
  dependencies are commented out). No account, no sign-up.
- On-device only: Room database holds child profiles, progress, stickers and
  settings; TTS via the device's own text-to-speech service. Nothing leaves
  the phone. (Original AI Studio export claimed a Gemini API — verified FALSE
  in code on 2026-11-24, so the listing must NOT claim any AI feature.)
- The 5 games (from `games/match3/CandyModes.kt`, read 2026-11-24):
  1. Number Crunch — match candies, popped digits add up to a target sum.
  2. Word Pop — pop letters to spell the word (CAT … PLANET by age tier).
  3. Pattern Pop — collect the exact colour pattern shown.
  4. Science Pop — collect water, sunshine and leaves.
  5. Candy Blast — pure score attack, combos and cascades.
  Each game has 10 levels = 50 levels total. Age tiers: Pre-School, K–1,
  Grade 2–3, Grade 4–5.
- Kept from the original: Parent Zone with a maths gate, sticker rewards,
  screen-time limit, TTS praise.
- Premium: the user shared a premium unlock key in his 18:18 message. The key
  value is NOT written anywhere in this file, in BUILD_PLAN.md, or in any
  proposal — it stays transient in the chat only. Whoever wires the premium
  gate must take it from that message and never store it in a file or log.

## 1. Play listing copy (ready to paste)

- **App name:** SproutPlay
- **Short description (69 / 80 chars):**
  `Match-3 candy games that teach math, words and science. 100% offline.`
- **Category:** Education
- **Full description (draft):**

```
SproutPlay turns learning into a candy game! Kids swap, match and pop
bright candies — and every pop teaches something.

🍬 5 SWEET GAMES, 50 LEVELS
• Number Crunch — pop candies and add up to the target number
• Word Pop — pop the letters to spell the magic word
• Pattern Pop — follow the colour pattern and collect it exactly
• Science Pop — collect water drops, sunshine and leaves
• Candy Blast — big combos, cascades and huge scores

🎓 LEARNING THAT FEELS LIKE PLAY
• Maths, spelling, patterns, logic and science — for Pre-School to Grade 5
• Levels grow with your child: bigger numbers, longer words, trickier goals
• Stars, stickers and sweet praise on every win

👨‍👩‍👧 SAFE FOR KIDS, BY DESIGN
• 100% offline — no internet needed, nothing ever leaves the phone
• No ads, no in-app purchases, no accounts, no tracking
• Parent Zone with a maths gate, plus a screen-time limit you control

Made with love by Zaviyan.
```

- **Contact email / website:** NOT decided yet — do not invent one. CalcWorker
  uses support@calcworker.com; SproutPlay needs its own decision (support
  address or business@zaviyanllc.com) before the listing is created.

## 2. Privacy policy (DRAFT — needs hosting before submission)

Modelled on the Gestara privacy-policy draft
(`~/workspace/apps/pregnancy-tracker/privacy-policy-draft.md`), adapted to a
kids' offline game. The play_console_publishing skill is explicit: a draft
that is not hosted blocks submission — a public URL must exist first.

> **Privacy Policy — SproutPlay (DRAFT)**
>
> Effective Date: [to be set at launch] — Operated by: Zaviyan (Zaviyan LLC)
>
> 1. Introduction. SproutPlay is a children's learning game operated by Zaviyan.
>    Our core promise: **your child's information stays on your phone.**
> 2. Information stored only on your device. The app works fully offline and
>    needs no account or sign-up. A child's first name/age (if entered),
>    game progress, levels, stars, stickers and settings are stored only in
>    the app's private on-device database and are never transmitted to us or
>    to any third party. Uninstalling the app permanently deletes all of it.
>    We have no copy and no way to recover it.
> 3. Text-to-speech. Spoken praise uses your phone's built-in text-to-speech
>    service. No voice or text is sent anywhere.
> 4. Information we do NOT collect: no name, email, phone number or address
>    beyond what stays on the device; no location; no advertising identifiers;
>    no analytics or tracking SDKs; no photos, audio recordings or contacts.
>    We do not sell personal information and we do not share it with data
>    brokers. We show no ads and offer no in-app purchases.
> 5. Children's privacy. SproutPlay is designed for children (Pre-School to
>    Grade 5) and their parents. Because the app collects nothing and sends
>    nothing off the device, we hold no children's personal information.
>    Parents control everything in the Parent Zone, protected by a maths gate.
> 6. Data security. Entries never leave the device, so they are protected by
>    your phone's own security (screen lock, encryption).
> 7. Your rights. View, change or delete everything inside the app at any
>    time, or remove it all at once by uninstalling the app.
> 8. Changes. If this policy changes, the updated version will be published
>    at the policy URL on the app's Play Store listing, with a new effective
>    date.
> 9. Contact. Questions about this policy: contact Zaviyan through the support
>    channel listed on the app's Play Store listing.
>
> **Hosting decision still open:** CalcWorker's policy lives at
> https://calcworker.com/privacy.html. SproutPlay needs the same treatment —
> e.g. a page on zaviyanllc.com or calcworker.com — decided and published
> before the Play submission. Do NOT submit with a placeholder URL.

## 3. Data safety answers (ready for the Console form)

Grounded in the manifest + build.gradle.kts checks above (no network
permission, no analytics/ads SDKs, everything on-device):

- Does your app collect or share any user data? **NO** — all data types: No.
- Data types to mark "not collected": personal info, financial info, health
  & fitness, location, contacts, photos/videos, audio, files, calendar, app
  activity, app info & performance, device IDs.
- Is data encrypted in transit? Not applicable — nothing is transmitted.
- Can users request data deletion? YES — everything is deleted by clearing
  app data or uninstalling; there is no account, so no account-deletion flow
  is needed.
- Independent security review: NO.
- Re-verify after any future SDK is added: any analytics/ads/network SDK
  would change these answers.

## 4. Families / target-audience checklist (complete in Console)

- App is primarily directed at children (Pre-School to Grade 5, ages ~3–11):
  enrol in **Designed for Families** and declare the target age groups in
  Console (confirm the exact age bands there — do not guess them here).
- Ads: **No**. In-app purchases: **No** (confirm the premium unlock is a
  key entered in the app, not a Play purchase — if that changes, the answers
  change).
- Content rating: complete the IARC questionnaire in Console.
- Teacher Approved: optional; not applied for.
- Closed testing first: the family testers Google Group already serves
  CalcWorker + Gestara (per play_console_publishing skill: keep one group
  for the app family, do not create ad-hoc tester lists). Production rollout
  only after the closed-test build is verified on a real phone — the user
  tests the debug APK first (BUILD_PLAN.md part 9).

## 5. Store assets still needed

- App icon 512×512 — derive from the existing launcher art
  (`res/drawable/ic_launcher_art.jpg`, treehouse/sprout-star artwork in the
  original ZIP).
- Feature graphic 1024×500 — not yet made.
- Phone screenshots (minimum 2) — take from the real device build once the
  candy UI is verified; must show the match-3 board, not the old quiz screens.
- Privacy policy hosted URL — see §2 (blocking item).

## 6. What is NOT in this pack (waits on the build / the user)

- The signed release AAB (needs the CI build verified first; keystore now
  exists under `release/` — never copy its password anywhere).
- Play Console app creation, listing submission, rollout — live-browser work
  for the main agent after the debug APK passes on the user's phone.
- If a Google sign-in code lands on the user's phone during Console work,
  that single step waits on him (per his standing access rule).
