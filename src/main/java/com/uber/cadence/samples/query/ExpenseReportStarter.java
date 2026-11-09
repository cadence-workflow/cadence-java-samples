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

import com.uber.cadence.client.WorkflowClient;
import com.uber.cadence.client.WorkflowOptions;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Starts an {@link ExpenseReportWorkflow} execution. Use Cadence Web, open the run, then the Home
 * tab.
 */
public final class ExpenseReportStarter {

  private ExpenseReportStarter() {}

  public static void main(String[] args) {
    try {
      WorkflowClient workflowClient = QuerySampleSupport.newWorkflowClient();

      WorkflowOptions options =
          new WorkflowOptions.Builder()
              .setTaskList(QueryConstants.TASK_LIST)
              .setExecutionStartToCloseTimeout(Duration.ofHours(2))
              .setWorkflowId("expense-report-" + UUID.randomUUID())
              .build();

      ExpenseReportWorkflow.WorkflowIface workflow =
          workflowClient.newWorkflowStub(ExpenseReportWorkflow.WorkflowIface.class, options);

      // Starts the workflow asynchronously — the starter can exit immediately.
      WorkflowClient.start(workflow::run, sampleReport());
      System.out.println(
          "Started ExpenseReportWorkflow. In Cadence Web, open the run, then the Home tab.");
      System.exit(0);
    } catch (RuntimeException e) {
      if (QuerySampleSupport.printHintIfDomainMissing(e)) {
        System.exit(1);
      }
      throw e;
    }
  }

  private static ExpenseReportModels.ExpenseReport sampleReport() {
    ExpenseReportModels.ExpenseReport report = new ExpenseReportModels.ExpenseReport();
    report.employee = "Sam Lee";
    report.department = "Engineering";
    report.currency = "USD";
    List<ExpenseReportModels.ExpenseItem> items = new ArrayList<>();
    items.add(item("Hotel, 2 nights", "Travel", 412.8));
    items.add(item("Client dinner", "Meals", 96.25));
    items.add(item("Conference pass", "Training", 350));
    report.items = items;
    return report;
  }

  private static ExpenseReportModels.ExpenseItem item(
      String description, String category, double amount) {
    ExpenseReportModels.ExpenseItem item = new ExpenseReportModels.ExpenseItem();
    item.description = description;
    item.category = category;
    item.amount = amount;
    return item;
  }
}
