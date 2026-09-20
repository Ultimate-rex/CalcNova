# Privacy Policy — CalcNova

**Last updated:** 2026

## Summary

CalcNova does not collect, store, transmit, or share any personal data,
under any circumstances. It works fully offline.

## What data CalcNova uses

CalcNova saves two things, and only on your own device:

| Data | Where it's stored | Purpose |
|---|---|---|
| Calculation history (expression + result + timestamp) | `history.json` in the app's private storage | So you can see and reuse past calculations |
| App preferences (theme, auto-calculate, angle mode, etc.) | `settings.json` in the app's private storage | So your settings persist between sessions |

Both files live in Android's app-private storage
(`context.filesDir`), which is sandboxed by the operating system —
no other app can read them, and CalcNova never uploads them anywhere.

## What CalcNova does NOT do

- It does **not** request the internet permission and makes **no network
  requests** of any kind — no analytics, no crash reporting, no ads, no
  telemetry.
- It does **not** access your camera, contacts, location, microphone,
  files outside its own private storage, or any other device data.
- It does **not** use cookies, advertising identifiers, or any tracking
  technology.
- It does **not** share data with any third party, because it collects
  none to share.

## Permissions

CalcNova requests **zero Android permissions**. You can verify this
yourself in `app/src/main/AndroidManifest.xml` — there is no
`<uses-permission>` entry in the app.

## Your data, your control

- You can clear your calculation history at any time from the **History**
  tab ("Clear all").
- Uninstalling the app deletes both `history.json` and `settings.json`
  along with it, since they exist only inside the app's private storage.
- Because everything stays on-device, there is nothing for CalcNova (or
  anyone else) to delete on a server — there is no server.

## Children's privacy

CalcNova collects no data from anyone, including children, and is safe
for all ages from a privacy standpoint.

## Changes to this policy

If CalcNova ever adds a feature that changes this (for example, an
optional backend for very large calculations, as noted in the project's
README), this Privacy Policy will be updated first, and any such feature
will be opt-in.

## Contact

This project is open source. For questions or concerns, please open an
issue on the project's GitHub repository.
