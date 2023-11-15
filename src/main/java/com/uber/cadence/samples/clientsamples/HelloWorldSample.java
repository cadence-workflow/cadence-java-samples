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

package com.uber.cadence.samples.clientsamples;

import com.uber.cadence.WorkflowExecution;
import com.uber.cadence.client.WorkflowClient;
import com.uber.cadence.client.WorkflowOptions;
import com.uber.cadence.samples.spring.common.Constant;
import com.uber.cadence.samples.spring.models.SampleMessage;
import com.uber.cadence.samples.spring.workflows.HelloWorldWorkflow;
import java.time.Duration;

public class HelloWorldSample {
  public static void main(String[] args) {
    WorkflowClient workflowClient = CadenceUtil.getWorkflowClient();
    WorkflowOptions workflowOptions =
        new WorkflowOptions.Builder()
            .setExecutionStartToCloseTimeout(Duration.ofSeconds(30))
            .setTaskList(Constant.TASK_LIST)
            .build();

    HelloWorldWorkflow helloWorldWorkflow =
        workflowClient.newWorkflowStub(HelloWorldWorkflow.class, workflowOptions);
    WorkflowExecution execution =
        WorkflowClient.start(helloWorldWorkflow::sayHello, new SampleMessage("Uber"));
    System.out.printf("WorkflowID: %s, RunID: %s", execution.getWorkflowId(), execution.getRunId());
  }
}
