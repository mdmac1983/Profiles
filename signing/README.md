# Signing key

Profiles (`app.orionmd.profiles`) is signed with the owner's OWN key (created 2026-11-09). The key is NOT in this repo:
`android.yml` reads it from the GitHub Actions secret `SIGNING_KEYSTORE_BASE64` (base64 of the keystore; alias and
passwords `launcher`). Without the secret a build is a debug-signed PREVIEW (a 14-day artifact, not a Release).

The key's SHA-256 is pinned in `tools/keystore.fingerprint`; `tools/checkkeystore.sh` and `tools/verify-apk.sh` refuse
any other key. In-app updates only install an APK signed with this same key, so keep a backup of the keystore outside
GitHub. Never regenerate it once Profiles is installed: a different key cannot update the app in place.

(The original project committed its key here on purpose; that public key was removed when this fork moved to its own.)
