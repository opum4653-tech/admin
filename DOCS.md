# NINMODS Admin Console Architecture & Data Contract Documentation

## 1. Navigation & TopAppBar Hierarchy Doctrine

### 1.1 Root Drawer Pages vs. Sub/Edit Pages
The admin console strictly divides destinations into two navigation tiers:

1. **Root Main Destinations** (`DASHBOARD`, `USERS`, `KEYS`, `DEPOSITS`, `UPLOAD`, `BROADCAST`, `SETTINGS`, `PROFILE`):
   - **Navigation Icon:** Official 3-Line Hamburger Menu (`Icons.Filled.Menu`) which slides out the `ModalNavigationDrawer`.
   - **TopBar Title:** Single concise label (`Dashboard`, `Users`, `License Keys`, `Deposits`, `Mod Deploy`, `Push Alerts`, `Configurations`, `Admin Profile`).
   - **Actions:** Quick action shortcuts (Push Alerts, Refresh, Profile).

2. **Sub-Pages / Edit Pages / Details Pages** (`GENERATE_KEY`, `ADD_USER`, `USER_DETAIL`, `TICKET_CHAT`):
   - **Navigation Icon:** The 3-line hamburger menu is **strictly replaced** by the Back Arrow (`Icons.AutoMirrored.Filled.ArrowBack`).
   - **TopBar Title:** Displays the exact context dynamically:
     - `USER_DETAIL` -> `"User: <username>"`
     - `TICKET_CHAT` -> `"Ticket #<id>"`
     - `GENERATE_KEY` -> `"Generate Keys"`
     - `ADD_USER` -> `"Add User"`
   - **Zero Duplicate Headers:** Sub-screens have no internal header row with back buttons; all navigation is unified into the system `TopAppBar`.
   - **Hardware BackHandler:** Pressing system back returns to the parent list (`GENERATE_KEY` -> `KEYS`, `ADD_USER`/`USER_DETAIL` -> `USERS`, `TICKET_CHAT` -> `SUPPORT`).

---

## 2. Responsive Layout & Anti-Wrapping Grid System

1. **`ResponsiveContentBox`:**
   - Enforces a centered `maxWidth = 760.dp` wrapper across all devices.
   - Automatically adapts from small 360dp phones to 600dp foldables and 1200dp tablets/desktops without stretching or breaking form layouts.

2. **Anti-Wrapping Chip Architecture:**
   - 4-item priority selectors use a symmetrical 2x2 grid (`Row` + `Modifier.weight(1f)` across 2 items per row), ensuring words never break into multi-line fragments.
   - Dynamic lists use `horizontalScroll(rememberScrollState())` with `maxLines = 1` for natural chip widths and swipeable UX.

3. **Telemetry Card Uniformity:**
   - All 8 metrics use identical `UniformMetricCard` components (height `112.dp`, centered icon, bold headline, label subtitle, zero text clipping).

4. **Action Button Spinner Standard (`LoadingButton`):**
   - Every asynchronous operation (login, key generation, user creation, mod upload, broadcast dispatch) displays an official `CircularProgressIndicator(20.dp)` and disables clicks during network execution.

---

## 3. Backend & Client Data Model Parity (100% Contract Match)

| Endpoint | Method | AdminApiClient Function | Backend Controller Method | Database Tables Touched |
| :--- | :--- | :--- | :--- | :--- |
| `/api/admin/auth/login` | POST | `login(user, pass)` | `adminCtrl.login` | `users` (`role = 'admin'`) |
| `/api/admin/dashboard` | GET | `getDashboard()` | `adminCtrl.getDashboard` | `users`, `license_keys`, `deposits`, `support_tickets` |
| `/api/admin/users` | GET | `getUsers(search, role)` | `adminCtrl.getUsers` | `users` |
| `/api/admin/users/create` | POST | `createUser(u, e, p, r, b)` | `adminCtrl.createUser` | `users`, `user_wallets` |
| `/api/admin/users/:id/action` | POST | `executeUserAction(id, act, rsn)` | `adminCtrl.userAction` | `users` (`status = 'banned'/'active'`) |
| `/api/admin/users/:id/balance` | POST | `updateUserBalance(id, act, amt, n)` | `adminCtrl.updateUserBalance` | `users`, `wallet_transactions` |
| `/api/admin/users/:id/role` | POST | `changeUserRole(id, role)` | `adminCtrl.changeUserRole` | `users` (`role`) |
| `/api/admin/keys` | GET | `getKeys(status, search)` | `adminCtrl.getKeys` | `license_keys` |
| `/api/admin/keys/generate` | POST | `generateBulkKeys(q, h, t, g, d, n, uId)` | `adminCtrl.generateBulkKeys` | `license_keys` |
| `/api/admin/keys/:id/action` | POST | `keyAction(id, act, hrs)` | `adminCtrl.keyAction` | `license_keys` |
| `/api/admin/deposits/all` | GET | `getAllDeposits(status)` | `adminCtrl.getAllDeposits` | `deposits`, `users` |
| `/api/admin/deposits/pending` | GET | `getPendingDeposits()` | `adminCtrl.getPendingDeposits` | `deposits`, `users` |
| `/api/admin/deposits/manual` | POST | `addManualDeposit(uId, amt, m, n)` | `adminCtrl.addManualDeposit` | `deposits`, `users`, `wallet_transactions` |
| `/api/admin/deposits/:id/approve` | POST | `approveDeposit(id, amt)` | `adminCtrl.approveDeposit` | `deposits`, `users`, `wallet_transactions` |
| `/api/admin/deposits/:id/reject` | POST | `rejectDeposit(id, rsn)` | `adminCtrl.rejectDeposit` | `deposits` |
| `/api/admin/support/tickets` | GET | `getSupportTickets(status)` | `adminCtrl.getSupportTickets` | `support_tickets`, `users` |
| `/api/admin/support/tickets/create` | POST | `createAdminTicket(uId, s, m, p)` | `adminCtrl.createAdminTicket` | `support_tickets`, `support_replies` |
| `/api/admin/support/tickets/:id` | GET | `getTicketReplies(id)` | `adminCtrl.getTicketReplies` | `support_replies` |
| `/api/admin/support/tickets/:id/reply` | POST | `replyTicket(id, msg)` | `adminCtrl.replyTicket` | `support_replies`, `support_tickets` |
| `/api/admin/support/tickets/:id/status` | POST | `updateTicketStatus(id, st)` | `adminCtrl.updateTicketStatus` | `support_tickets` |
| `/api/admin/broadcasts/send` | POST | `sendBroadcastNotification(...)` | `adminCtrl.sendBroadcast` | `broadcast_notifications` |
| `/api/admin/broadcasts` | GET | (Admin & Client Polling) | `adminCtrl.getBroadcasts` | `broadcast_notifications` |
| `/api/admin/mods/upload` | POST | `uploadModZip(type, fn, b64)` | `adminCtrl.uploadModZip` | Local storage (`downloads/external/` & `downloads/kernel/`) |
| `/api/admin/game-configs` | GET | `getGameConfigs()` | `adminCtrl.getGameConfigs` | `game_configurations` |
| `/api/admin/game-configs/:id` | POST | `updateGameConfig(...)` | `adminCtrl.updateGameConfig` | `game_configurations` |
| `/api/admin/plans` | GET | `getAdminPlans()` | `adminCtrl.getAdminPlans` | `subscription_plans` |

---

## 4. Privacy & Hardening Specification
- **Zero Raw Domain/Disk Exposure:** No client screen displays `https://ninmods.smmtor.com/...` or disk paths.
- **Brand Neutrality:** All references to "APatch" scrubbed from production Android UI.
- **Strict Role Isolation:** Loader clients cannot hit `/api/admin/*` routes; all admin APIs reject non-admin tokens with HTTP 403 Forbidden.
