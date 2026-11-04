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

import java.util.List;

/** Domain model and signal payloads for {@link ExpenseReportWorkflow}. */
public final class ExpenseReportModels {

  private ExpenseReportModels() {}

  public static final String STATUS_PENDING = "pending";
  public static final String STATUS_APPROVED = "approved";
  public static final String STATUS_PARTIALLY_APPROVED = "partially_approved";
  public static final String STATUS_REJECTED = "rejected";
  public static final String STATUS_EXPIRED = "expired";
  public static final String STATUS_CANCELED = "canceled";

  /**
   * Workflow input. Field names match the JSON keys in the CLI input and in the Markdoc {@code
   * input=} attribute of the {@code start} tag.
   */
  public static class ExpenseReport {
    public String employee;
    public String department;
    public String currency;
    public List<ExpenseItem> items;
  }

  /** One line of an expense report. */
  public static class ExpenseItem {
    public String description;
    public String category;
    public double amount;
  }

  /** Workflow result. Times are epoch millis, like the other query samples. */
  public static class ExpenseDecision {
    public String status;
    public double requestedAmount;
    public double approvedAmount;
    public String reviewer;
    public String comment;
    public long decidedAtMillis;
  }

  /** Payload for the {@code approve}, {@code approve_partial} and {@code reject} signals. */
  public static class ExpenseReviewSignal {
    public String reviewer;
    public String comment;
    public double amount;
  }
}
