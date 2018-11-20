package org.seckill.service.impl;

import org.apache.commons.collections.MapUtils;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Component , @Service ,@Dao ,@Controller

@Service
public class SeckillServiceImpl implements SeckillService{
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    //注入Service 依赖
    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    @Autowired
    private RedisDao redisDao;

    //md5盐值字符串，用于混淆md5
    private final String slat = "asdfjoqjaf45645234,l;qwe./,";

    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll();
    }

    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    /**
     * 使用Redis缓存，降低服务器压力
     * @param seckillId
     * @return
     */
    public Exposer exportSeckillUrl(long seckillId) {
//        1.缓存中查询
         Seckill seckill =redisDao.getSeckill(seckillId);
         if (seckill==null){
//             2.访问数据库
             seckill = seckillDao.queryById(seckillId);
             if(seckill==null){
                 return new Exposer(false,seckillId);
             }else {
//                 3.放入redis
                 redisDao.putSeckill(seckill);
             }
         }
        Date start = seckill.getStartTime();
        Date end = seckill.getEndTime();
        //获取系统当前时间进行比较
        Date nowtime = new Date();
        if(nowtime.getTime()>end.getTime()||
                nowtime.getTime()<start.getTime()){
            return new Exposer(false,seckillId,nowtime.getTime(),start.getTime(),end.getTime());
        }
           //md5值，非可逆
           String md5 =getMd5(seckillId);
        return new Exposer(true,md5,seckillId);
    }

    private String getMd5(long seckillId){
        String base = seckillId+"/"+slat;
        //使用Spring的工具类生成md5
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    @Transactional
    /**
     * 使用注解控制事务的优点
     * 1.开发团队形成一致的约定，明确标注事务方法的编程风格
     * 2.保证事务方法的执行时间尽可能短，不要穿插其他的网络操作或者剥离到事务方法的外部
     * 3.并不是所有的方法都需要事务,如只有一条修改，或只读操作
     */
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {

        if(md5==null||!md5.equals(getMd5(seckillId))){
            throw new SeckillException("seckill date rewrite");
        }
        //执行秒杀逻辑：减库存+记录购买行为
        Date now = new Date();
        try{
            //记录购买行为
            int insertCount = successKilledDao.insertSuccessKilled(seckillId,userPhone);
            if(insertCount<=0){
                //重复秒杀
                throw new RepeatKillException("seckill Repeated");
            }else{
                //减库存，热点商品竞争
                int updateCount = seckillDao.reduceNumber(seckillId,now);
                if(updateCount<=0){
                    //没有更新到记录，说明秒杀已经结束
                    throw new SeckillCloseException("seckill is close");
                }else{
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS,successKilled);
                }
            }
        }catch (SeckillCloseException e1){
            throw e1;
        } catch (RepeatKillException e2){
            throw e2;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            //将所有的编译期异常转换为运行期异常,Spring声明式事务管理会执行rollback操作
            throw new SeckillException("seckill inner error:"+e.getMessage());
        }



    }

    @Override
    public SeckillExecution executeSeckillByProcedure(long seckillId, long userPhone, String md5) {
        if(md5!=null||!md5.equals(getMd5(seckillId))){
            return new SeckillExecution(seckillId,SeckillStatEnum.DATE_REWRITE);
        }
        Date killTime = new Date();
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("seckillId",seckillId);
        map.put("phone",userPhone);
        map.put("killTime",killTime);
        map.put("result",null);
        //执行存储过程，result被赋值
        try {
            seckillDao.killByProcedure(map);
            //获取result
            int result = MapUtils.getInteger(map,"result",-2);
            if(result==1){
                SuccessKilled sk = successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                return  new SeckillExecution(seckillId,SeckillStatEnum.SUCCESS,sk);
            }else{
                return new SeckillExecution(seckillId,SeckillStatEnum.stateOf(result));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return new SeckillExecution(seckillId,SeckillStatEnum.INNER_ERROR);
        }
    }
}
