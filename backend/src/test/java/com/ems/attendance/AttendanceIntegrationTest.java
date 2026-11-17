package com.ems.attendance;

import com.ems.attendance.config.AttendanceTimeConfiguration;
import com.ems.attendance.service.AttendanceService;
import com.ems.employee_management_system.EmployeeManagementSystemApplication;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.nullValue;

@SpringBootTest(classes = EmployeeManagementSystemApplication.class)
class AttendanceIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AttendanceService attendanceService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void completesAttendanceAndSupportsDailyAndMonthlyHistory() throws Exception {
        long employeeId = 987654321L;
        LocalDate today = LocalDate.now(AttendanceTimeConfiguration.BUSINESS_ZONE);

        mockMvc.perform(post("/api/attendance/{employeeId}/check-in", employeeId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeId").value(employeeId))
                .andExpect(jsonPath("$.attendanceDate").value(today.toString()))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.checkOutAt").value(nullValue()))
                .andExpect(jsonPath("$.workingMinutes").value(nullValue()));

        mockMvc.perform(post("/api/attendance/{employeeId}/check-in", employeeId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());

        mockMvc.perform(get("/api/attendance/{employeeId}/daily", employeeId)
                        .param("date", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(post("/api/attendance/{employeeId}/check-out", employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.checkOutAt").exists())
                .andExpect(jsonPath("$.workingMinutes").isNumber());

        mockMvc.perform(post("/api/attendance/{employeeId}/check-out", employeeId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());

        mockMvc.perform(get("/api/attendance/{employeeId}/daily", employeeId)
                        .param("date", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mockMvc.perform(get("/api/attendance/{employeeId}/monthly", employeeId)
                        .param("year", Integer.toString(today.getYear()))
                        .param("month", Integer.toString(today.getMonthValue())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].attendanceDate").value(today.toString()))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));
    }

    @Test
    void concurrentCheckoutRequestsOnlyAllowOneToSucceed() throws Exception {
        long employeeId = 987654322L;
        attendanceService.checkIn(employeeId);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Integer> first = executor.submit(
                    () -> checkoutAfterSignal(employeeId, ready, start));
            Future<Integer> second = executor.submit(
                    () -> checkoutAfterSignal(employeeId, ready, start));

            assertTrue(ready.await(5, TimeUnit.SECONDS), "Both checkout tasks should be ready.");
            start.countDown();

            List<Integer> statuses = List.of(
                    first.get(10, TimeUnit.SECONDS),
                    second.get(10, TimeUnit.SECONDS));
            assertTrue(statuses.contains(200), "One checkout should succeed.");
            assertTrue(statuses.contains(409), "The competing checkout should conflict.");
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    private int checkoutAfterSignal(
            long employeeId, CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown();
        if (!start.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Timed out waiting to start concurrent checkout.");
        }
        return mockMvc.perform(post("/api/attendance/{employeeId}/check-out", employeeId))
                .andReturn()
                .getResponse()
                .getStatus();
    }
}
