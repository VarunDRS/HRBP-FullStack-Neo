package com.cars24.slack_hrbp.data;

import lombok.Data;
import java.util.Objects;

@Data
public class Pair {
    String userId;
    String username;

    public Pair(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair pair = (Pair) o;
        return Objects.equals(userId, pair.userId) && Objects.equals(username, pair.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username);
    }
}

