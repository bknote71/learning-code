package com.bknote71.rdbms.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DailyPostCount {
    Long memberId;
    LocalDate createdDate;
    Long count;
}
