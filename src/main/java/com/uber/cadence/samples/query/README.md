# Custom Workflow Controls (Markdoc query samples)

> **Concept overview:** [Workflow queries with formatted data](https://cadenceworkflow.io/docs/concepts/workflow-queries-formatted-data)

These workflows return **formatted markdown** from workflow queries so [Cadence Web](https://github.com/cadence-workflow/cadence-web) can render **Custom Workflow Controls**: Markdoc tags such as `{% signal %}` and `{% start %}` become buttons that signal this workflow or start a new one.

**Requires Cadence Web v4.0.14 or newer** for markdown rendering and interactive controls.

Constants used in the Markdoc snippets (`domain`, `taskList`, `cluster`) match [`QueryConstants.java`](QueryConstants.java): domain **`samples-domain`**, task list **`query`**, cluster **`cluster0`**.

## Prerequisites

1. Cadence server running (e.g. Docker Compose from the [Cadence repo](https://github.com/uber/cadence)).
2. **Cadence Web v4.0.14+** connected to the same cluster.
3. From the repo root, build: `./gradlew build`

### Register the domain (required once per cluster)

Starters use domain **`samples-domain`**. If you see `Domain samples-domain does not exist`, register it **before** starting workflows:

```bash
./gradlew -q execute -PmainClass=com.uber.cadence.samples.common.RegisterDomain
```

Or with the Cadence CLI:

```bash
cadence --domain samples-domain domain register
```

See also the root [README.md](../../../../../../../../README.md).

## Run the worker (terminal 1)

Leave this process running:

```bash
cd /path/to/cadence-java-samples
./gradlew -q execute -PmainClass=com.uber.cadence.samples.query.QueryWorker
```

## Start a workflow (terminal 2)

Run **one** of the starters (each starts a new workflow execution on task list `query`):

**Markdown Query** — query name `Signal` in the Web UI:

```bash
./gradlew -q execute -PmainClass=com.uber.cadence.samples.query.MarkdownQueryStarter
```

**Lunch Vote** — query name `options`:

```bash
./gradlew -q execute -PmainClass=com.uber.cadence.samples.query.LunchVoteStarter
```

**Order Fulfillment (ops dashboard)** — query name `dashboard`:

```bash
./gradlew -q execute -PmainClass=com.uber.cadence.samples.query.OrderFulfillmentStarter
```

**Expense Report (workflow Home page)** — query name `home`, shown in the **Home** tab:

```bash
./gradlew -q execute -PmainClass=com.uber.cadence.samples.query.ExpenseReportStarter
```

## Use Cadence Web

1. Open the UI (e.g. [http://localhost:8088](http://localhost:8088)) and select domain **`samples-domain`**.
2. Open the workflow run you started.
3. Open the **Query** tab.
4. Choose the query type (**`Signal`**, **`options`**, or **`dashboard`**) and run it.
5. Use the rendered buttons; re-run the query to refresh markdown after signals.

## Source layout

| Sample | Workflow interface / impl | Query name |
|--------|---------------------------|------------|
| Markdoc + activity | [`MarkdownQueryWorkflow.java`](MarkdownQueryWorkflow.java) | `Signal` |
| Lunch voting | [`LunchVoteWorkflow.java`](LunchVoteWorkflow.java) | `options` |
| Order dashboard | [`OrderFulfillmentWorkflow.java`](OrderFulfillmentWorkflow.java) | `dashboard` |
| Expense report | [`ExpenseReportWorkflow.java`](ExpenseReportWorkflow.java) | `home` |

## Expense Report (workflow Home page)

This sample shows a custom **Home** page in Cadence Web. The workflow takes an expense report, waits for a reviewer, and returns the decision. Its `home` query returns Markdoc that renders the report, review buttons, and a list of other expense reports.

It needs a Cadence Web build with Home pages. Start Cadence Web with `CADENCE_WORKFLOW_HOME_ENABLED=true`. The list of other reports needs advanced visibility.

### Start the workflow

```bash
./gradlew -q execute -PmainClass=com.uber.cadence.samples.query.ExpenseReportStarter
```

Or with the Cadence CLI:

```bash
cadence --domain samples-domain \
  workflow start \
  --tl query \
  --et 7200 \
  --workflow_type ExpenseReportWorkflow \
  --input '{"employee":"Sam Lee","department":"Engineering","currency":"USD","items":[{"description":"Hotel, 2 nights","category":"Travel","amount":412.8},{"description":"Client dinner","category":"Meals","amount":96.25},{"description":"Conference pass","category":"Training","amount":350}]}'
```

### What happens

Open the workflow in Cadence Web. The Home tab shows the report and these review actions:

- **Approve**: approves the full amount.
- **Approve amount**: approves the amount in the **Approved amount** field. It starts at half the requested amount.
- **Reject**: rejects the report.

With no review within 1 hour, the report expires. After a decision, the workflow completes, and the Home page shows the decision.

Below the review actions, the page shows a list of other expense reports and a **Submit another report** button that starts a new workflow with the amount you type.

### Home page tags

The returned Markdoc uses these tags. Each one gets domain, cluster, workflow ID and run ID from the page:

| Tag | Shows |
|-----|-------|
| `{% workflows query="..." /%}` | A list of workflows that match a visibility query. Needs advanced visibility. |
| `{% signal /%}` and `{% start /%}` | Buttons that signal this workflow or start a new one. The `start` tag sets `sdkLanguage="JAVA"`. |
| `{% input-field /%}` | A text or number input. A button sends its value as `$name`, for example `input={"amount":$approvedAmount}`. |

