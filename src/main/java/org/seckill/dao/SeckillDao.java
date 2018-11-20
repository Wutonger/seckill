package org.seckill.dao;

import org.apache.ibatis.annotations.Param;
import org.seckill.entity.Seckill;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface SeckillDao {

    int reduceNumber(@Param("seckillId")long seckillId, @Param("killTime")Date killTime);


    Seckill queryById(Long seckillId);


    List<Seckill> queryAll();

    void killByProcedure(Map<String,Object> map);
}
