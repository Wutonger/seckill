//存放主要交互的js代码
//javascript模块化

var seckill={
    //封装秒杀相关的ajax的url
    URL : {
            now : function () {
                return '/seckill/time/now';
            },
            exposer : function (seckillId) {
                return '/seckill/'+seckillId+'/exposer';
            },
            execution : function (seckillId,md5) {
                return  '/seckill/'+seckillId+'/'+md5+'/execution';
            }
    },
    //验证手机号
    validatePhone : function (phone) {
            if(phone&&phone.length==11&&!isNaN(phone))
                return true;
            else
                return false;
    },
    handleSeckillkill : function (seckillId,node) {
      //处理秒杀逻辑
        node.hide().html('<button class="btn btn-primary btn-lg " id="killBtn">开始秒杀</button>');
        $.post(seckill.URL.exposer(seckillId),{},function (result) {
           //在回掉函数中控制显示逻辑
            if(result&&result['success']){
                var exposer =result['date'];
                if(exposer['exposed']){
                    //开启秒杀,获取秒杀地址/]'
                    var md5 = exposer['md5'];
                    var killUrl = seckill.URL.execution(seckillId,md5);
                    console.log('killUrl:'+killUrl);
                    $('#killBtn').one('click',function () {
                        //绑定执行秒杀请求的操作
                        //1.先禁用按钮
                        $(this).addClass('disabled');
                        //2.发送秒杀请求
                        $.post(killUrl,{},function (result) {
                            if(result&&result['success']){
                                var killResult = result['date'];
                                var state = killResult['state'];
                                var stateInfo = killResult['stateInfo'];
                                //显示秒杀结果的成功与否
                                node.html('<span class="label label-success">'+stateInfo+'</span>');
                            }
                        })
                    })
                    node.show();
                }else{
                    //时间出现偏差，未开启秒杀
                    var now =exposer['now'];
                    var start = exposer['start'];
                    var end = exposer['end'];
                    seckill.countdown(seckillId,now,start,end);
                }
            }else {
                console.log('result:'+result);
            }
        })
    },
    countdown : function (seckillId,nowTime,startTime,endTime) {
        var seckillBox = $('#seckill-box');
        //1.时间的判断
        if(nowTime>endTime){
            //秒杀结束
            seckillBox.html('秒杀结束');
        }else if(nowTime<startTime){
            //秒杀未开始,计时
            var killTime = new Date(startTime+1000);
            seckillBox.countdown(killTime,function (event) {
                var format =event.strftime('秒杀开始计时 %D天 %H时 %M分 %S秒');
                seckillBox.html(format);
            }).on('finish.countdown',function () {
                seckill.handleSeckillkill(seckillId,seckillBox);
            });
        }else{
            seckill.handleSeckillkill(seckillId,seckillBox);
        }
    },
    //详情页秒杀逻辑
    detail : {
            //详情页初始化
            init : function(params) {
                //用户的手机验证和登陆，计时交互
                //在cookie查找手机号
                var killPhone = $.cookie('killPhone');
                if(!seckill.validatePhone(killPhone)){
                    //绑定phone
                    var killPhoneModal = $('#killPhoneModal');
                    killPhoneModal.modal({
                        //显示弹出层
                       show:true,
                        //禁止位置拖拽关闭
                       backdrop:'static',
                        //禁止键盘关闭
                       keyboard:false
                    });
                    $('#killPhoneBtn').click(function () {
                        var inputPhone = $('#killPhoneKey').val();
                        if(seckill.validatePhone(inputPhone)){
                            //将电话号码存入cookie
                            $.cookie('killPhone',inputPhone,{expires:7,path:'/seckill'});
                             //刷新页面
                             window.location.reload();
                        }else{
                           $('#killPhoneMessage').hide().html('<label class="label label-danger">手机号错误</label>').show(300);
                        }
                    })
                }
                //已经登陆,计时交互
                var startTime = params['startTime'];
                var endTime = params['endTime'];
                var seckillId = params['seckillId'];
                $.get(seckill.URL.now(),{},function (result) {
                       if(result&&result['success']){
                            var nowTime =result['date'];
                            //时间判断
                            seckill.countdown(seckillId,nowTime,startTime,endTime);
                       }else{
                           console.log('result:'+result);
                       }
                });
            }
    }
}