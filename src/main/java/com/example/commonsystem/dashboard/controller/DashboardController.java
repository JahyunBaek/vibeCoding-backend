package com.example.commonsystem.dashboard.controller;

import com.example.commonsystem.common.ApiResponse;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

  @GetMapping("/summary")
  public ApiResponse<DashboardSummary> summary() {
    DashboardSummary data = new DashboardSummary(
        4850,
        180.1,
        15231.89,
        20.1,
        List.of(240,300,200,278,189,239,278,189),
        List.of(12000.0, 14000.0, 13200.0, 15800.0, 12100.0),
        List.of(
            new Series("You", List.of(20,15,12,25,24,30,26,27)),
            new Series("Avg", List.of(22,19,16,14,18,15,17,21))
        )
    );
    return ApiResponse.ok(data);
  }

  public record DashboardSummary(
      int subscriptions,
      double subscriptionsChangePct,
      double revenue,
      double revenueChangePct,
      List<Integer> subscriptionsBars,
      List<Double> revenueLine,
      List<Series> exerciseSeries
  ) {}

  public record Series(
      String name,
      List<Integer> points
  ) {}
}
