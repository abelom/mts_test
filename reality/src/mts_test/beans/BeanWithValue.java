package mts_test.beans;

import org.springframework.beans.factory.annotation.Value;

public class BeanWithValue {
    @Value("${x}")
    String xxx;
}
