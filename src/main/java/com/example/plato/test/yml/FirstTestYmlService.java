package com.example.plato.test.yml;

import com.example.plato.test.model.FirstModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/6 18:52
 */
@Service
@Slf4j
public class FirstTestYmlService {

    public Long testMethod1(FirstModel longList) {
        log.info("FirstTestYmlService#testMethod1#longList:{}", longList);
        return 1000L;
    }

    public Integer testMethod2(Long id) {
        log.info("FirstTestYmlService#testMethod2#id:{}", id);
        return 123;
    }

}
