#数据库初始化脚本

#创建数据库
CREATE DATABASE seckill;
#使用数据库
use seckill;
#创建秒杀库存表
CREATE TABLE seckill(
  seckill_id bigint NOT NULL  '商品库存id',
  name varchar(120) NOT NULL COMMENT '商品名称',
  number int NOT NULL COMMENT '库存数量',
  start_time timestamp NOT NULL COMMENT '秒杀开启时间',
  end_time TIMESTAMP NOT NULL  COMMENT '秒杀结束时间',
  create_time TIMESTAMP NOT NULL DEFAULT current_timestamp COMMENT '创建时间',
  PRIMARY KEY (seckill_id),
  key idx_start_time(start_time),
  key idx_end_time(end_time),
  key idx_create_time(create_time)
)ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8 COMMENT ='秒杀库存表';
#初始化秒杀库存表数据
  insert into
    seckill(name,number,start_time,end_time)
  values
  ('1000元秒杀iphone8',100,'2015-11-01 00;00;00','2015-11-02 00;00;00'),
  ('500元秒杀ipad pro',200,'2015-11-01 00;00;00','2015-11-02 00;00;00'),
  ('1500元秒杀iphone8 plus',100,'2015-11-01 00;00;00','2015-11-02 00;00;00'),
  ('1000元秒杀iphone8',100,'2015-11-01 00;00;00','2015-11-02 00;00;00');

#秒杀成功明细表
#用户登录认证相关信息

CREATE table success_killed(
  seckill_id BIGINT NOT NULL COMMENT'秒杀商品id',
  user_phone BIGINT NOT NULL COMMENT '用户手机号',
  state TINYINT NOT NULL DEFAULT -1 COMMENT '-1:无效 0:成功 1:已付款',
  create_time TIMESTAMP NOT NULL DEFAULT current_timestamp COMMENT '创建时间',
  PRIMARY KEY (seckill_id,user_phone),/* 联合主键 */
  KEY idx_create_time(create_time)
)ENGINE=InnoDB  DEFAULT  CHARSET=utf8 COMMENT ='秒杀成功明细表';

