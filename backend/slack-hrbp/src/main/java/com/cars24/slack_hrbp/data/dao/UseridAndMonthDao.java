package com.cars24.slack_hrbp.data.dao;

import java.util.Map;

public interface UseridAndMonthDao {
    public Map<String, Map<String, String>> getUserDetails(String userid);
    Map<String, Map<String, String>> getUserDetails(String userid, String month);
    Map<String, Map<String, String>> getUserDetails(String userid, String frommonth, String tomonth);
}
