package com.atguigu.gmall1213.task.scheduled;

import com.atguigu.gmall1213.common.constant.MqConst;
import com.atguigu.gmall1213.common.service.RabbitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;




@Component   //扫描
@EnableScheduling   //开启定时任务

public class ScheduledTask {
    @Autowired
    private RabbitService rabbitService;
 /**
 * 每天凌晨1点执行
 */
 //每隔30秒执行
  @Scheduled(cron = "0/30 * * * * ?")
 // cron  定时任务的表达式 分 时 日 周 月 年
 //每天夜里一点发送一个消息
  //@Scheduled(cron = "0 0 1 * * ?")
  public void taskActivity() {
      System.out.println("定时任务来了");
    rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK,  MqConst.ROUTING_TASK_1, "");
 }

    /**
     * 每天下午18点执行
     */
//@Scheduled(cron = "0/35 * * * * ?")
    @Scheduled(cron = "0 0 18 * * ?")
    public void task18() {

        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_18, "");
    }

}
