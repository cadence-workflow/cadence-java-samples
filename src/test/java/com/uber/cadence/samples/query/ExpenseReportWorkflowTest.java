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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.uber.cadence.client.WorkflowClient;
import com.uber.cadence.client.WorkflowOptions;
import com.uber.cadence.client.WorkflowStub;
import com.uber.cadence.samples.query.ExpenseReportModels.ExpenseDecision;
import com.uber.cadence.samples.query.ExpenseReportModels.ExpenseItem;
import com.uber.cadence.samples.query.ExpenseReportModels.ExpenseReport;
import com.uber.cadence.samples.query.ExpenseReportModels.ExpenseReviewSignal;
import com.uber.cadence.testing.TestWorkflowEnvironment;
import com.uber.cadence.worker.Worker;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/** Unit test for {@link ExpenseReportWorkflow}. Doesn't use an external Cadence service. */
public class ExpenseReportWorkflowTest {

  /** Prints a history of the workflow under test in case of a test failure. */
  @Rule
  public TestWatcher watchman =
      new TestWatcher() {
        @Override
        protected void failed(Throwable e, Description description) {
          if (testEnv != null) {
            System.err.println(testEnv.getDiagnostics());
            testEnv.close();
          }
        }
      };

  private TestWorkflowEnvironment testEnv;
  private WorkflowClient workflowClient;

  @Before
  public void setUp() {
    testEnv = TestWorkflowEnvironment.newInstance();
    Worker worker = testEnv.newWorker(QueryConstants.TASK_LIST);
    worker.registerWorkflowImplementationTypes(ExpenseReportWorkflow.WorkflowImpl.class);
    testEnv.start();
    workflowClient = testEnv.newWorkflowClient();
  }

  @After
  public void tearDown() {
    testEnv.close();
  }

  private ExpenseReportWorkflow.WorkflowIface newStub() {
    WorkflowOptions options =
        new WorkflowOptions.Builder()
            .setTaskList(QueryConstants.TASK_LIST)
            .setExecutionStartToCloseTimeout(Duration.ofHours(2))
            .build();
    return workflowClient.newWorkflowStub(ExpenseReportWorkflow.WorkflowIface.class, options);
  }

  private static ExpenseReport sampleReport() {
    ExpenseReport report = new ExpenseReport();
    report.employee = "Sam Lee";
    report.department = "Engineering";
    report.currency = "USD";
    List<ExpenseItem> items = new ArrayList<>();
    items.add(item("Hotel, 2 nights", "Travel", 412.8));
    items.add(item("Client dinner", "Meals", 96.25));
    items.add(item("Conference pass", "Training", 350));
    report.items = items;
    return report;
  }

  private static ExpenseItem item(String description, String category, double amount) {
    ExpenseItem item = new ExpenseItem();
    item.description = description;
    item.category = category;
    item.amount = amount;
    return item;
  }

  @Test
  public void testPartialApproval() {
    ExpenseReportWorkflow.WorkflowIface workflow = newStub();
    WorkflowClient.start(workflow::run, sampleReport());

    ExpenseReviewSignal signal = new ExpenseReviewSignal();
    signal.reviewer = "finance-ops";
    signal.comment = "Partly in policy";
    signal.amount = 100;
    workflow.approvePartial(signal);

    ExpenseDecision decision = WorkflowStub.fromTyped(workflow).getResult(ExpenseDecision.class);
    assertEquals(ExpenseReportModels.STATUS_PARTIALLY_APPROVED, decision.status);
    assertEquals(100, decision.approvedAmount, 0.001);
    assertEquals(859.05, decision.requestedAmount, 0.001);
    assertEquals("finance-ops", decision.reviewer);
  }

  @Test
  public void testExpiresWithoutReview() {
    ExpenseReportWorkflow.WorkflowIface workflow = newStub();
    WorkflowClient.start(workflow::run, sampleReport());

    // The test service skips time while it waits for the result.
    ExpenseDecision decision = WorkflowStub.fromTyped(workflow).getResult(ExpenseDecision.class);
    assertEquals(ExpenseReportModels.STATUS_EXPIRED, decision.status);
    assertEquals(0, decision.approvedAmount, 0.001);
    assertEquals("System", decision.reviewer);
  }

  @Test
  public void testHomeQueryWhilePending() {
    ExpenseReportWorkflow.WorkflowIface workflow = newStub();
    WorkflowClient.start(workflow::run, sampleReport());

    MarkdownFormattedResponse response = workflow.homeQuery();
    assertEquals("formattedData", response.getCadenceResponseType());
    assertEquals("text/markdown", response.getFormat());
    assertEquals("Expense report from Sam Lee", response.getTitle());
    assertTrue(response.getData().contains("signalName=\"approve\""));
    assertTrue(response.getData().contains("sdkLanguage=\"JAVA\""));
  }
}
