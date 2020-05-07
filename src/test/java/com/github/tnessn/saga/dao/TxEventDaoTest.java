package com.github.tnessn.saga.dao;
 
import javax.annotation.Resource;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.tnessn.saga.Application;
import com.github.tnessn.saga.dao.TxEventDao;
 
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class}) // 指定启动类
public class TxEventDaoTest {
 
 
    @Resource
    private TxEventDao txEventDao;
 
    
}
