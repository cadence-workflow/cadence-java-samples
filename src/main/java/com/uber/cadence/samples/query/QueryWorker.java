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

import static com.uber.cadence.samples.query.QueryConstants.TASK_LIST;

import com.uber.cadence.client.WorkflowClient;
import com.uber.cadence.samples.common.SampleConstants;
import com.uber.cadence.worker.Worker;
import com.uber.cadence.worker.WorkerFactory;

/**
 * Hosts all Custom Workflow Controls sample workflows and {@link
 * MarkdownQueryWorkflow.MarkdownQueryActivities}. Run this before starting any workflow in {@link
 * QueryConstants#TASK_LIST}.
 */
public final class QueryWorker {

  private QueryWorker() {}

  public static void main(String[] args) {
    WorkflowClient workflowClient = QuerySampleSupport.newWorkflowClient();

    WorkerFactory factory = WorkerFactory.newInstance(workflowClient);
    Worker worker = factory.newWorker(TASK_LIST);

    // Registration scans each interface for @WorkflowMethod, @QueryMethod, and @SignalMethod
    // annotations and registers handlers. Signal names default to "InterfaceName::methodName"
    // unless @SignalMethod(name=...) overrides it.
    worker.registerWorkflowImplementationTypes(
        MarkdownQueryWorkflow.WorkflowImpl.class,
        LunchVoteWorkflow.WorkflowImpl.class,
        OrderFulfillmentWorkflow.WorkflowImpl.class,
        ExpenseReportWorkflow.WorkflowImpl.class);
    worker.registerActivitiesImplementations(
        new MarkdownQueryWorkflow.MarkdownQueryActivitiesImpl());

    // Non-blocking: the worker threads poll the task list in the background and this process
    // stays alive until killed.
    factory.start();
    System.out.println(
        "QueryWorker listening on task list \""
            + TASK_LIST
            + "\" (domain \""
            + SampleConstants.DOMAIN
            + "\").");
  }
}
