# Signing key (committed on purpose)

Tj, 2026-11-02: "it can be signed by github if possible, security isn't important". `launcher-keystore.b64` is the base64 of
`app/sideload.jks` (alias/password `launcher`). `android.yml` restores it (or the `SIGNING_KEYSTORE_BASE64` secret if one is ever set,
which wins) and signs every release with it, so GitHub signs and each update installs over the last one.
Because the repo is PUBLIC, anyone can sign an APK with this key; that only matters if someone tricks Tj into installing it. If that ever
matters: make the repo private or move the key to the secret and delete this file (a key change forces one uninstall).
Never regenerate it once anything is installed. SHA-256 is pinned in `tools/keystore.fingerprint`.
