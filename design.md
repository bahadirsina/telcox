# TelcoX Signal Atlas

## Document role

This file is the design-system source of truth for a high-fidelity desktop web application generated in Stitch. Apply every rule consistently across the complete product. Do not turn this into a marketing website, a mobile app, a generic admin template, or a collection of disconnected dashboard cards.

Product name: **TelcoX**  
Experience name: **Signal Atlas**  
Product type: Internal telecom CRM and operations platform  
Primary locale: Turkish (`tr-TR`)  
Currency: TRY  
Timezone: Europe/Istanbul  
Primary viewport: 1440 x 1024 desktop  
Supported desktop range: 1280-1920 px  
Tablet adaptation: 1024 px  
Mobile application: out of MVP scope

## Product truth

TelcoX manages a telecom subscriber lifecycle across event-driven microservices:

- Customer registration, identity documents, address/contact data, KYC approval or rejection
- Postpaid tariff, add-on and VAS catalog management with versions and effective dates
- New-line order flow with payment, automatic subscription activation and MSISDN allocation
- Subscription state changes: active, suspended, reactivated and terminated
- Voice, SMS and data quota tracking with 80% and 100% thresholds
- Monthly bill runs, invoices, PDF output and payment collection
- Failed payment attempts with retry windows at 24, 72 and 168 hours
- SMS and email templates, dispatch history and consent preferences
- Ticket queues, comments, assignment, resolution and SLA monitoring
- Users, roles, permissions and immutable audit logs
- Service health, Kafka topics, Debezium connectors, traces and logs

### Primary actors

- Call center agent: searches customers, sees a complete subscriber context, solves tickets and performs permitted service actions
- Dealer representative: creates a customer, uploads KYC documents and starts a new-line order
- System administrator: manages catalog, users, roles and audit records
- Billing operator: runs billing, investigates invoices and payment retries
- Platform operator: monitors microservices, Kafka, Debezium and trace links
- Customer: represented as CRM data, not as the primary user of this internal application

## Design thesis

**Signal Atlas** treats telecom operations as a living map, not a spreadsheet warehouse. Every customer, order, payment and event is a coordinate in a larger network. The interface must feel calm, exact and expensive while still supporting dense operational work.

The signature visual language is **signal cartography**:

- Fine contour lines, orbital arcs and connected points imply coverage, routing and data flow.
- A narrow illuminated **Pulse Spine** marks the active area of important panels and lifecycle timelines.
- Concentric quota rings and radial tick marks communicate capacity without copying common speedometer widgets.
- Event identifiers, MSISDN values, correlation IDs and timestamps use restrained mono typography.
- Curves and linework are decorative context only; never place them behind dense text or data tables.

### Desired emotional outcome

- Confident, not loud
- Technical, not cold
- Premium, not ornamental
- Memorable, not eccentric
- Dense, but never cramped
- Operationally serious, but unmistakably a telecommunications product

## Originality constraints

- Do not copy Turkcell yellow/navy, Vodafone red/white, Türk Telekom blue/purple, or any real operator's identity.
- Do not use a generic blue SaaS dashboard, cyberpunk neon, glassmorphism everywhere, bento-card overload or stock telecom photography.
- Do not use antenna towers, globe icons, handshake photos, floating 3D SIM cards or generic gradient blobs.
- Do not make every metric a separate rounded card. Prefer composed canvases, rails, bands, tables and inspector panels.
- Do not use excessive gradients, giant corner radii, pill-shaped containers for everything or low-contrast gray text.

## Brand mark

Create an original TelcoX wordmark and compact monogram:

- The monogram is an abstract `X` built from two crossing signal routes with four small terminal nodes.
- It must read as a precise network junction, not as a telecom competitor logo.
- Full wordmark: `TELCOX` in uppercase with slightly expanded tracking.
- Product subtitle: `SIGNAL ATLAS` in small mono capitals.
- Use the monogram in the collapsed navigation rail, login screen and favicon treatment.

## Color system

The default application combines a deep operational shell with warm, readable work surfaces. Pure black and pure white are not primary surfaces.

### Foundation

| Token | Hex | Role |
|---|---:|---|
| `ink-950` | `#07110F` | App shell, navigation, dark hero surfaces |
| `ink-900` | `#0D1815` | Elevated dark surface |
| `ink-800` | `#17231F` | Dark divider and hover surface |
| `ivory-50` | `#FAF9F5` | Primary work canvas |
| `ivory-100` | `#F2EFE6` | Secondary work surface |
| `stone-200` | `#DDD9CE` | Light borders and dividers |
| `stone-500` | `#797E78` | Muted light-theme text |
| `text-dark` | `#111815` | Primary text on light surfaces |
| `text-light` | `#EDF1EB` | Primary text on dark surfaces |

### Brand and data accents

| Token | Hex | Role |
|---|---:|---|
| `signal-coral` | `#FF5A43` | Primary action, selected route, brand energy |
| `spectrum-lime` | `#C7F36B` | Live network pulse, healthy status, positive emphasis |
| `ion-violet` | `#8D7CFF` | Analytics, selected data series, secondary action |
| `copper` | `#C9855B` | Premium detail, archived/versioned state |
| `signal-blue` | `#62A8FF` | Informational state only, never the dominant brand color |
| `amber` | `#F5B84B` | Warning, nearing SLA or quota threshold |
| `fault` | `#F24F68` | Error, overdue, failed or destructive action |

### Color behavior

- Use `signal-coral` for one primary action per view, active navigation trace and key selection.
- Use `spectrum-lime` sparingly for live/healthy telemetry; never for large backgrounds.
- Dark navigation uses `text-light`; primary content uses `text-dark` on warm ivory.
- Status must always combine color, icon and text.
- Charts use no more than four simultaneous accent colors.
- Minimum text contrast is WCAG AA.

## Typography

- Display and headings: **Sora**, fallback `Inter`, `Arial`, sans-serif
- Interface and body: **Inter**, fallback `Arial`, sans-serif
- IDs, amounts, MSISDN, timestamps and telemetry: **IBM Plex Mono**, fallback monospace
- Turkish glyphs must render correctly.
- Use tabular numerals for prices, quotas, invoice totals and telemetry.

### Type scale

| Role | Size / line height | Weight |
|---|---|---|
| Display | 44 / 52 | 650 |
| Page title | 32 / 40 | 650 |
| Section title | 22 / 30 | 650 |
| Panel title | 16 / 24 | 650 |
| Body | 14 / 22 | 450 |
| Dense table | 13 / 20 | 450 |
| Label | 12 / 16 | 600 |
| Micro / telemetry | 11 / 16 | 500 mono |

Use sentence case in Turkish. Avoid all caps except tiny telemetry labels, IDs and the brand subtitle.

## Spacing and geometry

- Base spacing unit: 4 px
- Primary spacing steps: 4, 8, 12, 16, 24, 32, 48, 64
- Application grid: 12 columns, 24 px gutters
- Main content padding: 32 px at 1440, 24 px at 1280, 20 px at 1024
- Navigation rail: 72 px collapsed
- Context navigation panel: 224 px expanded
- Utility bar: 64 px
- Standard control height: 40 px
- Compact control height: 32 px
- Data table row: 48 px; compact operations table: 40 px
- Panel radius: 14 px
- Control radius: 9 px
- Badge radius: 6 px; never fully pill-shaped unless it is a binary toggle or filter chip
- Light border: 1 px `stone-200`
- Dark border: 1 px rgba(237, 241, 235, 0.12)
- Shadows: soft and low; prefer borders and surface tone to large floating shadows

## Application shell

### Navigation

Use a deep `ink-950` left rail with the TelcoX monogram at the top. The rail can expand into a 224 px contextual panel. Navigation groups:

1. `Genel Bakış`
2. `Müşteriler`
3. `Katalog`
4. `Siparişler`
5. `Abonelikler`
6. `Kullanım`
7. `Faturalama`
8. `Ödemeler`
9. `Bildirimler`
10. `Talepler`
11. `Yönetim`
12. `Platform Ops`

The active item uses a coral route line and a small terminal node, not a filled rounded rectangle. Show text labels in expanded mode and accessible tooltips in collapsed mode.

### Utility bar

- Breadcrumb or current operational context on the left
- Global command/search trigger in the center: `Müşteri, MSISDN, fatura, sipariş ara...` with `⌘ K`
- Environment indicator such as `PROD` or `UAT`
- Compact live service health indicator
- Notifications
- Current user, role and avatar menu

### Page header

- Eyebrow for bounded context, then page title and one-line operational purpose
- Contextual actions aligned right; one coral primary action maximum
- Optional freshness line such as `Son güncelleme 14:32:08`

## Signature components

### Network Pulse Ribbon

A slim horizontal operational ribbon that combines service health, Kafka throughput, open SLA risks and bill-run state. Use small sparklines and mono values. This is a composed strip, not four cards.

### Pulse Spine

A 2 px illuminated vertical line with terminal nodes. Use it for order saga steps, KYC decision history, payment retries and ticket activity. Completed nodes are lime, current nodes coral, waiting nodes muted and failed nodes fault red.

### Customer Signal Header

A compact 360-degree customer summary:

- Name and masked identity number
- Customer ID and KYC state
- Primary MSISDN and subscription state
- Risk/SLA flags
- Quick actions based on permission
- No oversized avatar; identity is data-led

### Quota Orbit

Three precise concentric or offset rings for data, voice and SMS. Include used/remaining values, threshold markers at 80% and 100%, period dates and accessible textual equivalents. Never resemble a car speedometer.

### Saga Rail

A horizontal desktop lifecycle map with an optional vertical detail rail:

`Sipariş oluşturuldu → Ödeme → MSISDN ayırma → Abonelik aktivasyonu → Bildirim`

Each node opens event payload, timestamp, correlation ID and retry/compensation details in a right inspector.

### Data tables

- Sticky header, deliberate column widths, row selection and keyboard navigation
- First column can pin on wide datasets
- Filters appear in a compact query bar, not a giant form
- Pagination and result count remain visible
- Row actions appear on focus/hover and in a clear overflow menu
- Support empty, loading, error and partial-data states

### Right inspector

Use a 420-520 px right-side inspector for audit details, event payloads, quick edits and contextual history. Keep the primary list visible behind it. Do not navigate away for every detail.

### Status language

Use Turkish status labels with a compact icon and shape:

- `Aktif`, `Başarılı`, `Sağlıklı`
- `Bekliyor`, `İşleniyor`, `Planlandı`
- `Askıda`, `Riskli`, `SLA Yaklaşıyor`
- `Başarısız`, `Gecikmiş`, `Bağlantı Yok`
- `Sonlandırıldı`, `İptal`, `Arşiv`

## Forms and actions

- Labels stay above fields; never rely on placeholder-only forms.
- Show formatting examples for TCKN, MSISDN, ICCID and money fields.
- Group long forms into meaningful sections with a progress spine.
- Preserve entered data between wizard steps.
- Inline validation is specific and Turkish.
- Destructive actions require reason, impact statement and confirmation.
- KYC approve/reject, subscription termination, refund and bill-run trigger must show permission-aware confirmation dialogs.
- Use optimistic feedback only for reversible low-risk actions.
- Show correlation ID in error details for support workflows.

## Data visualization

- Prefer direct labels over legends.
- Use restrained area/line charts for volume over time.
- Use stacked horizontal bars for lifecycle distribution.
- Use a compact event stream for Kafka and notification throughput.
- Use countdown bars for SLA and payment retry windows.
- Avoid 3D charts, decorative pie charts and misleading dual axes.
- Every chart has a text summary and exact-value tooltip.

## Motion

- Micro interaction: 120-160 ms
- Panel and inspector transition: 220-260 ms
- Page-level staged reveal: maximum 420 ms
- Easing: smooth deceleration, never bouncy
- Signal lines may trace once on initial load; they must then become still.
- Live values may crossfade; do not continuously pulse large surfaces.
- Respect `prefers-reduced-motion` and provide an effectively static experience.

## Screen blueprints

Create the following high-fidelity screens as one coherent system. Use realistic Turkish sample data and consistent entities across screens.

### 1. Giriş / Yetkilendirme

- Split composition: compact login area and a dark signal-map field
- TelcoX wordmark, environment, SSO/Keycloak action and security note
- Sample state for expired session and insufficient role
- Avoid a generic centered white login card

### 2. Genel Bakış / Operasyon Kokpiti

- Network Pulse Ribbon across the top
- Lifecycle flow showing today's onboarding from application to activation
- Operational summaries for customers, orders, invoices, tickets and service health
- Exceptions-first zone: failed payments, SLA risks, stuck sagas, KYC queue
- Compact activity stream with domain events
- Quick actions: `Yeni müşteri`, `Yeni hat siparişi`, `Bill-run başlat`
- This screen must feel like an editorial operations cockpit, not a uniform card grid

### 3. Müşteri Listesi ve Arama

- Search by name, customer ID, masked TCKN, MSISDN
- Filters for KYC, customer status, subscription status, city and creation date
- Dense table with status, active line count, open ticket count and outstanding balance
- Saved views such as `KYC Bekleyenler`, `Borç Riski`, `Yeni Aktivasyonlar`
- Right inspector preview on row selection

### 4. Müşteri 360 Detayı

- Customer Signal Header
- Tabs: `Özet`, `Abonelikler`, `Kullanım`, `Faturalar`, `Ödemeler`, `Talepler`, `Belgeler`, `Audit`
- Unified lifecycle timeline spanning customer, order, subscription, payment and notifications
- Mask PII by default; permission-based reveal leaves an audit trace
- Show current balance, last invoice, quota risk and open SLA at a glance

### 5. Müşteri Oluşturma / Düzenleme ve KYC

- Guided sections: kimlik, iletişim, adres, belgeler, doğrulama
- Drag-and-drop ID document upload with preview and metadata
- Side-by-side entered data and document review for KYC
- Approve and reject actions with reason; show decision history on Pulse Spine
- Include success, validation and duplicate-customer warning states

### 6. Ürün Kataloğu Yönetimi

- Hybrid catalog tree and comparison canvas for tariffs, add-ons and VAS
- Tariff rows/cards expose code, monthly fee, data/voice/SMS, type, status and effective dates
- Version timeline and `Mevcut aboneleri koru` behavior
- Create/edit side sheet with linked add-ons and target segment
- Preview a tariff as it will appear inside the order wizard

### 7. Yeni Hat Sipariş Sihirbazı

- Five steps: `Müşteri → Tarife → Ek Paketler → Ödeme → Onay`
- Persistent order summary rail with monthly and one-time totals
- Search/select existing customer or continue from customer detail
- Clear plan comparison with data, voice, SMS and effective dates
- Payment uses masked card details and an idempotency state
- Final confirmation visualizes the event chain that will run after submission

### 8. Sipariş Detayı ve Saga Timeline

- Dominant Saga Rail with current state, elapsed time and service ownership
- Event log includes timestamps, correlation IDs and expandable payload details
- Highlight waiting, retrying, failed and compensation states
- Cancel action explains which compensations and refunds will be triggered
- Include one successful and one failed/compensating visual state

### 9. Abonelikler / Abonelik Detayı

- Search by customer, MSISDN, ICCID, IMSI and tariff
- Detail includes MSISDN, SIM identities, tariff snapshot and activation dates
- State machine visualization for active, suspended, reactivated and terminated
- Permission-aware actions with reason and effective timestamp
- Related order, invoices, usage and audit links

### 10. Kullanım ve Kota

- Quota Orbit for data, voice and SMS
- Billing-period selector and usage trend
- Threshold event markers at 80% and 100%
- Filterable CDR history with voice/SMS/data types
- Overage projection and exact amount passed to billing
- Clear `veri gecikmeli olabilir` freshness state

### 11. Faturalama Kontrol Merkezi

- Bill-run command area with scope, period, estimated subscriber count and confirmation
- Live run progress with stages and failure count
- Invoice list with issued, paid, overdue, cancelled and partially paid states
- Totals for revenue, tax, overdue balance and collection rate
- Saved view for exceptions and downloadable reconciliation

### 12. Fatura Detayı ve PDF Görüntüleme

- Split view: structured invoice data and embedded PDF preview
- Line items: monthly fee, add-ons, overage, VAS, taxes
- Payment and notification history
- Actions: download PDF, resend, mark for investigation
- Preserve invoice audit and immutable issued values

### 13. Ödeme Konsolu

- Secure payment form with method selection and masked saved cards
- Payment detail includes amount, invoice, external reference and idempotency key
- Attempt timeline and retry schedule at 24/72/168 hours
- Countdown to next retry and exhausted state
- Refund workflow with amount, reason and impact confirmation

### 14. Bildirim Merkezi

- Tabs: `Şablonlar`, `Gönderim Geçmişi`, `Tercihler`
- Template editor for SMS and email with variables and live preview
- Delivery status stream and channel filters
- Customer opt-in/opt-out state is explicit and cannot be overridden silently
- Failed dispatch inspector with retry and correlation details

### 15. Talep / Ticket Çalışma Alanı

- Three-pane operations layout: queue, ticket conversation/detail, customer context
- Queues for unassigned, mine, SLA risk, waiting customer and resolved
- Visible SLA countdown with severity and ownership
- Comments, internal notes, assignment and resolution actions
- Customer 360 context remains visible without leaving the ticket

### 16. Yönetim, Roller ve Audit

- Users, roles and permissions with a readable permission matrix
- Role-based navigation preview
- Audit log with actor, action, entity, before/after summary, timestamp and correlation ID
- Sensitive reveal and export actions are themselves audited
- Diff inspector for old/new JSON values

### 17. Platform Ops / Network Room

- Dark NOC-style surface within the same design system
- Service topology map for gateway, identity, customer, catalog, order, subscription, usage, billing, payment, notification and ticket services
- Health, latency and error rate per service
- Kafka topic throughput and consumer lag
- Debezium connector status, last event and restart action
- Links to traces and logs with correlation ID
- Make this an operational map, not a decorative architecture diagram

### 18. System States and Pattern Sheet

- Empty results, initial empty product, loading skeleton, partial data, offline, 403, 404 and RFC 7807 error states
- Toasts, confirmations, side inspectors, tables, charts, filters and status patterns
- Light content surface and dark ops surface examples
- Accessibility annotations for focus order, keyboard actions and contrast

## Prototype flows

Connect these clickable journeys:

1. `Giriş → Genel Bakış → Müşteri ara → Müşteri 360 → Yeni hat siparişi → Ödeme → Sipariş saga → Aktif abonelik`
2. `Genel Bakış → KYC kuyruğu → Belge incele → Onayla → Müşteri 360`
3. `Faturalama → Bill-run başlat → Çalışma ilerlemesi → Fatura detayı → Ödeme`
4. `Talep kuyruğu → Ticket detayı → Müşteri bağlamı → Yorum → Çöz`
5. `Platform Ops → Sorunlu servis → Debezium connector → Trace/log bağlantısı`

## Responsive behavior

- At 1280 px, collapse contextual navigation first; do not compress dense tables below readable widths.
- At 1024 px, use the 72 px rail, stack secondary panels and move inspectors to full-height overlays.
- Complex tables may scroll horizontally with pinned identity columns.
- Order wizard and KYC remain step-based and usable at 1024 px.
- Do not generate a separate consumer mobile app.

## Accessibility and operational safety

- WCAG 2.2 AA contrast, visible focus and complete keyboard support
- Touch targets at least 40 x 40 px even on dense views
- Never communicate status with color alone
- Text remains selectable and zoomable to 200%
- All icon-only controls have Turkish tooltips and accessible labels
- Charts have summaries; quota visuals have numeric alternatives
- Focus is trapped correctly in dialogs and returned to the invoking control
- Destructive actions clearly state affected customer, subscription, invoice or event
- PII is masked by default and reveal actions are permission-gated

## Content style

- Interface language is Turkish; technical identifiers remain in their canonical English form.
- Use concise operational language: `KYC bekliyor`, `Ödeme tekrar denenecek`, `Saga 4 dk bekliyor`.
- Use realistic but fictional Turkish names, cities and data.
- Example MSISDN: `+90 532 ••• •• 47`
- Example customer: `Derya Yılmaz`
- Example tariff: `Atlas 40 GB`
- Example invoice: `FTR-2026-061842`
- Example order: `ORD-26-10482`
- Example correlation ID: `9f3c1b7a...e21d`
- Dates: `19 Haz 2026, 14:32`
- Money: `1.249,90 TL`

## Final quality gate

The result must look like one senior product design team built the entire system. Before finishing:

- Verify all 18 screens share the same shell, typography, tokens and component behavior.
- Verify entities and sample values remain consistent across related screens.
- Verify the core onboarding, billing and quota acceptance scenarios are visually represented.
- Verify no screen falls back to a generic card dashboard or competitor-inspired telecom branding.
- Verify dense data remains readable and every major screen includes loading, empty and error thinking.
- Verify prototype links complete the five defined journeys.
- Prefer a smaller number of exceptional, fully resolved compositions over decorative filler.
