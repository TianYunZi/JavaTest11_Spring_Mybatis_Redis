package org.redis.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * Created by Admin on 2017/5/18.
 */
@Service
public class ParamReader {

    private Environment environment;

    @Autowired
    public ParamReader(Environment environment) {
        this.environment = environment;
    }

    public String getEnvParam(String key) {
        return environment.getProperty(key);
    }

    public String[] getEnvParamList(String key) {
        String property = environment.getProperty(key);
        if (property.contains(",")) {
            return property.split(",");
        } else {
            return new String[]{property};
        }
    }
}
