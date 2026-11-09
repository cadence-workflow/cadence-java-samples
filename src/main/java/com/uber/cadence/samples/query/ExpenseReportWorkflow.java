/*
 *  Copyright 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *  Modifications copyright (C) 2017 Uber Technologies, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"). You may not
 *  use this file except in compliance with the License. A copy of the License is
 *  located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 *  or in the "license" file accompanying this file. This file is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */

package com.uber.cadence.samples.query;

import static com.uber.cadence.samples.query.ExpenseReportModels.STATUS_APPROVED;
import static com.uber.cadence.samples.query.ExpenseReportModels.STATUS_CANCELED;
import static com.uber.cadence.samples.query.ExpenseReportModels.STATUS_EXPIRED;
import static com.uber.cadence.samples.query.ExpenseReportModels.STATUS_PARTIALLY_APPROVED;
import static com.uber.cadence.samples.query.ExpenseReportModels.STATUS_PENDING;
import static com.uber.cadence.samples.query.ExpenseReportModels.STATUS_REJECTED;
import static com.uber.cadence.samples.query.QueryConstants.EXPENSE_REPORT_WORKFLOW_TYPE;
import static com.uber.cadence.samples.query.QueryConstants.TASK_LIST;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.uber.cadence.workflow.QueryMethod;
import com.uber.cadence.workflow.SignalMethod;
import com.uber.cadence.workflow.Workflow;
import com.uber.cadence.workflow.WorkflowMethod;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CancellationException;

/**
 * Waits for a reviewer to approve or reject an expense report. The Markdoc query named {@code home}
 * is the workflow Home tab in Cadence Web: it shows the report, the review actions, the decision,
 * and a list of other expense reports. The Home tab needs Cadence Web with {@code
 * CADENCE_WORKFLOW_HOME_ENABLED=true}.
 */
public final class ExpenseReportWorkflow {

  private ExpenseReportWorkflow() {}

  /**
   * The {@code name} on each {@code @SignalMethod} must match the {@code signalName} in the Markdoc
   * template.
   */
  public interface WorkflowIface {

    @WorkflowMethod(
      name = EXPENSE_REPORT_WORKFLOW_TYPE,
      executionStartToCloseTimeoutSeconds = 7200,
      taskList = TASK_LIST
    )
    ExpenseReportModels.ExpenseDecision run(ExpenseReportModels.ExpenseReport report);

    /** Cadence Web shows the query named "home" as the workflow Home tab. */
    @QueryMethod(name = "home")
    MarkdownFormattedResponse homeQuery();

    @SignalMethod(name = "approve")
    void approve(ExpenseReportModels.ExpenseReviewSignal signal);

    @SignalMethod(name = "approve_partial")
    void approvePartial(ExpenseReportModels.ExpenseReviewSignal signal);

    @SignalMethod(name = "reject")
    void reject(ExpenseReportModels.ExpenseReviewSignal signal);
  }

  public static final class WorkflowImpl implements WorkflowIface {

    private static final DateTimeFormatter TIME_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);
    private static final Duration REVIEW_TIMEOUT = Duration.ofHours(1);
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private ExpenseReportModels.ExpenseReport report = new ExpenseReportModels.ExpenseReport();
    private final ExpenseReportModels.ExpenseDecision decision =
        new ExpenseReportModels.ExpenseDecision();
    private long submittedAtMillis;
    private String workflowId;

    // Signal handlers only record the first review. run() applies it, after it knows the amount.
    private String reviewSignalName;
    private ExpenseReportModels.ExpenseReviewSignal review;

    public WorkflowImpl() {
      decision.status = STATUS_PENDING;
    }

    @Override
    public ExpenseReportModels.ExpenseDecision run(ExpenseReportModels.ExpenseReport input) {
      report = input;
      submittedAtMillis = Workflow.currentTimeMillis();
      workflowId = Workflow.getWorkflowInfo().getWorkflowId();

      double requested = 0;
      if (report.items != null) {
        for (ExpenseReportModels.ExpenseItem item : report.items) {
          if (item.amount < 0) {
            throw new IllegalArgumentException(
                String.format(Locale.US, "item \"%s\" has a negative amount", item.description));
          }
          requested += item.amount;
        }
      }
      decision.requestedAmount = requested;

      Map<String, Object> attrs = new HashMap<>();
      attrs.put("CustomKeywordField", STATUS_PENDING);
      attrs.put("CustomDoubleField", requested);
      Workflow.upsertSearchAttributes(attrs);

      try {
        boolean reviewed = Workflow.await(REVIEW_TIMEOUT, () -> review != null);
        if (reviewed) {
          applyReview(requested);
        } else {
          decide(STATUS_EXPIRED, 0, "System", "No review within 1 hour");
        }
      } catch (CancellationException e) {
        decide(STATUS_CANCELED, 0, "System", "Workflow canceled");
        upsertStatus();
        throw e;
      }

      upsertStatus();
      return decision;
    }

    private void applyReview(double requested) {
      String reviewer = review.reviewer;
      String comment = review.comment;
      switch (reviewSignalName) {
        case "approve":
          decide(STATUS_APPROVED, requested, reviewer, comment);
          break;
        case "approve_partial":
          // Clamp to [0, requested]. The full amount is an approval and zero is a rejection.
          double amount = Math.max(0, Math.min(review.amount, requested));
          if (amount >= requested) {
            decide(STATUS_APPROVED, requested, reviewer, comment);
          } else if (amount == 0) {
            decide(STATUS_REJECTED, 0, reviewer, comment);
          } else {
            decide(STATUS_PARTIALLY_APPROVED, amount, reviewer, comment);
          }
          break;
        case "reject":
        default:
          decide(STATUS_REJECTED, 0, reviewer, comment);
          break;
      }
    }

    private void decide(String status, double approved, String reviewer, String comment) {
      decision.status = status;
      decision.approvedAmount = approved;
      decision.reviewer = reviewer;
      decision.comment = comment;
      decision.decidedAtMillis = Workflow.currentTimeMillis();
    }

    private void upsertStatus() {
      Map<String, Object> attrs = new HashMap<>();
      attrs.put("CustomKeywordField", decision.status);
      Workflow.upsertSearchAttributes(attrs);
    }

    private void recordReview(String signalName, ExpenseReportModels.ExpenseReviewSignal signal) {
      if (review == null) {
        reviewSignalName = signalName;
        review = signal;
      }
    }

    @Override
    public void approve(ExpenseReportModels.ExpenseReviewSignal signal) {
      recordReview("approve", signal);
    }

    @Override
    public void approvePartial(ExpenseReportModels.ExpenseReviewSignal signal) {
      recordReview("approve_partial", signal);
    }

    @Override
    public void reject(ExpenseReportModels.ExpenseReviewSignal signal) {
      recordReview("reject", signal);
    }

    @Override
    public MarkdownFormattedResponse homeQuery() {
      return new MarkdownFormattedResponse(
          makeExpenseHome(), "Expense report from " + report.employee);
    }

    // Cadence Web fills in the domain, cluster, workflow ID and run ID of the action tags below.
    // The workflow ID is only added to the workflows query, to leave out the current report.
    private String makeExpenseHome() {
      String currency = report.currency;
      boolean pending = STATUS_PENDING.equals(decision.status);

      StringBuilder md = new StringBuilder();
      md.append(
          String.format(
              Locale.US,
              "**%s** (%s) submitted this report at %s.\n\n",
              escapeTags(report.employee),
              escapeTags(report.department),
              formatTime(submittedAtMillis)));
      md.append("| Status | Requested | Approved |\n");
      md.append("|--------|-----------|----------|\n");
      md.append(
          String.format(
              Locale.US,
              "| %s | %s | %s |\n\n",
              getStatusBadge(decision.status),
              escapeTags(money(decision.requestedAmount, currency)),
              escapeTags(money(decision.approvedAmount, currency))));

      md.append("### Line items\n\n");
      md.append("| Description | Category | Amount |\n");
      md.append("|-------------|----------|--------|\n");
      if (report.items != null) {
        for (ExpenseReportModels.ExpenseItem item : report.items) {
          md.append(
              String.format(
                  Locale.US,
                  "| %s | %s | %.2f |\n",
                  escapeTags(item.description),
                  escapeTags(item.category),
                  item.amount));
        }
      }
      md.append("\n");

      if (pending) {
        md.append("### Review\n\n");
        md.append("Approve or reject within 1 hour, or the report expires.\n\n");
        md.append(
            String.format(
                Locale.US,
                "{%% signal signalName=\"approve\" label=%s "
                    + "input={\"reviewer\":\"finance-ops\",\"comment\":\"Looks good\"} /%%}\n",
                markdocString("✓ Approve " + money(decision.requestedAmount, currency))));
        md.append(
            String.format(
                Locale.US,
                "{%% input-field name=\"approvedAmount\" label=\"Approved amount\" "
                    + "type=\"number\" default=%.2f required=true /%%}\n",
                decision.requestedAmount / 2));
        md.append(
            "{% signal signalName=\"approve_partial\" label=\"◐ Approve amount\" "
                + "input={\"reviewer\":\"finance-ops\",\"comment\":\"Partly in policy\","
                + "\"amount\":$approvedAmount} /%}\n");
        md.append(
            "{% signal signalName=\"reject\" label=\"✗ Reject\" "
                + "input={\"reviewer\":\"finance-ops\",\"comment\":\"Missing receipts\"} /%}\n");
      } else {
        md.append("### Decision\n\n");
        md.append(
            String.format(
                Locale.US,
                "**%s** decided at %s: *%s*\n",
                escapeTags(decision.reviewer),
                formatTime(decision.decidedAtMillis),
                escapeTags(decision.comment)));
      }

      md.append("\n---\n\n### Other expense reports\n\n");
      md.append(
          String.format(
              Locale.US,
              "{%% workflows query=%s "
                  + "columns=\"WorkflowID,CloseStatus,CustomKeywordField,StartTime,CloseTime\" /%%}\n",
              markdocString(
                  "WorkflowType = '"
                      + EXPENSE_REPORT_WORKFLOW_TYPE
                      + "' AND WorkflowID != '"
                      + workflowId
                      + "'")));

      md.append("\n---\n\n### New report\n\n");
      md.append(
          String.format(
              Locale.US,
              "{%% input-field name=\"newAmount\" "
                  + "label=%s type=\"number\" required=true /%%}\n\n",
              markdocString("Amount for the new receipt (" + currency + ")")));
      md.append(
          String.format(
              Locale.US,
              "{%% start\n"
                  + "\tworkflowType=\"%s\"\n"
                  + "\tlabel=\"Submit another report\"\n"
                  + "\ttaskList=\"%s\"\n"
                  + "\tsdkLanguage=\"JAVA\"\n"
                  + "\ttimeoutSeconds=7200\n"
                  + "\tinput={\"employee\":%s,\"department\":%s,\"currency\":%s,"
                  + "\"items\":[{\"description\":\"Team lunch\",\"category\":\"Meals\","
                  + "\"amount\":$newAmount}]}\n"
                  + "/%%}\n",
              EXPENSE_REPORT_WORKFLOW_TYPE,
              TASK_LIST,
              markdocString(report.employee),
              markdocString(report.department),
              markdocString(currency)));
      return md.toString();
    }

    private static String money(double amount, String currency) {
      return String.format(Locale.US, "%.2f %s", amount, currency);
    }

    private static String formatTime(long millis) {
      return TIME_FMT.format(Instant.ofEpochMilli(millis));
    }

    /** Stops user text from adding Markdoc tags. Markdown shows {@code &#123;} as "{". */
    private static String escapeTags(String s) {
      return s == null ? "" : s.replace("{%", "&#123;%");
    }

    /**
     * Quotes s as a Markdoc string. Markdoc reads JSON string escapes, so report text can't end the
     * tag.
     */
    private static String markdocString(String s) {
      return GSON.toJson(s);
    }

    private static String getStatusBadge(String status) {
      switch (status) {
        case STATUS_PENDING:
          return "🟡 **Pending review**";
        case STATUS_APPROVED:
          return "🟢 **Approved**";
        case STATUS_PARTIALLY_APPROVED:
          return "🟠 **Partially approved**";
        case STATUS_REJECTED:
          return "🔴 **Rejected**";
        case STATUS_EXPIRED:
          return "⚪ **Expired**";
        case STATUS_CANCELED:
          return "⚫ **Canceled**";
        default:
          return status;
      }
    }
  }
}
