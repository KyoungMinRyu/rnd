package com.lam.rnd.firebaseTest;

import com.lam.rnd.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class firebaseTest
{
    @Resource
    UserService userService;

    @Test
    public void test()
    {
        userService.selectUser();
    }

}
