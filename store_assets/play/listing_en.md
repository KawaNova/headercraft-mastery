# JWT Inspector - Token Decoder & Claims Analyzer

## Title (Max 30 characters)
JWT Inspector - Token Decoder

## Short Description (Max 80 characters)
100% On-Device JWT Token Decoder with Expiration Countdown & Mock Token Builder

## Full Description (Max 4000 characters)
Worried about pasting production JWT tokens into random online web decoders? Need to quickly inspect token expiration and custom claims without sending credentials across the network?

JWT Inspector is a privacy-first, 100% on-device JSON Web Token decoder and testing utility crafted for backend engineers, frontend developers, security specialists, and API testers.

Everything is processed locally on your phone. No network requests, no telemetry, no leaks.

---

### 🌟 Key Features

#### 1. 🛡️ 100% On-Device & Privacy Guaranteed
- Decodes and verifies Base64Url tokens completely on your local device.
- Safely inspect production tokens, bearer authentication headers, and sensitive customer payload data with total confidence.

#### 2. 🎨 Tri-Color Token Highlighting
- **Header (Rose Red)**: Algorithmic specifications (`alg`: RS256, HS256, ES256), token types (`typ`), and key IDs (`kid`).
- **Payload (Violet Purple)**: Formatted, beautified JSON claims data with one-tap clipboard copy.
- **Signature (Cyan Blue)**: Cryptographic signature hash block.

#### 3. ⏰ Expiration (exp) Realtime Countdown
- Automatically identifies `exp` (Expiration Time), `iat` (Issued At), and `nbf` (Not Before) timestamps.
- Formats dates in both local timezone and UTC.
- Real-time status badges: "Active (expires in 47 min 20 sec)" or "Expired (3 hours ago)" to debug authentication failures fast.

#### 4. 📊 Built-in Claims Glossary
- Clear explanations for registered and standard claims: `iss` (Issuer), `sub` (Subject), `aud` (Audience), `roles` (RBAC), `scope`, `auth_time`, and custom application properties.

#### 5. ⚡ Mock JWT Generator (HS256)
- Generate signed mock JWTs for API prototyping and testing.
- Custom Subject (`sub`), Name, Email, Role, configurable expiration (1h / 24h / 7d / 30d), and secret keys.

#### 6. 📚 6 Production-Grade Templates
- Curated sample tokens from industry standard providers:
  - Auth0 / OpenID Connect (OIDC) ID Token
  - Firebase Authentication Client Token
  - AWS Cognito User Pool Token
  - Standard OAuth 2.0 Access Token
  - GitHub App Server-to-Server Token
  - Expired Token (for testing 401 error handling)

#### 7. 📁 Secure Local Storage
- Bookmark frequently used tokens with custom names and notes in local DataStore.
